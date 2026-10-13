package com.smartbody.presenter

/**
 * Validaciones de formulario del lado de la aplicacion (seccion 29).
 * Son una ayuda para el usuario: el backend vuelve a validar todo por seguridad.
 */
object Validaciones {

    private val FORMATO_EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val LONGITUD_MINIMA_PASSWORD = 8

    fun validarNombre(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu nombre"
        valor.trim().length < 2 -> "El nombre es demasiado corto"
        valor.trim().length > 100 -> "El nombre es demasiado largo"
        else -> null
    }

    fun validarEmail(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo"
        !FORMATO_EMAIL.matches(valor.trim()) -> "El correo no tiene un formato válido"
        else -> null
    }

    fun validarPassword(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña"
        valor.length < LONGITUD_MINIMA_PASSWORD -> "La contraseña debe tener al menos $LONGITUD_MINIMA_PASSWORD caracteres"
        else -> null
    }

    fun validarPasswordLogin(valor: String): String? =
        if (valor.isBlank()) "Ingresa tu contraseña" else null

    fun validarConfirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Acepta coma o punto como separador decimal. */
    fun leerDecimal(valor: String): Double? = valor.trim().replace(',', '.').toDoubleOrNull()

    fun validarEdad(valor: String): String? {
        val edad = valor.trim().toIntOrNull()
        return when {
            valor.isBlank() -> "Ingresa tu edad"
            edad == null -> "Ingresa un número entero"
            edad !in 12..100 -> "La edad debe estar entre 12 y 100 años"
            else -> null
        }
    }

    fun validarPeso(valor: String): String? = validarRango(valor, 25.0, 350.0, "peso", "kg", obligatorio = true)

    fun validarAltura(valor: String): String? = validarRango(valor, 100.0, 250.0, "altura", "cm", obligatorio = true)

    /** Medidas corporales opcionales del registro de progreso. */
    fun validarMedidaOpcional(valor: String, nombre: String, minimo: Double = 10.0, maximo: Double = 250.0): String? =
        validarRango(valor, minimo, maximo, nombre, "cm", obligatorio = false)

    private fun validarRango(
        valor: String,
        minimo: Double,
        maximo: Double,
        nombre: String,
        unidad: String,
        obligatorio: Boolean
    ): String? {
        if (valor.isBlank()) return if (obligatorio) "Ingresa tu $nombre" else null
        val numero = leerDecimal(valor) ?: return "Ingresa un número válido"
        return if (numero < minimo || numero > maximo) {
            "Debe estar entre ${minimo.toInt()} y ${maximo.toInt()} $unidad"
        } else {
            null
        }
    }
}
