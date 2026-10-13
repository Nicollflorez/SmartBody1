# Arranca Jenkins en esta maquina sin Docker (equipo con 4 GB de RAM).
#
#   powershell -ExecutionPolicy Bypass -File devops\jenkins\run-jenkins.ps1
#
# - Descarga la ultima version LTS de jenkins.war la primera vez.
# - JENKINS_HOME queda en devops\jenkins\home (ignorado por git).
# - Puerto 8090 para no chocar con el backend (8080).
# - Exporta ANDROID_HOME para que el pipeline compile la app sin local.properties.
#
# Requisitos: JDK 17 en el PATH y el SDK de Android instalado por Android Studio.

$ErrorActionPreference = "Stop"
$carpeta = $PSScriptRoot
$war = Join-Path $carpeta "jenkins.war"
$home_jenkins = Join-Path $carpeta "home"

if (-not (Test-Path $war)) {
    Write-Host "Descargando Jenkins LTS..."
    Invoke-WebRequest -Uri "https://get.jenkins.io/war-stable/latest/jenkins.war" -OutFile $war
}
if (-not (Test-Path $home_jenkins)) { New-Item -ItemType Directory $home_jenkins | Out-Null }

$env:JENKINS_HOME = $home_jenkins
if (-not $env:ANDROID_HOME) {
    $sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (Test-Path $sdk) { $env:ANDROID_HOME = $sdk }
}
Write-Host "JENKINS_HOME = $env:JENKINS_HOME"
Write-Host "ANDROID_HOME = $env:ANDROID_HOME"
Write-Host "Jenkins en http://localhost:8090  (Ctrl+C para detener)"

# Heap limitado: Jenkins solo orquesta; Maven y Gradle corren en procesos aparte.
& java -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF-8 -jar $war --httpPort=8090
