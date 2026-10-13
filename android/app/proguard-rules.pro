# Reglas minimas para Retrofit/Gson en compilaciones ofuscadas.
-keep class com.smartbody.model.dto.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
