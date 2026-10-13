package com.smartbody.model.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Prepara la foto de perfil antes de subirla: corrige la orientacion, la recorta en
 * cuadrado y la reduce a JPEG liviano. Asi se ahorran datos y se respeta el limite del backend.
 */
class ProcesadorImagen(private val contentResolver: ContentResolver) {

    fun prepararFoto(uri: Uri, ladoMaximo: Int = 512, calidad: Int = 85): ByteArray? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        // Decodificar a menor resolucion evita cargar fotos de muchos megapixeles en memoria.
        var muestreo = 1
        while (max(limites.outWidth, limites.outHeight) / (muestreo * 2) >= ladoMaximo) {
            muestreo *= 2
        }
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val original = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: return null

        val orientada = corregirOrientacion(uri, original)
        val lado = min(orientada.width, orientada.height)
        val cuadrada = Bitmap.createBitmap(
            orientada,
            (orientada.width - lado) / 2,
            (orientada.height - lado) / 2,
            lado,
            lado
        )
        val destino = min(lado, ladoMaximo)
        val final = Bitmap.createScaledBitmap(cuadrada, destino, destino, true)

        return ByteArrayOutputStream().use { salida ->
            final.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            salida.toByteArray()
        }
    }

    private fun corregirOrientacion(uri: Uri, imagen: Bitmap): Bitmap {
        val orientacion = try {
            contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return imagen
        }
        val matriz = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(imagen, 0, 0, imagen.width, imagen.height, matriz, true)
    }
}
