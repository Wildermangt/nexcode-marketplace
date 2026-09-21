package com.nexcode.marketplace.ui.camara

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.databinding.ActivityCamaraBinding
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.avisar
import java.io.File

/**
 * Camara para fotografiar productos.
 *
 * Usa CameraX: la vista previa se ata al ciclo de vida de la actividad, asi que
 * la camara se libera sola al salir. La foto se guarda en la cache de la
 * aplicacion y se devuelve como {@code Uri} a la pantalla que la pidio, que es
 * quien la comprime y la sube a Firebase Storage.
 */
class CamaraActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityCamaraBinding

    private var captura: ImageCapture? = null
    private var camaraTrasera = true

    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) iniciarCamara() else mostrarSinPermiso()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityCamaraBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.barraSuperior.ajustarASistema()
        vistas.barraInferior.ajustarASistema(arriba = false, abajo = true)

        vistas.cerrar.setOnClickListener { finish() }
        vistas.capturar.setOnClickListener { tomarFoto() }
        vistas.cambiar.setOnClickListener {
            camaraTrasera = !camaraTrasera
            iniciarCamara()
        }
        vistas.pedirPermiso.setOnClickListener {
            pedirPermiso.launch(Manifest.permission.CAMERA)
        }

        if (hayPermiso()) iniciarCamara() else pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    private fun hayPermiso() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun mostrarSinPermiso() {
        vistas.sinPermiso.visibility = View.VISIBLE
    }

    /**
     * Ata la vista previa y la captura al ciclo de vida de la actividad.
     *
     * {@code ProcessCameraProvider.getInstance} devuelve un ListenableFuture:
     * la camara tarda unos milisegundos en quedar disponible y se avisa por
     * devolucion de llamada, ya en el hilo principal.
     */
    private fun iniciarCamara() {
        vistas.sinPermiso.visibility = View.GONE
        val futuro = ProcessCameraProvider.getInstance(this)
        futuro.addListener({
            try {
                val proveedor = futuro.get()

                val vistaPrevia = Preview.Builder().build().also {
                    it.setSurfaceProvider(vistas.vistaPrevia.surfaceProvider)
                }
                captura = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val selector = if (camaraTrasera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                proveedor.unbindAll()
                proveedor.bindToLifecycle(this, selector, vistaPrevia, captura)
            } catch (e: Exception) {
                vistas.root.avisar(getString(R.string.error_camara))
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun tomarFoto() {
        val captura = this.captura ?: return
        Animaciones.latido(vistas.capturar)

        val archivo = File(cacheDir, "producto_${System.currentTimeMillis()}.jpg")
        val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

        captura.takePicture(
            opciones,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                    setResult(
                        Activity.RESULT_OK,
                        Intent().setData(Uri.fromFile(archivo))
                    )
                    finish()
                }

                override fun onError(error: ImageCaptureException) {
                    vistas.root.avisar(getString(R.string.error_camara))
                }
            }
        )
    }

    /**
     * Contrato para pedir una fotografia desde otra pantalla.
     * Devuelve la {@code Uri} de la imagen capturada, o null si se cancelo.
     */
    class TomarFotoDeProducto : ActivityResultContract<Unit, Uri?>() {

        override fun createIntent(contexto: Context, entrada: Unit): Intent =
            Intent(contexto, CamaraActivity::class.java)

        override fun parseResult(codigo: Int, intento: Intent?): Uri? =
            if (codigo == Activity.RESULT_OK) intento?.data else null
    }
}
