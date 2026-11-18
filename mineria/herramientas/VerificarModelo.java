import weka.classifiers.Classifier;
import weka.core.*;
import java.io.*;
import java.nio.file.*;

/** Verifica que el modelo serializado carga y predice con Weka igual que en el entrenamiento. */
public class VerificarModelo {
    public static void main(String[] args) throws Exception {
        Object[] par = (Object[]) SerializationHelper.read("modelos/REPTree.model");
        Classifier clf = (Classifier) par[0];
        Instances cab = (Instances) par[1];
        cab.setClassIndex(cab.numAttributes() - 1);
        System.out.println("Atributos en cabecera: " + (cab.numAttributes() - 1) + " + clase " + cab.classAttribute().name());

        // 1) Registro real del dataset limpio: prediccion frente a valor real
        Instances limpio;
        try (BufferedReader r = Files.newBufferedReader(Path.of("datos/smartbody_limpio.arff"))) { limpio = new Instances(r); }
        limpio.setClassIndex(limpio.numAttributes() - 1);
        double errSum = 0; int n = 2000;
        for (int i = 0; i < n; i++) {
            Instance in = limpio.instance(i);
            errSum += Math.abs(clf.classifyInstance(in) - in.classValue());
        }
        System.out.printf("Primeros %d registros limpios (dentro del entrenamiento): MAE = %.2f%n", n, errSum / n);
        Instance real = limpio.instance(0);
        System.out.printf("Ejemplo: real=%.1f  predicho=%.1f%n", real.classValue(), clf.classifyInstance(real));

        // 2) Caso de ejemplo usado en la API (hombre 28 anos, 90->80 kg, PERDER_PESO)
        Instance ej = new DenseInstance(cab.numAttributes());
        ej.setDataset(cab);
        String[][] vals = {
            {"edad","28"}, {"sexo","MASCULINO"}, {"altura_cm","170"}, {"peso_inicial_kg","90"},
            {"peso_objetivo_kg","80"}, {"diferencia_peso_kg","10"}, {"imc_inicial","31.14"},
            {"objetivo","PERDER_PESO"}, {"nivel_actividad","MODERADO"}, {"nivel_experiencia","INTERMEDIO"},
            {"entrenamientos_semana","4"}, {"minutos_sesion","45"}, {"tipo_actividad","MIXTO"},
            {"consumo_energetico_kcal","2833"}, {"balance_energetico_kcal","403"}, {"restricciones_total","0"},
            {"adherencia_nutricional_pct","90"}, {"adherencia_ejercicio_pct","85"}};
        for (String[] kv : vals) {
            Attribute a = cab.attribute(kv[0]);
            if (a.isNumeric()) ej.setValue(a, Double.parseDouble(kv[1])); else ej.setValue(a, kv[1]);
        }
        ej.setMissing(cab.classIndex());
        System.out.printf("Caso de ejemplo (API devolvio 22.6): Weka predice %.2f semanas%n", clf.classifyInstance(ej));
    }
}
