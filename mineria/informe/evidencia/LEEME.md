# Evidencia de Weka (mineria/informe/evidencia)

Generada con `mineria/herramientas/EvidenciaWeka.java`, con la API de Weka 3.8.6 y los valores
por defecto del Explorer (validacion cruzada de 10 particiones, semilla 1).

Estos archivos contienen la **misma informacion** que se ve en Weka Explorer, pero no son capturas
de la ventana. Las capturas de la ventana de Explorer se hacen aparte (ver la guia del paso 4).

## Fases 2 y 3: Preprocess

| Archivo | Equivale a | Fase |
|---|---|---|
| `01_fase2_resumen_original.txt` | Preprocess: resumen de atributos del dataset original | 2 |
| `02_fase3_resumen_limpio.txt` | Preprocess: resumen de atributos del dataset limpio | 3 |
| `03_fase2_visualize_all_original.png` | Preprocess → Visualize All (original) | 2 |
| `04_fase3_visualize_all_limpio.png` | Preprocess → Visualize All (limpio) | 3 |

## Fases 4 y 5: Classify (validacion cruzada de 10 particiones)

| Algoritmo | Salida del Explorer | Real frente a predicho | Distribucion del error | Metricas |
|---|---|---|---|---|
| REPTree (elegido) | `salida_REPTree.txt` | `05_REPTree_real_vs_predicho.png` | `06_REPTree_residuos.png` | `metricas_REPTree.csv` |
| LinearRegression | `salida_LinearRegression.txt` | `05_LinearRegression_real_vs_predicho.png` | `06_LinearRegression_residuos.png` | `metricas_LinearRegression.csv` |
| RandomForest | `salida_RandomForest.txt` | `05_RandomForest_real_vs_predicho.png` | `06_RandomForest_residuos.png` | `metricas_RandomForest.csv` |

Estructura de cada `salida_*.txt`: *Run information*, *Classifier model (full training set)*
y *Summary* (correlacion, MAE, RMSE, errores relativos y numero de instancias), igual que la pestana
Classify del Explorer.

Arbol de REPTree en formato DOT: `arbol_REPTree.dot` (10.008 nodos; se puede visualizar con Graphviz
o con Weka, en Result list → Visualize tree).

## Advertencias para la exposicion

- Las cifras de estas salidas son las mismas que las del informe de la API de Java (`resultados_modelos.txt`)
  con diferencias en el tercer decimal, porque el Explorer usa la semilla 1 y la API la semilla 42.
- `semanas_estimadas` tiene un pico en 104 semanas: es el tope de la formula del generador
  (ver `mineria/README.md`, seccion 5).
- `adherencia_nutricional_pct` y `adherencia_ejercicio_pct` tienen un pico en 100 % por el recorte de la
  distribucion normal.
- El dataset es sintetico: las correlaciones altas validan el flujo, no la precision con personas reales.

## Lo que falta por hacer en Weka Explorer

Las capturas de la ventana de Weka (pasos 1 a 6 de la guia) las hace la persona que presenta,
porque requieren la interfaz grafica. Los nombres de archivo estan en la tabla del paso 6 de la guia.

## Metricas finales (validacion cruzada de 10 particiones, valores por defecto del Explorer)

| Algoritmo | MAE (sem) | RMSE (sem) | CC | R2 | Archivo |
|---|---:|---:|---:|---:|---|
| LinearRegression | 14,1133 | 18,5279 | 0,6582 | 0,4333 | `metricas_LinearRegression.csv` |
| REPTree (elegido) | 4,7152 | 6,9378 | 0,9596 | 0,9205 | `metricas_REPTree.csv` |
| RandomForest | 3,7375 | 5,3812 | 0,9775 | 0,9522 | `metricas_RandomForest.csv` |

Estas cifras difieren en el tercer decimal de las de la API de Java (`informe/resultados_modelos.txt`)
porque usan semillas distintas (1 frente a 42).
