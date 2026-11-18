const pptxgen = require("pptxgenjs");

const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE"; // 13.33 x 7.5 in
pres.title = "Weka aplicado a SmartBody";
pres.author = "Nicoll Yuherly Florez; Gian Carlo Pulido Gutierrez";

// Paleta: azul marino dominante, cian como acento, dorado para alertas
const BG = "0B1F3A";
const PANEL = "13294B";
const PANEL2 = "1B3A66";
const CYAN = "2EC4F1";
const GOLD = "F5B841";
const WHITE = "FFFFFF";
const MUTED = "A9BCD6";
const GREEN = "4CD08D";
const RED = "FF8A80";
const H = "Cambria"; // titulos
const B = "Calibri"; // texto

const IMG = "C:/proyects/SmartBody/mineria/presentacion/img/";

function nueva() {
  const s = pres.addSlide();
  s.background = { color: BG };
  return s;
}

function titulo(s, texto) {
  s.addText(texto, {
    x: 0.6, y: 0.4, w: 12.1, h: 0.9, fontFace: H, fontSize: 32, bold: true,
    color: WHITE, margin: 0, valign: "middle", isTextBox: true,
  });
}

function pie(s) {
  s.addText("Weka aplicado a SmartBody · Tecnologías Emergentes", {
    x: 0.6, y: 6.95, w: 9, h: 0.3, fontFace: B, fontSize: 10, color: MUTED,
    margin: 0, isTextBox: true,
  });
}

function tarjeta(s, x, y, w, h, relleno = PANEL, borde = null) {
  s.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x, y, w, h, rectRadius: 0.12,
    fill: { color: relleno },
    line: borde ? { color: borde, width: 2 } : { color: relleno, width: 0.5 },
    shadow: { type: "outer", blur: 8, offset: 3, angle: 90, color: "000000", opacity: 0.35 },
  });
}

function numero(s, x, y, n, d = 0.55, color = CYAN) {
  s.addShape(pres.shapes.OVAL, { x, y, w: d, h: d, fill: { color } });
  s.addText(String(n), {
    x, y, w: d, h: d, align: "center", valign: "middle", fontFace: H,
    fontSize: 18, bold: true, color: BG, margin: 0, isTextBox: true,
  });
}

function lista(items, tamano = 16) {
  return items.map((t, i) => ({
    text: t,
    options: { bullet: true, breakLine: i < items.length - 1, paraSpaceAfter: 8, fontSize: tamano, color: WHITE },
  }));
}

// ---------------------------------------------------------------- 1. Portada
{
  const s = nueva();
  s.addShape(pres.shapes.OVAL, { x: 9.3, y: -1.4, w: 6.2, h: 6.2, fill: { color: PANEL2, transparency: 35 }, line: { type: "none" } });
  s.addShape(pres.shapes.OVAL, { x: -2.2, y: 4.9, w: 5.2, h: 5.2, fill: { color: CYAN, transparency: 88 }, line: { type: "none" } });
  s.addText("TECNOLOGÍAS EMERGENTES · ING. CARLOS PINEDA", {
    x: 0.9, y: 1.5, w: 10, h: 0.4, fontFace: B, fontSize: 14, bold: true, color: CYAN, charSpacing: 2, margin: 0, isTextBox: true,
  });
  s.addText("Weka aplicado a SmartBody", {
    x: 0.9, y: 2.0, w: 11.5, h: 1.3, fontFace: H, fontSize: 50, bold: true, color: WHITE, margin: 0, isTextBox: true,
  });
  s.addText("Estimación del tiempo hasta el peso objetivo con árboles de decisión y validación cruzada", {
    x: 0.9, y: 3.4, w: 10.5, h: 0.9, fontFace: B, fontSize: 22, color: MUTED, margin: 0, isTextBox: true,
  });
  s.addText([
    { text: "Nicoll Yuherly Florez", options: { breakLine: true, fontSize: 20, bold: true, color: WHITE } },
    { text: "Gian Carlo Pulido Gutierrez", options: { fontSize: 20, bold: true, color: WHITE } },
  ], { x: 0.9, y: 5.1, w: 7, h: 1.1, fontFace: B, margin: 0, paraSpaceAfter: 4, isTextBox: true });
  s.addText("Noviembre de 2026", {
    x: 0.9, y: 6.4, w: 6, h: 0.4, fontFace: B, fontSize: 14, color: MUTED, margin: 0, isTextBox: true,
  });
}

// ---------------------------------------------------------------- 2. Contenido
{
  const s = nueva();
  titulo(s, "Contenido");
  const items = [
    "Problema y objetivo", "¿Qué es Weka?", "Instalación del entorno", "Datos y preparación",
    "Modelado y resultados", "Integración en SmartBody", "Conclusiones",
  ];
  items.forEach((t, i) => {
    const col = i < 4 ? 0 : 1;
    const fila = col === 0 ? i : i - 4;
    const x = col === 0 ? 0.9 : 6.9;
    const y = 1.9 + fila * 1.2;
    numero(s, x, y, i + 1, 0.6);
    s.addText(t, { x: x + 0.9, y: y, w: 5, h: 0.6, fontFace: B, fontSize: 22, color: WHITE, valign: "middle", margin: 0, isTextBox: true });
  });
  pie(s);
}

// ---------------------------------------------------------------- 3. Problema
{
  const s = nueva();
  titulo(s, "¿Cuánto tardaría llegar al peso objetivo?");
  s.addText([
    { text: "SmartBody crea planes nutricionales y rutinas personalizadas, pero no indica cuánto tiempo necesitaría cada persona para alcanzar su meta.", options: { breakLine: true, paraSpaceAfter: 10 } },
    { text: "Queremos estimar ese tiempo en semanas con datos físicos, del plan, de la actividad y de la adherencia.", options: { breakLine: true, paraSpaceAfter: 10 } },
    { text: "El resultado es orientativo: no garantiza ninguna pérdida ni ganancia de peso.", options: {} },
  ], { x: 0.6, y: 1.6, w: 12.1, h: 2.4, fontFace: B, fontSize: 18, color: WHITE, margin: 0, valign: "top", isTextBox: true });

  const cajas = [
    { x: 0.6, t: "Entrada", d: "Edad, sexo, altura, peso actual y objetivo, plan, disponibilidad y adherencia." },
    { x: 4.8, t: "Modelo", d: "Weka · regresión supervisada entrenada con 90.000 registros limpios." },
    { x: 9.0, t: "Salida", d: "Semanas estimadas y fecha aproximada, siempre con aviso de orientación." },
  ];
  cajas.forEach((c, i) => {
    tarjeta(s, c.x, 4.4, 3.7, 2.1, PANEL, CYAN);
    s.addText(c.t, { x: c.x + 0.3, y: 4.6, w: 3.2, h: 0.5, fontFace: H, fontSize: 22, bold: true, color: CYAN, margin: 0, isTextBox: true });
    s.addText(c.d, { x: c.x + 0.3, y: 5.15, w: 3.2, h: 1.2, fontFace: B, fontSize: 15, color: WHITE, margin: 0, valign: "top", isTextBox: true });
    if (i < 2) {
      s.addShape(pres.shapes.RIGHT_ARROW, { x: c.x + 3.8, y: 5.25, w: 0.5, h: 0.4, fill: { color: GOLD }, line: { type: "none" } });
    }
  });
  pie(s);
}

// ---------------------------------------------------------------- 4. Que es Weka
{
  const s = nueva();
  titulo(s, "Weka: herramienta libre de aprendizaje");
  s.addText("Universidad de Waikato · Java · versión 3.8.6", {
    x: 0.6, y: 1.3, w: 12, h: 0.5, fontFace: B, fontSize: 16, color: MUTED, margin: 0, isTextBox: true,
  });
  const tarjetas = [
    { x: 0.6, t: "Explorer", d: "Interfaz gráfica con pestañas: Preprocess, Classify y Visualize. Sirve para explorar datos y mostrar resultados." },
    { x: 4.7, t: "API de Java", d: "La misma librería dentro de nuestro programa: entrena, evalúa y guarda el modelo que usa el backend." },
    { x: 8.8, t: "Formato ARFF", d: "Archivo de texto con los atributos y sus tipos. La última columna es la clase: lo que el modelo predice." },
  ];
  tarjetas.forEach((c, i) => {
    tarjeta(s, c.x, 2.1, 3.9, 3.9, PANEL);
    numero(s, c.x + 0.35, 2.45, i + 1, 0.6);
    s.addText(c.t, { x: c.x + 0.35, y: 3.3, w: 3.3, h: 0.6, fontFace: H, fontSize: 24, bold: true, color: WHITE, margin: 0, isTextBox: true });
    s.addText(c.d, { x: c.x + 0.35, y: 3.95, w: 3.25, h: 1.9, fontFace: B, fontSize: 15, color: MUTED, margin: 0, valign: "top", isTextBox: true });
  });
  s.addText("Usamos weka-stable 3.8.6 en todas las fases, para que cualquier persona pueda repetir los resultados.", {
    x: 0.6, y: 6.25, w: 12.1, h: 0.5, fontFace: B, fontSize: 15, italic: true, color: GOLD, margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 5. Instalacion
{
  const s = nueva();
  titulo(s, "Instalación: Java 17 y Weka 3.8.6");
  const pasos = [
    { t: "Verificar Java", d: "OpenJDK 17.0.19, comprobado con java -version." },
    { t: "Descargar Weka con Maven", d: "Un pom.xml mínimo declara weka-stable; Maven copia las 32 librerías a la carpeta lib." },
    { t: "Abrir la interfaz", d: "Se lanza el GUI Chooser y desde ahí se abre Explorer." },
  ];
  pasos.forEach((p, i) => {
    const y = 1.6 + i * 1.75;
    tarjeta(s, 0.6, y, 5.9, 1.5, PANEL);
    numero(s, 0.9, y + 0.45, i + 1, 0.6);
    s.addText(p.t, { x: 1.8, y: y + 0.2, w: 4.5, h: 0.45, fontFace: H, fontSize: 20, bold: true, color: WHITE, margin: 0, isTextBox: true });
    s.addText(p.d, { x: 1.8, y: y + 0.65, w: 4.5, h: 0.75, fontFace: B, fontSize: 14, color: MUTED, margin: 0, valign: "top", isTextBox: true });
  });

  tarjeta(s, 6.9, 1.6, 5.8, 2.7, "0A1A30", CYAN);
  s.addText([
    { text: "java -version", options: { breakLine: true, color: GREEN } },
    { text: "  openjdk 17.0.19", options: { breakLine: true, color: MUTED } },
    { text: " ", options: { breakLine: true, fontSize: 8 } },
    { text: 'mvn dependency:copy-dependencies', options: { breakLine: true, color: GREEN } },
    { text: '  -DoutputDirectory=lib', options: { breakLine: true, color: MUTED } },
    { text: " ", options: { breakLine: true, fontSize: 8 } },
    { text: 'java -Xmx2g -cp "lib\\*"', options: { breakLine: true, color: GREEN } },
    { text: '  weka.gui.GUIChooser', options: { color: GREEN } },
  ], { x: 7.2, y: 1.85, w: 5.3, h: 2.2, fontFace: "Consolas", fontSize: 13, margin: 0, valign: "top", isTextBox: true });

  s.addImage({ path: IMG + "guichooser.png", x: 6.9, y: 4.5, w: 5.8, h: 2.25, sizing: { type: "contain", w: 5.8, h: 2.25 } });
  s.addText("Weka GUI Chooser: desde aquí se abre Explorer", {
    x: 6.9, y: 6.6, w: 5.8, h: 0.3, fontFace: B, fontSize: 11, color: MUTED, align: "center", margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 6. Datos
{
  const s = nueva();
  titulo(s, "Datos: 100.000 registros sintéticos");
  const stats = [
    { n: "100.000", l: "filas generadas con semilla fija" },
    { n: "19", l: "atributos: 18 predictores y la clase" },
    { n: "90.000", l: "registros limpios para entrenar" },
  ];
  stats.forEach((st, i) => {
    tarjeta(s, 0.6, 1.6 + i * 1.6, 4.9, 1.35, PANEL);
    s.addText(st.n, { x: 0.9, y: 1.7 + i * 1.6, w: 4.4, h: 0.7, fontFace: H, fontSize: 36, bold: true, color: CYAN, margin: 0, isTextBox: true });
    s.addText(st.l, { x: 0.9, y: 2.4 + i * 1.6, w: 4.4, h: 0.4, fontFace: B, fontSize: 14, color: WHITE, margin: 0, isTextBox: true });
  });

  s.addText("Distribución de los registros", { x: 5.9, y: 1.6, w: 6.8, h: 0.4, fontFace: H, fontSize: 18, bold: true, color: WHITE, margin: 0, isTextBox: true });
  s.addChart(pres.charts.BAR, [{
    name: "Registros",
    labels: ["LIMPIO", "OUTLIER", "DUPLICADO", "FALTANTE", "INVÁLIDO"],
    values: [80000, 10000, 3500, 3500, 3000],
  }], {
    x: 5.9, y: 2.1, w: 6.8, h: 4.2, barDir: "col", chartColors: [CYAN],
    showValue: true, dataLabelPosition: "outEnd", dataLabelColor: WHITE, dataLabelFormatCode: "#,##0",
    dataLabelFontSize: 13, dataLabelFontBold: true, dataLabelFontFace: B,
    catAxisLabelColor: MUTED, valAxisLabelColor: MUTED, catAxisLabelFontFace: B, valAxisLabelFontFace: B,
    catAxisLabelFontSize: 12, valAxisLabelFontSize: 11, valAxisLabelFormatCode: "#,##0",
    valGridLine: { color: "23416B", size: 0.5 }, catGridLine: { style: "none" }, showLegend: false,
  });
  s.addText("Dataset sintético: no son datos clínicos reales.", {
    x: 0.6, y: 6.5, w: 12, h: 0.35, fontFace: B, fontSize: 13, italic: true, color: GOLD, margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 7. Preparacion
{
  const s = nueva();
  titulo(s, "Preparación: de 100.000 a 90.000 filas");
  const cajas = [
    { t: "100.000", d: "dataset original", f: PANEL, c: WHITE },
    { t: "−3.500", d: "duplicados", f: PANEL, c: RED },
    { t: "−3.500", d: "faltantes", f: PANEL, c: RED },
    { t: "−3.000", d: "inválidos", f: PANEL, c: RED },
    { t: "90.000", d: "limpios", f: CYAN, c: BG },
  ];
  cajas.forEach((c, i) => {
    const x = 0.6 + i * 2.5;
    tarjeta(s, x, 1.7, 2.2, 2.3, c.f, c.f === CYAN ? null : PANEL2);
    s.addText(c.t, { x, y: 2.0, w: 2.2, h: 0.9, fontFace: H, fontSize: 28, bold: true, color: c.c, align: "center", margin: 0, isTextBox: true });
    s.addText(c.d, { x, y: 3.0, w: 2.2, h: 0.5, fontFace: B, fontSize: 15, color: c.c, align: "center", margin: 0, isTextBox: true });
  });

  tarjeta(s, 0.6, 4.5, 12.1, 2.1, PANEL, GOLD);
  s.addText("Los 10.000 outliers se conservan", {
    x: 0.95, y: 4.75, w: 11.5, h: 0.5, fontFace: H, fontSize: 22, bold: true, color: GOLD, margin: 0, isTextBox: true,
  });
  s.addText("Cumplen todas las reglas de validación: son valores extremos plausibles (edades de 62 a 80 años, pesos de 120 a 160 kg). Eliminarlos sesgaría el modelo hacia personas típicas.", {
    x: 0.95, y: 5.35, w: 11.5, h: 1.0, fontFace: B, fontSize: 16, color: WHITE, margin: 0, valign: "top", isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 8. Explorer dataset
{
  const s = nueva();
  titulo(s, "Weka Explorer: explorar el dataset original");
  s.addText("Preprocess → Open file → smartbody_dataset_100000.arff", {
    x: 0.6, y: 1.25, w: 12, h: 0.4, fontFace: "Consolas", fontSize: 14, color: GREEN, margin: 0, isTextBox: true,
  });
  tarjeta(s, 0.6, 1.8, 6.1, 4.3, PANEL);
  s.addImage({ path: IMG + "01_resumen.png", x: 0.8, y: 1.95, w: 5.7, h: 3.7, sizing: { type: "contain", w: 5.7, h: 3.7 } });
  s.addText("Relación, 100.000 instancias y 19 atributos", {
    x: 0.6, y: 6.2, w: 6.1, h: 0.4, fontFace: B, fontSize: 13, color: MUTED, align: "center", margin: 0, isTextBox: true,
  });

  tarjeta(s, 6.9, 1.8, 5.8, 4.3, PANEL);
  s.addImage({ path: IMG + "03_edad.png", x: 7.1, y: 1.95, w: 5.4, h: 3.7, sizing: { type: "contain", w: 5.4, h: 3.7 } });
  s.addText("edad: mínimo −5 y máximo 250, valores imposibles", {
    x: 6.9, y: 6.2, w: 5.8, h: 0.4, fontFace: B, fontSize: 13, color: GOLD, align: "center", margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 9. Modelado
{
  const s = nueva();
  titulo(s, "Modelado con tres algoritmos");
  const algos = [
    { x: 0.6, t: "LinearRegression", r: "functions", d: "Línea base sencilla y rápida, para comparar.", c: "-R 1.0E-8" },
    { x: 4.7, t: "REPTree", r: "trees", d: "Árbol de decisión con poda. Es el modelo que elegimos.", c: "-M 2 -N 3 -S 42" },
    { x: 8.8, t: "RandomForest", r: "trees", d: "Conjunto de 100 árboles. Más precisión, pero más peso.", c: "-I 100 -K 0" },
  ];
  algos.forEach((a, i) => {
    tarjeta(s, a.x, 1.7, 3.9, 3.0, PANEL, i === 1 ? CYAN : null);
    s.addText(a.t, { x: a.x + 0.35, y: 1.95, w: 3.3, h: 0.5, fontFace: H, fontSize: 22, bold: true, color: WHITE, margin: 0, isTextBox: true });
    s.addText("Weka · " + a.r, { x: a.x + 0.35, y: 2.5, w: 3.3, h: 0.35, fontFace: B, fontSize: 13, color: CYAN, margin: 0, isTextBox: true });
    s.addText(a.d, { x: a.x + 0.35, y: 3.0, w: 3.3, h: 0.9, fontFace: B, fontSize: 15, color: MUTED, margin: 0, valign: "top", isTextBox: true });
    s.addText(a.c, { x: a.x + 0.35, y: 4.05, w: 3.3, h: 0.4, fontFace: "Consolas", fontSize: 13, color: GREEN, margin: 0, isTextBox: true });
  });

  tarjeta(s, 0.6, 5.0, 12.1, 1.6, PANEL2);
  s.addText([
    { text: "Validación cruzada de 10 particiones · misma semilla · clase semanas_estimadas", options: { breakLine: true, bold: true, color: WHITE, fontSize: 17, paraSpaceAfter: 6 } },
    { text: "En Explorer: Classify → Choose → trees → REPTree → Cross-validation (10 folds) → Start", options: { color: MUTED, fontSize: 14 } },
  ], { x: 0.9, y: 5.25, w: 11.5, h: 1.2, fontFace: B, margin: 0, valign: "middle", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 10. Resultados
{
  const s = nueva();
  titulo(s, "Resultados: REPTree equilibra error y tamaño");
  s.addText("Error absoluto medio (MAE) en semanas", {
    x: 0.6, y: 1.5, w: 6, h: 0.4, fontFace: H, fontSize: 16, bold: true, color: WHITE, margin: 0, isTextBox: true,
  });
  s.addChart(pres.charts.BAR, [{
    name: "MAE", labels: ["LinearRegression", "REPTree", "RandomForest"], values: [14.11, 4.72, 3.74],
  }], {
    x: 0.6, y: 2.0, w: 5.7, h: 4.6, barDir: "col", chartColors: [CYAN],
    showValue: true, dataLabelPosition: "outEnd", dataLabelColor: WHITE, dataLabelFormatCode: "0.00",
    dataLabelFontSize: 14, dataLabelFontBold: true, dataLabelFontFace: B,
    catAxisLabelColor: MUTED, valAxisLabelColor: MUTED, catAxisLabelFontFace: B, valAxisLabelFontFace: B,
    catAxisLabelFontSize: 12, valAxisLabelFontSize: 11,
    valGridLine: { color: "23416B", size: 0.5 }, catGridLine: { style: "none" }, showLegend: false,
  });

  const hdr = (t) => ({ text: t, options: { bold: true, color: WHITE, fill: { color: "1F4E79" }, align: "center" } });
  const cel = (t, destacada = false) => ({ text: t, options: { color: WHITE, fill: { color: destacada ? "1F4E79" : PANEL }, align: "center", bold: destacada } });
  const filas = [
    [hdr("Algoritmo"), hdr("MAE"), hdr("RMSE"), hdr("CC"), hdr("Tamaño")],
    [cel("LinearRegression"), cel("14,11"), cel("18,53"), cel("0,66"), cel("9 KB")],
    [cel("REPTree (elegido)", true), cel("4,72", true), cel("6,94", true), cel("0,96", true), cel("1,3 MB", true)],
    [cel("RandomForest"), cel("3,74"), cel("5,38"), cel("0,98"), cel("519 MB")],
  ];
  s.addTable(filas, {
    x: 6.7, y: 2.0, w: 6.0, colW: [2.1, 0.9, 0.95, 0.85, 1.2],
    fontFace: B, fontSize: 14, border: { type: "solid", pt: 0.75, color: "2A4A78" }, rowH: 0.55,
  });
  s.addText("MAE y RMSE en semanas · CC: coeficiente de correlación · semilla 1 del Explorer.", {
    x: 6.7, y: 4.75, w: 6.0, h: 0.6, fontFace: B, fontSize: 12, color: MUTED, margin: 0, valign: "top", isTextBox: true,
  });
  tarjeta(s, 6.7, 5.45, 6.0, 1.15, PANEL2, CYAN);
  s.addText("REPTree tiene 1 semana más de error que RandomForest, pero su modelo pesa 400 veces menos.", {
    x: 6.95, y: 5.6, w: 5.6, h: 0.85, fontFace: B, fontSize: 15, color: WHITE, margin: 0, valign: "middle", isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 11. Real vs predicho
{
  const s = nueva();
  titulo(s, "Real frente a predicho (REPTree)");
  tarjeta(s, 0.6, 1.6, 7.2, 5.1, PANEL);
  s.addImage({ path: IMG + "11_reptree_errores.png", x: 0.8, y: 1.75, w: 6.8, h: 4.8, sizing: { type: "contain", w: 6.8, h: 4.8 } });

  tarjeta(s, 8.2, 1.6, 4.5, 2.35, PANEL, CYAN);
  s.addText("3,1 semanas", { x: 8.5, y: 1.8, w: 4, h: 0.8, fontFace: H, fontSize: 34, bold: true, color: CYAN, margin: 0, isTextBox: true });
  s.addText("error mediano: la mitad de las predicciones se equivoca menos que esto", {
    x: 8.5, y: 2.6, w: 4, h: 0.9, fontFace: B, fontSize: 14, color: WHITE, margin: 0, valign: "top", isTextBox: true,
  });

  tarjeta(s, 8.2, 4.2, 4.5, 2.5, PANEL, GOLD);
  s.addText("11,1 semanas", { x: 8.5, y: 4.4, w: 4, h: 0.8, fontFace: H, fontSize: 34, bold: true, color: GOLD, margin: 0, isTextBox: true });
  s.addText("error del 90 % de las predicciones; los puntos en 104 son el tope de la fórmula", {
    x: 8.5, y: 5.2, w: 4, h: 1.2, fontFace: B, fontSize: 14, color: WHITE, margin: 0, valign: "top", isTextBox: true,
  });
  s.addText("Cuanto más cerca de la diagonal, mejor. Cifras de la validación con Java (semilla 42).", {
    x: 0.6, y: 6.75, w: 12, h: 0.3, fontFace: B, fontSize: 11, italic: true, color: MUTED, margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 12. Error por rango
{
  const s = nueva();
  titulo(s, "El error crece cuando el plazo es largo");
  s.addChart(pres.charts.BAR, [
    { name: "LinearRegression", labels: ["0–12", "12–24", "24–48", "48–104"], values: [14.46, 13.48, 9.41, 22.0] },
    { name: "REPTree", labels: ["0–12", "12–24", "24–48", "48–104"], values: [1.64, 2.87, 4.74, 7.53] },
    { name: "RandomForest", labels: ["0–12", "12–24", "24–48", "48–104"], values: [1.78, 2.37, 3.35, 6.3] },
  ], {
    x: 0.6, y: 1.5, w: 8.6, h: 5.2, barDir: "col", barGapWidthPct: 60,
    chartColors: ["6B7FA3", CYAN, GOLD],
    showValue: true, dataLabelPosition: "outEnd", dataLabelColor: WHITE, dataLabelFormatCode: "0.0", dataLabelFontSize: 10, dataLabelFontFace: B,
    catAxisLabelColor: MUTED, valAxisLabelColor: MUTED, catAxisLabelFontFace: B, valAxisLabelFontFace: B,
    catAxisLabelFontSize: 12, valAxisLabelFontSize: 11,
    valGridLine: { color: "23416B", size: 0.5 }, catGridLine: { style: "none" },
    showLegend: true, legendPos: "b", legendColor: WHITE, legendFontSize: 12, legendFontFace: B,
    showTitle: true, title: "MAE por rango de semanas reales", titleColor: WHITE, titleFontSize: 14, titleFontFace: B,
  });

  tarjeta(s, 9.6, 1.6, 3.1, 5.1, PANEL);
  s.addText([
    { text: "Plazos cortos: REPTree y RandomForest se equivocan unas 2 semanas.", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Plazos de 48 a 104 semanas: el error sube hasta 7,5 semanas en REPTree.", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Causa: la fórmula tiene un tope en 104 semanas y hay menos casos en ese rango.", options: { bullet: true } },
  ], { x: 9.85, y: 1.85, w: 2.7, h: 4.7, fontFace: B, fontSize: 14, color: WHITE, margin: 0, valign: "top", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 13. Seleccion
{
  const s = nueva();
  titulo(s, "¿Por qué REPTree y no RandomForest?");

  tarjeta(s, 0.6, 1.6, 5.9, 4.6, PANEL);
  s.addText("RandomForest", { x: 0.95, y: 1.85, w: 5.2, h: 0.55, fontFace: H, fontSize: 26, bold: true, color: WHITE, margin: 0, isTextBox: true });
  s.addText([
    { text: "MAE 3,74 semanas", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Modelo de 519 MB", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "3.230 s de validación cruzada", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Demasiado pesado para cargarlo en un servicio, en un equipo de 3,8 GB sin Docker", options: { bullet: true, color: RED } },
  ], { x: 0.95, y: 2.6, w: 5.2, h: 3.4, fontFace: B, fontSize: 17, color: WHITE, margin: 0, valign: "top", isTextBox: true });

  tarjeta(s, 6.8, 1.6, 5.9, 4.6, PANEL, CYAN);
  s.addText("REPTree (elegido)", { x: 7.15, y: 1.85, w: 5.2, h: 0.55, fontFace: H, fontSize: 26, bold: true, color: CYAN, margin: 0, isTextBox: true });
  s.addText([
    { text: "MAE 4,72 semanas", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Modelo de 1,3 MB", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "99 s de validación cruzada", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Misma estabilidad entre particiones (desviación del MAE de 0,033)", options: { bullet: true, breakLine: true, paraSpaceAfter: 10 } },
    { text: "Interpretable en sus primeras ramas", options: { bullet: true, color: GREEN } },
  ], { x: 7.15, y: 2.6, w: 5.2, h: 3.5, fontFace: B, fontSize: 17, color: WHITE, margin: 0, valign: "top", isTextBox: true });

  s.addText("Criterio: equilibrio entre error, tamaño, estabilidad e interpretabilidad. No siempre gana el modelo más preciso.", {
    x: 0.6, y: 6.4, w: 12.1, h: 0.45, fontFace: B, fontSize: 15, italic: true, color: GOLD, margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 14. Arbol
{
  const s = nueva();
  titulo(s, "Qué explica el árbol de REPTree");
  tarjeta(s, 0.6, 1.6, 7.0, 5.0, PANEL);
  s.addImage({ path: IMG + "12_reptree_arbol.png", x: 0.8, y: 1.75, w: 6.6, h: 4.7, sizing: { type: "contain", w: 6.6, h: 4.7 } });
  s.addText([
    { text: "Primer corte: diferencia_peso_kg, es decir, cuántos kilos hay que bajar o subir.", options: { bullet: true, breakLine: true, paraSpaceAfter: 14 } },
    { text: "Después: objetivo, balance_energetico_kcal y adherencia_nutricional_pct.", options: { bullet: true, breakLine: true, paraSpaceAfter: 14 } },
    { text: "El árbol completo tiene 10.008 nodos: se explican las ramas principales, no el árbol entero.", options: { bullet: true, color: GOLD } },
  ], { x: 8.0, y: 1.9, w: 4.7, h: 4.6, fontFace: B, fontSize: 17, color: WHITE, margin: 0, valign: "top", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 15. Integracion
{
  const s = nueva();
  titulo(s, "Integración en SmartBody");
  const cajas = [
    { x: 0.6, w: 2.8, t: "App Android", d: "Pantalla Tiempo hasta tu objetivo" },
    { x: 3.9, w: 2.8, t: "Nginx gateway", d: "/api/predicciones → progreso" },
    { x: 7.2, w: 2.9, t: "Servicio progreso", d: "Modelo REPTree cargado una vez" },
    { x: 10.6, w: 2.1, t: "MySQL", d: "Tabla predicciones" },
  ];
  cajas.forEach((c, i) => {
    tarjeta(s, c.x, 2.0, c.w, 2.1, i === 2 ? PANEL2 : PANEL, i === 2 ? CYAN : null);
    s.addText(c.t, { x: c.x + 0.2, y: 2.25, w: c.w - 0.4, h: 0.6, fontFace: H, fontSize: 19, bold: true, color: WHITE, align: "center", margin: 0, isTextBox: true });
    s.addText(c.d, { x: c.x + 0.2, y: 2.9, w: c.w - 0.4, h: 1.0, fontFace: B, fontSize: 14, color: MUTED, align: "center", margin: 0, valign: "top", isTextBox: true });
    if (i < cajas.length - 1) {
      s.addShape(pres.shapes.RIGHT_ARROW, { x: c.x + c.w + 0.1, y: 2.85, w: 0.3, h: 0.35, fill: { color: GOLD }, line: { type: "none" } });
    }
  });

  const notas = [
    { t: "Validación de entrada", d: "Rangos, coherencia del peso objetivo y calorías mínimas seguras." },
    { t: "Seguridad", d: "JWT en cada petición. Nadie puede ver predicciones de otro usuario." },
    { t: "Seguimiento", d: "Compara la estimación con las medidas reales registradas después." },
  ];
  notas.forEach((n, i) => {
    const x = 0.6 + i * 4.1;
    tarjeta(s, x, 4.6, 3.8, 1.9, PANEL);
    s.addText(n.t, { x: x + 0.3, y: 4.8, w: 3.2, h: 0.5, fontFace: H, fontSize: 18, bold: true, color: CYAN, margin: 0, isTextBox: true });
    s.addText(n.d, { x: x + 0.3, y: 5.3, w: 3.2, h: 1.1, fontFace: B, fontSize: 14, color: WHITE, margin: 0, valign: "top", isTextBox: true });
  });
  s.addText("En desarrollo, la app habla directamente con el monolito. En despliegue, el gateway Nginx reparte las peticiones al servicio progreso.", {
    x: 0.6, y: 6.6, w: 12.1, h: 0.3, fontFace: B, fontSize: 12, italic: true, color: GOLD, margin: 0, isTextBox: true,
  });
  pie(s);
}

// ---------------------------------------------------------------- 16. Endpoint y resultado
{
  const s = nueva();
  titulo(s, "Endpoints y un resultado real");
  const eps = [
    { m: "POST", c: GREEN, p: "/api/predicciones/tiempo-perdida-peso", d: "Calcula, guarda y responde 201" },
    { m: "GET", c: CYAN, p: "/api/predicciones", d: "Historial del usuario autenticado" },
    { m: "GET", c: CYAN, p: "/api/predicciones/{id}/seguimiento", d: "Compara la estimación con las medidas reales" },
  ];
  eps.forEach((e, i) => {
    const y = 1.6 + i * 1.7;
    tarjeta(s, 0.6, y, 6.6, 1.45, PANEL);
    s.addText(e.m, { x: 0.9, y: y + 0.45, w: 1.0, h: 0.5, fontFace: B, fontSize: 15, bold: true, color: e.c, margin: 0, isTextBox: true });
    s.addText(e.p, { x: 2.0, y: y + 0.2, w: 5, h: 0.5, fontFace: "Consolas", fontSize: 14, color: WHITE, margin: 0, isTextBox: true });
    s.addText(e.d, { x: 2.0, y: y + 0.75, w: 5, h: 0.45, fontFace: B, fontSize: 14, color: MUTED, margin: 0, isTextBox: true });
  });

  tarjeta(s, 7.6, 1.6, 5.1, 4.9, PANEL2, CYAN);
  s.addText("Ejemplo: de 90 a 80 kg", { x: 7.95, y: 1.85, w: 4.5, h: 0.5, fontFace: H, fontSize: 20, bold: true, color: WHITE, margin: 0, isTextBox: true });
  s.addText("22,6", { x: 7.95, y: 2.5, w: 4.5, h: 1.4, fontFace: H, fontSize: 80, bold: true, color: CYAN, margin: 0, isTextBox: true });
  s.addText("semanas estimadas", { x: 7.95, y: 3.9, w: 4.5, h: 0.45, fontFace: B, fontSize: 16, color: WHITE, margin: 0, isTextBox: true });
  s.addText("Hombre de 28 años · 170 cm · PERDER_PESO · 2.430 kcal al día", { x: 7.95, y: 4.5, w: 4.5, h: 0.7, fontFace: B, fontSize: 14, color: MUTED, margin: 0, valign: "top", isTextBox: true });
  s.addText("Estimación orientativa: no garantiza ninguna pérdida de peso.", { x: 7.95, y: 5.5, w: 4.5, h: 0.8, fontFace: B, fontSize: 14, italic: true, color: GOLD, margin: 0, valign: "top", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 17. Pruebas
{
  const s = nueva();
  titulo(s, "Pruebas realizadas");
  const stats = [
    { n: "103", l: "pruebas unitarias del monolito (18 nuevas de predicción)" },
    { n: "6", l: "pruebas unitarias de la lógica de la app Android" },
    { n: "4", l: "respuestas HTTP verificadas con JWT real: 201, 400, 401 y 404" },
  ];
  stats.forEach((st, i) => {
    const x = 0.6 + i * 4.1;
    tarjeta(s, x, 1.7, 3.8, 3.2, PANEL, i === 0 ? CYAN : null);
    s.addText(st.n, { x: x + 0.3, y: 1.95, w: 3.2, h: 1.2, fontFace: H, fontSize: 64, bold: true, color: CYAN, margin: 0, isTextBox: true });
    s.addText(st.l, { x: x + 0.3, y: 3.25, w: 3.2, h: 1.4, fontFace: B, fontSize: 15, color: WHITE, margin: 0, valign: "top", isTextBox: true });
  });
  tarjeta(s, 0.6, 5.3, 12.1, 1.2, PANEL2);
  s.addText([
    { text: "Android: la app compila (BUILD SUCCESSFUL).", options: { breakLine: true, color: WHITE, paraSpaceAfter: 4 } },
    { text: "Pendiente: probar la pantalla en un dispositivo real.", options: { color: GOLD } },
  ], { x: 0.9, y: 5.45, w: 11.5, h: 0.9, fontFace: B, fontSize: 16, margin: 0, valign: "middle", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 18. Limitaciones y futuro
{
  const s = nueva();
  titulo(s, "Limitaciones y trabajo futuro");
  tarjeta(s, 0.6, 1.6, 5.9, 5.0, PANEL, GOLD);
  s.addText("Limitaciones", { x: 0.95, y: 1.85, w: 5.2, h: 0.5, fontFace: H, fontSize: 24, bold: true, color: GOLD, margin: 0, isTextBox: true });
  s.addText(lista([
    "Datos sintéticos: validan el flujo, no la precisión con personas reales.",
    "La fórmula tiene un tope de 104 semanas.",
    "La adherencia la declara el usuario; la app todavía no la registra.",
    "El árbol de 10.008 nodos solo se explica por sus ramas principales.",
  ], 16), { x: 0.95, y: 2.5, w: 5.2, h: 3.9, fontFace: B, margin: 0, valign: "top", isTextBox: true });

  tarjeta(s, 6.8, 1.6, 5.9, 5.0, PANEL, CYAN);
  s.addText("Siguientes pasos", { x: 7.15, y: 1.85, w: 5.2, h: 0.5, fontFace: H, fontSize: 24, bold: true, color: CYAN, margin: 0, isTextBox: true });
  s.addText(lista([
    "Recoger datos longitudinales reales, con consentimiento y anonimizados.",
    "Registrar en la app la adherencia al plan y a las rutinas.",
    "Probar la pantalla Android en un dispositivo.",
    "Desplegar el gateway con Docker o Kubernetes.",
    "Reentrenar el modelo solo con validación controlada.",
  ], 16), { x: 7.15, y: 2.5, w: 5.2, h: 3.9, fontFace: B, margin: 0, valign: "top", isTextBox: true });
  pie(s);
}

// ---------------------------------------------------------------- 19. Conclusiones
{
  const s = nueva();
  titulo(s, "Conclusiones");
  const pts = [
    "Weka permitió recorrer las fases de CRISP-DM con herramientas libres y reproducibles.",
    "REPTree dio el mejor equilibrio entre error (4,7 semanas), tamaño (1,3 MB) y estabilidad.",
    "El modelo se integró en SmartBody sin reentrenarlo en cada petición, con validaciones y aislamiento entre usuarios.",
    "Es una herramienta orientativa: no es una promesa de resultados.",
  ];
  pts.forEach((t, i) => {
    const y = 1.6 + i * 1.25;
    tarjeta(s, 0.6, y, 12.1, 1.05, PANEL);
    numero(s, 0.95, y + 0.25, i + 1, 0.55);
    s.addText(t, { x: 1.8, y: y + 0.1, w: 10.7, h: 0.85, fontFace: B, fontSize: 18, color: WHITE, margin: 0, valign: "middle", isTextBox: true });
  });
  pie(s);
}

// ---------------------------------------------------------------- 20. Cierre
{
  const s = nueva();
  s.addShape(pres.shapes.OVAL, { x: 8.9, y: 3.6, w: 6, h: 6, fill: { color: CYAN, transparency: 90 }, line: { type: "none" } });
  s.addShape(pres.shapes.OVAL, { x: -2.5, y: -2.2, w: 5.5, h: 5.5, fill: { color: PANEL2, transparency: 30 }, line: { type: "none" } });
  s.addText("Gracias", { x: 0.9, y: 1.9, w: 11, h: 1.4, fontFace: H, fontSize: 60, bold: true, color: WHITE, margin: 0, isTextBox: true });
  s.addText([
    { text: "Nicoll Yuherly Florez", options: { breakLine: true, fontSize: 22, bold: true, color: WHITE, paraSpaceAfter: 6 } },
    { text: "Gian Carlo Pulido Gutierrez", options: { fontSize: 22, bold: true, color: WHITE } },
  ], { x: 0.9, y: 3.6, w: 8, h: 1.4, fontFace: B, margin: 0, isTextBox: true });
  s.addText("¿Preguntas?", { x: 0.9, y: 5.4, w: 8, h: 0.6, fontFace: H, fontSize: 26, italic: true, color: CYAN, margin: 0, isTextBox: true });
}

pres.writeFile({ fileName: "C:/proyects/SmartBody/mineria/presentacion/Weka_SmartBody_presentacion.pptx" })
  .then((f) => console.log("Escrito:", f))
  .catch((e) => { console.error(e); process.exit(1); });
