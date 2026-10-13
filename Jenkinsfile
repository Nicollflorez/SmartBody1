// Pipeline de lanzamiento de Smartbody (Etapa 5: gestion de lanzamientos).
//
// Fases: Build -> Test -> Package -> Publish -> Release.
//   - Cada ejecucion en main publica un SNAPSHOT en GitHub Packages (repositorio de artefactos).
//   - Con PUBLICAR_RELEASE=true se pide aprobacion, se publica la version oficial, se etiqueta
//     en Git (vX.Y.Z) y el artefacto queda listo para la Etapa 6 (despliegue con Docker/Kubernetes).
//
// Corre en un agente Windows con JDK 17 y el SDK de Android (ANDROID_HOME). Maven y Gradle
// usan su wrapper, asi que no hace falta instalarlos en Jenkins.
// Credencial requerida en Jenkins: 'github-packages' (usuario + token con write:packages y repo).

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()                         // una compilacion a la vez (4 GB de RAM)
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout()
    }

    parameters {
        booleanParam(name: 'PUBLICAR_RELEASE', defaultValue: false,
            description: 'Publica una version oficial (sin -SNAPSHOT), la etiqueta en Git y la deja lista para el despliegue')
        string(name: 'VERSION_RELEASE', defaultValue: '',
            description: 'Version a liberar, por ejemplo 1.0.0. Vacia: se usa la del pom sin -SNAPSHOT')
        string(name: 'API_BASE_URL', defaultValue: 'http://10.0.2.2:8080/',
            description: 'URL del backend que se compila dentro del APK (BuildConfig.API_BASE_URL)')
    }

    environment {
        GITHUB_OWNER   = 'Nicollflorez'
        GITHUB_REPO    = 'SmartBody'
        PACKAGES_URL   = "https://maven.pkg.github.com/${GITHUB_OWNER}/${GITHUB_REPO}"
        MAVEN_SETTINGS = "${WORKSPACE}\\devops\\maven\\settings.xml"
        // Limites de memoria para que Jenkins, Maven y Gradle convivan en 4 GB de RAM.
        MAVEN_OPTS     = '-Xmx512m'
        GRADLE_MEMORIA = '-Dorg.gradle.jvmargs=-Xmx1536m -Pkotlin.daemon.jvmargs=-Xmx768m'
        MVN            = 'mvnw.cmd -B -ntp'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Version con la que se compila y publica todo el lanzamiento.
                    dir('backend') {
                        def versionPom = bat(returnStdout: true,
                            script: "@${MVN} -q help:evaluate -Dexpression=project.version -DforceStdout").trim()
                        if (params.PUBLICAR_RELEASE) {
                            env.VERSION = params.VERSION_RELEASE?.trim() ?: versionPom.replace('-SNAPSHOT', '')
                            bat "${MVN} versions:set -DnewVersion=${env.VERSION} -DgenerateBackupPoms=false"
                        } else {
                            env.VERSION = versionPom
                        }
                    }
                    env.ES_MAIN = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').endsWith('main') ? 'true' : 'false'
                    currentBuild.displayName = "#${BUILD_NUMBER} v${env.VERSION}"
                    echo "Version del lanzamiento: ${env.VERSION} (release=${params.PUBLICAR_RELEASE}, main=${env.ES_MAIN})"
                }
            }
        }

        stage('Build: backend') {
            steps {
                dir('backend') { bat "${MVN} -DskipTests compile" }
            }
        }

        stage('Test: backend') {
            steps {
                dir('backend') { bat "${MVN} test" }
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml' }
            }
        }

        stage('Build + Test: Android') {
            steps {
                dir('android') {
                    bat "gradlew.bat --no-daemon ${GRADLE_MEMORIA} " +
                        "-Psmartbody.apiBaseUrl=${params.API_BASE_URL} " +
                        "-Psmartbody.versionName=${env.VERSION} -Psmartbody.versionCode=${BUILD_NUMBER} " +
                        "testDebugUnitTest assembleRelease"
                }
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'android/app/build/test-results/testDebugUnitTest/*.xml' }
            }
        }

        stage('Package') {
            steps {
                dir('backend') { bat "${MVN} -DskipTests package" }
                archiveArtifacts artifacts: 'backend/target/smartbody-backend-*.jar, android/app/build/outputs/apk/release/*.apk',
                                 fingerprint: true
            }
        }

        stage('Publish: snapshot') {
            // Solo compilaciones de main que no son release: sube el SNAPSHOT al repositorio de artefactos.
            when { allOf { environment name: 'ES_MAIN', value: 'true'; not { expression { params.PUBLICAR_RELEASE } } } }
            steps { publicarArtefactos() }
        }

        stage('Release') {
            when { expression { params.PUBLICAR_RELEASE } }
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    input message: "Aprobar el lanzamiento de Smartbody v${env.VERSION} y publicarlo en GitHub Packages?", ok: 'Publicar'
                }
                publicarArtefactos()
                withCredentials([usernamePassword(credentialsId: 'github-packages',
                                                  usernameVariable: 'GITHUB_ACTOR', passwordVariable: 'GITHUB_TOKEN')]) {
                    script {
                        def partes = env.VERSION.tokenize('.')
                        partes[-1] = (partes[-1].toInteger() + 1).toString()
                        def siguiente = partes.join('.') + '-SNAPSHOT'
                        bat """
                            git config user.name "Jenkins Smartbody"
                            git config user.email "jenkins@smartbody.local"
                            git add backend/pom.xml
                            git commit -m "Release v${env.VERSION}"
                            git tag -a v${env.VERSION} -m "Smartbody v${env.VERSION}"
                            cd backend
                            ${MVN} versions:set -DnewVersion=${siguiente} -DgenerateBackupPoms=false
                            cd ..
                            git add backend/pom.xml
                            git commit -m "Siguiente version de desarrollo ${siguiente}"
                            @git push https://%GITHUB_ACTOR%:%GITHUB_TOKEN%@github.com/${GITHUB_OWNER}/${GITHUB_REPO}.git HEAD:main v${env.VERSION}
                        """
                    }
                }
                echo "Lanzamiento v${env.VERSION} publicado. Artefactos en ${PACKAGES_URL} listos para la Etapa 6."
            }
        }
    }

    post {
        success { echo "Pipeline OK: Smartbody v${env.VERSION}" }
        failure { echo 'Pipeline fallido: revisar la fase marcada en rojo.' }
    }
}

// Sube el jar del backend y el APK (como artefacto Maven) a GitHub Packages.
def publicarArtefactos() {
    withCredentials([usernamePassword(credentialsId: 'github-packages',
                                      usernameVariable: 'GITHUB_ACTOR', passwordVariable: 'GITHUB_TOKEN')]) {
        dir('backend') {
            bat "${MVN} -s ${MAVEN_SETTINGS} -DskipTests -Dgithub.owner=${GITHUB_OWNER} -Dgithub.repo=${GITHUB_REPO} deploy"
            bat "${MVN} -s ${MAVEN_SETTINGS} deploy:deploy-file " +
                "-DrepositoryId=github -Durl=${PACKAGES_URL} " +
                "-DgroupId=com.smartbody -DartifactId=smartbody-android -Dversion=${env.VERSION} -Dpackaging=apk " +
                "-Dfile=..\\android\\app\\build\\outputs\\apk\\release\\app-release-unsigned.apk"
        }
    }
}
