package com.smartbody.service.prediccion;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.InputStream;
import java.util.Map;

/**
 * Modelo Weka entrenado (REPTree, mineria/). Se carga UNA vez al arrancar y se reutiliza en cada
 * peticion: nunca se reentrena en una peticion. La cabecera guardada con el modelo fija el orden,
 * los tipos y los valores nominales de los atributos, asi que la entrada se construye igual que
 * en el entrenamiento.
 */
@Component
public class ModeloPrediccion {

    private static final Logger log = LoggerFactory.getLogger(ModeloPrediccion.class);

    public static final String NOMBRE = "REPTree";
    public static final String VERSION = "1.0";
    static final String RECURSO = "modelos/prediccion-reptree-1.0.model";

    private Classifier clasificador;
    private Instances cabecera;

    @PostConstruct
    void cargar() {
        try (InputStream entrada = new ClassPathResource(RECURSO).getInputStream()) {
            Object[] contenido = (Object[]) SerializationHelper.read(entrada);
            clasificador = (Classifier) contenido[0];
            cabecera = (Instances) contenido[1];
            cabecera.setClassIndex(cabecera.numAttributes() - 1);
            log.info("Modelo de prediccion {} v{} cargado ({} atributos).", NOMBRE, VERSION, cabecera.numAttributes() - 1);
        } catch (Exception e) {
            // El servicio sigue funcionando; el endpoint de prediccion responde 503 y se registra el motivo.
            clasificador = null;
            cabecera = null;
            log.error("No se pudo cargar el modelo de prediccion {}: {}", RECURSO, e.getMessage(), e);
        }
    }

    public boolean disponible() {
        return clasificador != null && cabecera != null;
    }

    /**
     * Estima las semanas con los valores indicados por nombre de atributo.
     *
     * @throws IllegalArgumentException si falta un atributo o un valor nominal no existe en el entrenamiento
     * @throws IllegalStateException    si el modelo no esta cargado o no puede predecir
     */
    public synchronized double predecir(Map<String, Object> valores) {
        if (!disponible()) {
            throw new IllegalStateException("El modelo no esta cargado.");
        }
        try {
            DenseInstance instancia = new DenseInstance(cabecera.numAttributes());
            instancia.setDataset(cabecera);
            for (int i = 0; i < cabecera.numAttributes(); i++) {
                if (i == cabecera.classIndex()) {
                    continue;
                }
                Attribute atributo = cabecera.attribute(i);
                Object valor = valores.get(atributo.name());
                if (valor == null) {
                    throw new IllegalArgumentException("Falta el atributo " + atributo.name());
                }
                if (atributo.isNumeric()) {
                    instancia.setValue(atributo, ((Number) valor).doubleValue());
                } else {
                    instancia.setValue(atributo, valor.toString());
                }
            }
            instancia.setMissing(cabecera.classIndex());
            return clasificador.classifyInstance(instancia);
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalStateException("No se pudo calcular la prediccion.", e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular la prediccion.", e);
        }
    }
}
