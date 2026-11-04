# Reglas de ProGuard. La version de publicacion no ofusca (minifyEnabled = false),
# asi que este archivo queda como punto de extension.
-keepattributes Signature
-keepattributes *Annotation*

# --- Llamada con Leandro (WebRTC) ---
# WebRTC llama a sus clases Java desde codigo nativo (JNI); R8 no ve esas llamadas.
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
