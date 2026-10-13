package com.smartbody.service.reglas;

import com.smartbody.dto.PlanNutricionalResponse;
import com.smartbody.dto.RestriccionResponse;
import com.smartbody.dto.RutinaResponse;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Asistente del chatbot basado en reglas (MVP).
 * Detecta la intencion de la pregunta por palabras clave y responde con los datos reales del usuario:
 * rutina, plan nutricional, recetas, hidratacion y progreso. No da diagnosticos medicos.
 *
 * <p>Tono: llama al usuario por su nombre, es muy amable y cercano, y cada respuesta lo motiva a
 * seguir entrenando. La unica excepcion es el dolor o las lesiones: ahi el mensaje es igual de
 * carinoso, pero prioriza la salud y nunca invita a entrenar.</p>
 */
@Component
public class AsistentePorReglas implements AgenteConversacional {

    /** El orden define la prioridad cuando dos intenciones tienen el mismo puntaje. */
    enum Intencion {
        DOLOR, CAMBIAR, RECETA, AGUA, MACROS, MOTIVACION, DESCANSO, NUTRICION, RUTINA, PROGRESO, APP, AGRADECIMIENTO, SALUDO
    }

    private static final Map<Intencion, List<String>> PALABRAS_CLAVE = new EnumMap<>(Map.ofEntries(
            Map.entry(Intencion.DOLOR, List.of("dolor", "duele", "lesion", "lastim", "mareo", "marea", "enfermo",
                    "medico", "fractura", "desmay", "hinchad")),
            Map.entry(Intencion.CAMBIAR, List.of("cambiar", "cambio", "sustitu", "reemplaz", "no me gusta", "otro ejercicio",
                    "otra comida", "otro alimento")),
            Map.entry(Intencion.RECETA, List.of("receta", "preparo", "preparar", "preparacion", "cocinar", "cocino")),
            Map.entry(Intencion.AGUA, List.of("agua", "hidrat", "beber", "bebo", "bebida", "litro", "sed", "liquido")),
            Map.entry(Intencion.MACROS, List.of("proteina", "carbohidrato", "grasa", "macro")),
            Map.entry(Intencion.NUTRICION, List.of("caloria", "kcal", "comer", "como hoy", "comida", "dieta", "desayun",
                    "almuerz", "cena", "ceno", "snack", "merienda", "alimento", "nutric")),
            Map.entry(Intencion.RUTINA, List.of("rutina", "entren", "ejercicio", "gym", "gimnasio", "serie", "repeticion",
                    "musculo", "dia ")),
            Map.entry(Intencion.PROGRESO, List.of("imc", "peso", "progreso", "medida", "avance", "adelgaz", "kilo", "bajar",
                    "engord")),
            Map.entry(Intencion.MOTIVACION, List.of("motiv", "pereza", "cansad", "animo", "desanim", "no puedo", "ganas",
                    "constancia", "rendir")),
            Map.entry(Intencion.DESCANSO, List.of("dormir", "duermo", "sueno", "descans", "recuper", "agujeta")),
            Map.entry(Intencion.APP, List.of("como uso", "como funciona", "aplicacion", "app", "perfil", "foto",
                    "contrasena", "cerrar sesion", "editar", "ayuda", "que puedes", "que sabes")),
            Map.entry(Intencion.AGRADECIMIENTO, List.of("gracias", "genial", "perfecto", "excelente", "chao", "adios")),
            Map.entry(Intencion.SALUDO, List.of("hola", "buenas", "buenos dias", "hey", "que tal"))
    ));

    /** Frases de cierre que invitan a seguir entrenando. "{nombre}" se reemplaza por el nombre del usuario. */
    static final List<String> CIERRES_MOTIVADORES = List.of(
            "¡Cada día cuenta y lo estás haciendo increíble, {nombre}! 💪",
            "¡Tú puedes, {nombre}! Un paso a la vez te lleva muy lejos. 🚀",
            "¡Sigue así, {nombre}! Tu constancia es tu superpoder. 🌟",
            "Recuerda, {nombre}: la constancia le gana al talento. ¡Vamos por más! 🔥",
            "¡Tu mejor versión se construye hoy, {nombre}! Estoy aquí para acompañarte. 💚",
            "¡Qué orgullo ver tu compromiso con tu bienestar, {nombre}! A darlo todo. 🏆"
    );

    private static final Pattern DIA = Pattern.compile("\\bdia\\s*(\\d)");
    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    static final List<String> SUGERENCIAS_INICIALES = List.of(
            "¿Qué ejercicios tengo en el día 1?", "¿Cuánta agua debo tomar?", "¿Qué como en el almuerzo?");

    private final Random azar;

    public AsistentePorReglas() {
        this(new Random());
    }

    AsistentePorReglas(Random azar) {
        this.azar = azar;
    }

    @Override
    public RespuestaChat responder(String mensaje, ContextoChat contexto) {
        String texto = normalizar(mensaje);
        Intencion intencion = detectarIntencion(texto);
        if (intencion == null) {
            return noEntendido(contexto);
        }
        return switch (intencion) {
            case DOLOR -> dolor(contexto);
            case CAMBIAR -> cambiar(contexto);
            case RECETA -> receta(texto, contexto);
            case AGUA -> agua(contexto);
            case MACROS -> macros(contexto);
            case NUTRICION -> nutricion(texto, contexto);
            case RUTINA -> rutina(texto, contexto);
            case PROGRESO -> progreso(contexto);
            case MOTIVACION -> motivacion(contexto);
            case DESCANSO -> descanso(contexto);
            case APP -> ayudaApp(contexto);
            case AGRADECIMIENTO -> agradecimiento(contexto);
            case SALUDO -> saludo(contexto);
        };
    }

    // ------------------------------------------------------------------
    // Deteccion de intencion
    // ------------------------------------------------------------------

    static Intencion detectarIntencion(String texto) {
        Intencion mejor = null;
        int mejorPuntaje = 0;
        for (Intencion intencion : Intencion.values()) {
            int puntaje = (int) PALABRAS_CLAVE.get(intencion).stream().filter(p -> contiene(texto, p)).count();
            // Ante cualquier sintoma de dolor o lesion, la seguridad tiene prioridad.
            if (intencion == Intencion.DOLOR && puntaje > 0) {
                return Intencion.DOLOR;
            }
            if (puntaje > mejorPuntaje) {
                mejor = intencion;
                mejorPuntaje = puntaje;
            }
        }
        return mejor;
    }

    /** La palabra clave debe aparecer al inicio de una palabra ("entren" encuentra "entrenamiento"). */
    private static boolean contiene(String texto, String palabraClave) {
        return Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(palabraClave)).matcher(texto).find();
    }

    /** Minusculas y sin tildes: "¿Cuánta agua?" -> "¿cuanta agua?". */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------
    // Respuestas
    // ------------------------------------------------------------------

    private RespuestaChat saludo(ContextoChat contexto) {
        String respuesta = "¡Hola, " + nombre(contexto) + "! 👋 ¡Qué alegría tenerte por aquí!\n\n"
                + "Soy tu asistente de Smartbody y estoy para acompañarte en cada paso hacia tu mejor versión. "
                + "Puedo ayudarte con:\n"
                + "• Los ejercicios de tu rutina\n"
                + "• Tu plan de comidas y sus recetas\n"
                + "• Cuánta agua tomar\n"
                + "• Tu IMC y tu progreso\n"
                + "• Cómo usar la aplicación\n\n";
        respuesta += contexto.perfilCompleto()
                ? "¿Por dónde quieres empezar hoy? ¡Vamos con toda! 💪"
                : "Para darte consejos hechos a tu medida, primero completa tu perfil. ¡Te va a encantar el resultado! 💚";
        return new RespuestaChat(respuesta, SUGERENCIAS_INICIALES);
    }

    private RespuestaChat dolor(ContextoChat contexto) {
        StringBuilder respuesta = new StringBuilder(nombre(contexto) + ", lo primero siempre es tu bienestar 💚\n\n"
                + "Si sientes dolor, mareo o cualquier molestia fuera de lo normal, detén el ejercicio de inmediato "
                + "y consulta a un profesional de la salud. Smartbody no puede dar diagnósticos y no reemplaza "
                + "una valoración médica.");

        List<RestriccionResponse> salud = contexto.perfil() == null || contexto.perfil().restricciones() == null ? List.of()
                : contexto.perfil().restricciones().stream().filter(r -> r.tipo().name().equals("SALUD")).toList();
        if (!salud.isEmpty()) {
            respuesta.append("\n\nTienes registrado: ")
                    .append(salud.stream().map(RestriccionResponse::nombre).collect(Collectors.joining(", ")))
                    .append(". Tu rutina ya evita los ejercicios contraindicados para eso.");
        }
        respuesta.append("\n\nSi tienes una lesión nueva, regístrala en Perfil → Editar perfil → Restricciones "
                        + "y genera un plan nuevo para que tu rutina la tenga en cuenta.\n\n")
                .append("Descansa y cuídate mucho, ").append(nombre(contexto))
                .append(". Recuperarte bien también es avanzar, y aquí estaré cuando quieras volver. 🤗");
        return new RespuestaChat(respuesta.toString(), List.of("¿Cuánto debo descansar?", "¿Cómo cambio un ejercicio?"));
    }

    private RespuestaChat cambiar(ContextoChat contexto) {
        String respuesta = "¡Por supuesto, " + nombre(contexto) + "! 🔄 Tu plan tiene que gustarte para que lo disfrutes al máximo.\n\n"
                + "• Un ejercicio: ve a Rutina, toca el ejercicio y elige «Cambiar este ejercicio».\n"
                + "• Un alimento: ve a Nutrición y toca «Cambiar» junto al alimento.\n\n"
                + "Solo verás opciones compatibles con tu nivel y tus restricciones. Si cambias un alimento, "
                + "la cantidad se ajusta para mantener las calorías y la receta se actualiza sola.";
        return respuestaMotivadora(respuesta, contexto, List.of("¿Qué ejercicios tengo en el día 1?", "¿Qué como en el almuerzo?"));
    }

    private RespuestaChat agua(ContextoChat contexto) {
        Hidratacion hidratacion = contexto.hidratacion();
        if (hidratacion == null) {
            return perfilIncompleto(contexto);
        }
        String respuesta = "¡Excelente pregunta, " + nombre(contexto) + "! 💧 Mantenerte hidratado es clave para rendir al máximo.\n\n"
                + "Tu meta de hidratación:\n"
                + "• Días de entrenamiento: " + numero(hidratacion.litrosDiaEntrenamiento()) + " litros (unos "
                + hidratacion.vasosDiaEntrenamiento() + " vasos)\n"
                + "• Días de descanso: " + numero(hidratacion.litrosDiaDescanso()) + " litros\n\n"
                + "Consejos:\n" + vinetas(hidratacion.recomendaciones().stream().limit(4).toList()) + "\n\n"
                + "Evita:\n" + vinetas(hidratacion.evitar());
        return respuestaMotivadora(respuesta, contexto, List.of("¿Cuántas calorías debo comer?", "¿Qué ejercicios tengo en el día 1?"));
    }

    private RespuestaChat nutricion(String texto, ContextoChat contexto) {
        PlanNutricionalResponse plan = contexto.plan();
        if (plan == null) {
            return sinPlan(contexto);
        }
        Optional<PlanNutricionalResponse.Comida> comida = comidaMencionada(texto, plan);
        if (comida.isPresent()) {
            PlanNutricionalResponse.Comida c = comida.get();
            String alimentos = c.alimentos().stream()
                    .map(a -> "• " + (a.icono() == null ? "" : a.icono() + " ") + numero(a.cantidad()) + " g de "
                            + a.nombre().toLowerCase(ESPANOL) + " (" + a.calorias() + " kcal)")
                    .collect(Collectors.joining("\n"));
            String respuesta = "¡Con mucho gusto, " + nombre(contexto) + "! 🍽️ Esto es lo que te preparé para tu "
                    + c.nombre().toLowerCase(ESPANOL) + " (" + c.calorias() + " kcal):\n" + alimentos;
            if (c.receta() != null) {
                respuesta += "\n\nReceta sugerida: " + c.receta().titulo() + ". ¡Se ve deliciosa!";
            }
            return respuestaMotivadora(respuesta, contexto, List.of("Receta " + deLa(c),
                    "¿Cómo cambio un alimento?", "¿Cuánta agua debo tomar?"));
        }

        String comidas = plan.comidas().stream()
                .map(c -> "• " + c.nombre() + " (" + c.calorias() + " kcal): "
                        + c.alimentos().stream().map(a -> a.nombre().toLowerCase(ESPANOL)).collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));
        String meta = plan.caloriasObjetivo() == null ? "" : " (tu meta estimada es " + plan.caloriasObjetivo() + " kcal)";
        String respuesta = "¡Claro que sí, " + nombre(contexto) + "! 🍽️ Comer bien es darle a tu cuerpo el combustible que se merece.\n\n"
                + "Tu plan de hoy suma " + plan.caloriasTotales() + " kcal" + meta + ":\n" + comidas
                + "\n\nPregúntame por una comida, por ejemplo «¿qué como en la cena?».";
        return respuestaMotivadora(respuesta, contexto,
                List.of("¿Qué como en el desayuno?", "Receta del almuerzo", "¿Cuánta proteína tiene mi plan?"));
    }

    private RespuestaChat receta(String texto, ContextoChat contexto) {
        PlanNutricionalResponse plan = contexto.plan();
        if (plan == null) {
            return sinPlan(contexto);
        }
        Optional<PlanNutricionalResponse.Comida> comida = comidaMencionada(texto, plan);
        if (comida.isEmpty() || comida.get().receta() == null) {
            String recetas = plan.comidas().stream()
                    .filter(c -> c.receta() != null)
                    .map(c -> "• " + c.nombre() + ": " + c.receta().titulo())
                    .collect(Collectors.joining("\n"));
            return new RespuestaChat("¡Me encanta que quieras cocinar, " + nombre(contexto) + "! 👩‍🍳 "
                    + "Estas son las recetas de tu plan:\n" + recetas + "\n\n¿De cuál quieres que te cuente la preparación?",
                    plan.comidas().stream().map(c -> "Receta " + deLa(c)).limit(3).toList());
        }

        Receta receta = comida.get().receta();
        String pasos = IntStream.range(0, receta.pasos().size())
                .mapToObj(i -> (i + 1) + ". " + receta.pasos().get(i))
                .collect(Collectors.joining("\n"));
        String respuesta = "¡Manos a la obra, " + nombre(contexto) + "! 👩‍🍳 Vamos a preparar: " + receta.titulo() + "\n\n"
                + "Ingredientes:\n" + vinetas(receta.ingredientes()) + "\n\n"
                + "Preparación:\n" + pasos
                + (receta.bebida() == null ? "" : "\n\nBebida: " + receta.bebida());
        return respuestaMotivadora(respuesta, contexto, List.of("¿Cómo cambio un alimento?", "¿Cuánta agua debo tomar?"));
    }

    private RespuestaChat macros(ContextoChat contexto) {
        PlanNutricionalResponse plan = contexto.plan();
        if (plan == null) {
            return sinPlan(contexto);
        }
        String consejo = switch (codigoObjetivo(contexto)) {
            case "GANAR_MASA" -> "Para ganar masa muscular reparte la proteína en todas tus comidas, especialmente después de entrenar. ¡Tus músculos te lo van a agradecer!";
            case "PERDER_PESO" -> "Para perder peso, la proteína te ayuda a sentir saciedad y a conservar músculo. ¡Vas por excelente camino!";
            default -> "Mantén esta distribución equilibrada y prioriza alimentos naturales. ¡Tu cuerpo lo va a notar!";
        };
        String respuesta = "¡Buena pregunta, " + nombre(contexto) + "! 📊 Tu plan diario aporta aproximadamente:\n"
                + "• Proteína: " + numero(plan.proteinaG()) + " g\n"
                + "• Carbohidratos: " + numero(plan.carbohidratosG()) + " g\n"
                + "• Grasas: " + numero(plan.grasasG()) + " g\n\n" + consejo;
        return respuestaMotivadora(respuesta, contexto, List.of("¿Qué como en el almuerzo?", "¿Cuántas calorías debo comer?"));
    }

    private RespuestaChat rutina(String texto, ContextoChat contexto) {
        RutinaResponse rutina = contexto.rutina();
        if (rutina == null) {
            return contexto.perfilCompleto()
                    ? new RespuestaChat("¡" + nombre(contexto) + ", ya casi puedes empezar! 🏋️ "
                    + "Aún no tienes una rutina: ve a Inicio y toca «Generar mi plan». ¡Tu primer entrenamiento te espera!",
                    SUGERENCIAS_INICIALES)
                    : perfilIncompleto(contexto);
        }

        Matcher dia = DIA.matcher(texto);
        if (dia.find()) {
            int numero = Integer.parseInt(dia.group(1));
            Optional<RutinaResponse.Dia> encontrado = rutina.dias().stream().filter(d -> d.numero() == numero).findFirst();
            if (encontrado.isEmpty()) {
                return new RespuestaChat(nombre(contexto) + ", tu rutina tiene " + rutina.dias().size()
                        + " días de entrenamiento. Pregúntame por un día entre 1 y " + rutina.dias().size() + " y lo vemos juntos. 😊",
                        List.of("¿Qué ejercicios tengo en el día 1?"));
            }
            RutinaResponse.Dia d = encontrado.get();
            String ejercicios = IntStream.range(0, d.ejercicios().size())
                    .mapToObj(i -> {
                        RutinaResponse.Item e = d.ejercicios().get(i);
                        String descanso = e.descansoSeg() > 0 ? ", descanso " + e.descansoSeg() + " s" : "";
                        return (i + 1) + ". " + e.nombre() + " — " + prescripcion(e) + descanso;
                    })
                    .collect(Collectors.joining("\n"));
            List<String> sugerencias = numero < rutina.dias().size()
                    ? List.of("¿Qué ejercicios tengo en el día " + (numero + 1) + "?", "¿Cómo cambio un ejercicio?")
                    : List.of("¿Cómo cambio un ejercicio?", "¿Cuánto debo descansar?");
            String respuesta = "¡Vamos con toda, " + nombre(contexto) + "! 🏋️ Este es tu día " + d.numero() + ": "
                    + d.enfoque() + " (unos " + d.minutosEstimados() + " min)\n\n" + ejercicios
                    + "\n\nEn la pestaña Rutina puedes tocar cada ejercicio para ver cómo hacerlo paso a paso.";
            return respuestaMotivadora(respuesta, contexto, sugerencias);
        }

        String dias = rutina.dias().stream()
                .map(d -> "• Día " + d.numero() + ": " + d.enfoque() + " (" + d.ejercicios().size() + " ejercicios, unos "
                        + d.minutosEstimados() + " min)")
                .collect(Collectors.joining("\n"));
        String respuesta = "¡Me encanta tu energía, " + nombre(contexto) + "! 🏋️ Tu " + rutina.nombre().toLowerCase(ESPANOL)
                + " tiene " + rutina.diasPorSemana() + " días por semana:\n" + dias
                + "\n\nPregúntame por un día, por ejemplo «ejercicios del día 2».";
        return respuestaMotivadora(respuesta, contexto,
                List.of("¿Qué ejercicios tengo en el día 1?", "¿Cómo cambio un ejercicio?", "¿Cuánto debo descansar?"));
    }

    private RespuestaChat progreso(ContextoChat contexto) {
        if (!contexto.perfilCompleto()) {
            return perfilIncompleto(contexto);
        }
        var perfil = contexto.perfil();
        var progreso = contexto.progreso();
        boolean hayHistorial = progreso != null && progreso.medidas() != null && progreso.medidas().size() > 1
                && progreso.cambioPesoKg() != null;

        StringBuilder respuesta = new StringBuilder("¡Veamos cuánto has avanzado, ").append(nombre(contexto)).append("! 📈\n\n");
        if (perfil.imc() != null) {
            respuesta.append("Tu IMC actual es ").append(numero(perfil.imc())).append(" (")
                    .append(perfil.clasificacionImc()).append(").\n");
        }
        if (hayHistorial) {
            double cambio = progreso.cambioPesoKg();
            respuesta.append("Empezaste con ").append(numero(progreso.pesoInicial())).append(" kg y hoy registras ")
                    .append(numero(progreso.pesoActual())).append(" kg (")
                    .append(cambio > 0 ? "+" : "").append(numero(cambio)).append(" kg).\n");
            String objetivo = codigoObjetivo(contexto);
            if (objetivo.equals("PERDER_PESO") && cambio < 0 || objetivo.equals("GANAR_MASA") && cambio > 0) {
                respuesta.append("\n¡Vas en la dirección de tu objetivo! 🎉 Todo ese esfuerzo se está notando: date el crédito que mereces.\n");
            } else {
                respuesta.append("\nLos resultados llegan con constancia: cada entrenamiento suma, aunque la báscula tarde en mostrarlo.\n");
            }
        } else {
            respuesta.append("Tu peso registrado es ").append(numero(perfil.pesoKg())).append(" kg. ¡Este es tu punto de partida!\n");
        }
        respuesta.append("\nRegistra tu peso y tus medidas cada semana en la pestaña Progreso para ver tu evolución en el gráfico.");
        return respuestaMotivadora(respuesta.toString(), contexto, List.of("¿Cuántas calorías debo comer?", "Dame motivación"));
    }

    private RespuestaChat motivacion(ContextoChat contexto) {
        String n = nombre(contexto);
        String respuesta = n + ", primero que todo: ¡qué bueno que me escribas! 💚 Es completamente normal tener días "
                + "con menos energía, y aun así aquí estás, pensando en tu bienestar. Eso ya dice mucho de ti.\n\n"
                + "Algunas ideas para hoy:\n"
                + "• Empieza con solo 10 minutos: casi siempre, una vez que arrancas, quieres seguir.\n"
                + "• Pon tu música favorita y prepara tu ropa con anticipación.\n"
                + "• Mira tu progreso en la pestaña Progreso: ¡has recorrido más camino del que crees!\n"
                + "• Duerme bien e hidrátate; muchas veces el cansancio viene de ahí.\n\n"
                + "No se trata de hacerlo perfecto, se trata de no rendirse. Yo creo en ti, " + n + ". "
                + "¡Hoy es un gran día para demostrarte de lo que eres capaz! 🔥";
        return new RespuestaChat(respuesta, List.of("¿Qué ejercicios tengo en el día 1?", "¿Cómo va mi progreso?"));
    }

    private RespuestaChat descanso(ContextoChat contexto) {
        Integer dias = contexto.perfil() == null ? null : contexto.perfil().diasPorSemana();
        String diasDescanso = dias == null ? "" : "\n• Con tu plan de " + dias + " días de entrenamiento tienes "
                + (7 - dias) + " días de descanso por semana: aprovéchalos para caminar o estirar.";
        String respuesta = "¡Muy bien pensado, " + nombre(contexto) + "! 😴 Descansar también es entrenar: "
                + "es cuando tus músculos se recuperan y se hacen más fuertes.\n\n"
                + "• Duerme entre 7 y 9 horas.\n"
                + "• Evita entrenar el mismo grupo muscular con intensidad dos días seguidos."
                + diasDescanso + "\n"
                + "• Si tienes dolor muscular leve, un poco de movilidad y buena hidratación te ayudan a recuperarte.";
        return respuestaMotivadora(respuesta, contexto, List.of("¿Cuánta agua debo tomar?", "¿Qué ejercicios tengo en el día 1?"));
    }

    private RespuestaChat ayudaApp(ContextoChat contexto) {
        String respuesta = "¡Claro, " + nombre(contexto) + "! 📱 Te cuento cómo sacarle el máximo provecho a Smartbody:\n\n"
                + "• Inicio: tu resumen y el botón para generar tu plan.\n"
                + "• Rutina: los ejercicios de cada día; toca uno para ver cómo hacerlo o cambiarlo.\n"
                + "• Nutrición: tus comidas, recetas y cuánta agua tomar.\n"
                + "• Progreso: registra tu peso y medidas y mira tu evolución.\n"
                + "• Perfil: edita tus datos, restricciones y foto, o cierra sesión.\n\n"
                + "Y cuando tengas cualquier duda, aquí estoy para ayudarte. 😊";
        return respuestaMotivadora(respuesta, contexto, SUGERENCIAS_INICIALES);
    }

    private RespuestaChat agradecimiento(ContextoChat contexto) {
        String respuesta = "¡Gracias a ti, " + nombre(contexto) + "! 💚 Es un verdadero gusto acompañarte. "
                + "Aquí estaré siempre que necesites una mano con tu entrenamiento o tu alimentación.";
        return respuestaMotivadora(respuesta, contexto, SUGERENCIAS_INICIALES);
    }

    private RespuestaChat noEntendido(ContextoChat contexto) {
        return new RespuestaChat("¡Ups, " + nombre(contexto) + "! 🤔 No logré entenderte del todo, pero no te preocupes, "
                + "¡seguro lo resolvemos juntos!\n\n"
                + "Puedo ayudarte con tu rutina, tu plan de comidas y sus recetas, cuánta agua tomar, tu progreso "
                + "y el uso de la aplicación. Prueba con una de estas preguntas:", SUGERENCIAS_INICIALES);
    }

    private RespuestaChat perfilIncompleto(ContextoChat contexto) {
        return new RespuestaChat("¡" + nombre(contexto) + ", me encantaría ayudarte con eso! 😊 Para darte recomendaciones "
                + "hechas a tu medida, primero necesito conocerte un poquito más.\n\n"
                + "Completa tu perfil en la pestaña Perfil → Completar perfil. ¡Es rápido y es el primer paso "
                + "hacia tus metas! 📝💪", List.of("¿Cómo uso la aplicación?"));
    }

    private RespuestaChat sinPlan(ContextoChat contexto) {
        if (!contexto.perfilCompleto()) {
            return perfilIncompleto(contexto);
        }
        return new RespuestaChat("¡" + nombre(contexto) + ", ya casi lo tienes! 🍽️ Aún no tienes un plan nutricional: "
                + "ve a Inicio y toca «Generar mi plan». ¡Tu cuerpo te lo va a agradecer!", SUGERENCIAS_INICIALES);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /** Agrega al final una frase que motiva al usuario a seguir entrenando. */
    private RespuestaChat respuestaMotivadora(String respuesta, ContextoChat contexto, List<String> sugerencias) {
        String cierre = CIERRES_MOTIVADORES.get(azar.nextInt(CIERRES_MOTIVADORES.size())).replace("{nombre}", nombre(contexto));
        return new RespuestaChat(respuesta + "\n\n" + cierre, sugerencias);
    }

    /** Primer nombre del usuario con mayuscula inicial. El texto evita palabras con genero para incluir a todos. */
    static String nombre(ContextoChat contexto) {
        String nombre = contexto.primerNombre();
        if (nombre.isEmpty()) {
            return "deportista";
        }
        return nombre.substring(0, 1).toUpperCase(ESPANOL) + nombre.substring(1);
    }

    private static Optional<PlanNutricionalResponse.Comida> comidaMencionada(String texto, PlanNutricionalResponse plan) {
        String momento = contiene(texto, "desayun") ? "DESAYUNO"
                : contiene(texto, "almuerz") ? "ALMUERZO"
                : contiene(texto, "cena") || contiene(texto, "ceno") ? "CENA"
                : contiene(texto, "snack") || contiene(texto, "merienda") ? "SNACK"
                : null;
        if (momento == null) {
            return Optional.empty();
        }
        return plan.comidas().stream().filter(c -> c.momento().name().equals(momento)).findFirst();
    }

    /** "del desayuno", "de la cena". */
    private static String deLa(PlanNutricionalResponse.Comida comida) {
        String nombre = comida.nombre().toLowerCase(ESPANOL);
        return (comida.momento().name().equals("CENA") ? "de la " : "del ") + nombre;
    }

    private static String codigoObjetivo(ContextoChat contexto) {
        return contexto.perfil() == null || contexto.perfil().objetivo() == null ? "" : contexto.perfil().objetivo().codigo();
    }

    static String prescripcion(RutinaResponse.Item e) {
        if (e.repeticiones() != null) {
            return e.series() + " x " + e.repeticiones() + " reps";
        }
        if (e.duracionSeg() != null) {
            String duracion = e.duracionSeg() >= 60 && e.duracionSeg() % 60 == 0
                    ? (e.duracionSeg() / 60) + " min" : e.duracionSeg() + " s";
            return e.series() == 1 ? duracion : e.series() + " x " + duracion;
        }
        return e.series() + " series";
    }

    private static String vinetas(List<String> lineas) {
        return lineas.stream().map(l -> "• " + l).collect(Collectors.joining("\n"));
    }

    private static String numero(Double valor) {
        if (valor == null) {
            return "-";
        }
        return valor % 1 == 0 ? String.valueOf(valor.intValue()) : String.format(ESPANOL, "%.1f", valor);
    }
}
