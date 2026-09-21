package com.nexcode.marketplace.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.FragmentPerfilBinding
import com.nexcode.marketplace.ui.admin.AdminActivity
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.pedidos.PedidosActivity
import com.nexcode.marketplace.ui.principal.PrincipalActivity
import kotlinx.coroutines.launch

/**
 * Perfil del usuario: quien es, cuanto ha comprado y los accesos a pedidos,
 * panel del vendedor, chat y cierre de sesion.
 */
class PerfilFragment : Fragment() {

    private var _vistas: FragmentPerfilBinding? = null
    private val vistas get() = _vistas!!

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estado: Bundle?
    ): View {
        _vistas = FragmentPerfilBinding.inflate(inflador, contenedor, false)
        return vistas.root
    }

    override fun onViewCreated(vista: View, estado: Bundle?) {
        super.onViewCreated(vista, estado)
        vistas.encabezado.ajustarASistema()

        val usuario = app.usuario ?: return
        vistas.iniciales.text = usuario.iniciales
        vistas.nombre.text = usuario.nombre
        vistas.correo.text = usuario.correo
        vistas.tipoUsuario.text = getString(
            if (usuario.esAdministrador) R.string.tipo_administrador else R.string.tipo_comprador
        ).uppercase()
        vistas.miembroDesde.text = getString(
            R.string.miembro_desde, Formato.fechaLarga(usuario.fechaRegistro)
        )

        vistas.opcionPanel.visibility =
            if (usuario.esAdministrador) View.VISIBLE else View.GONE

        vistas.opcionPedidos.setOnClickListener {
            startActivity(Intent(requireContext(), PedidosActivity::class.java))
        }
        vistas.opcionPanel.setOnClickListener {
            startActivity(Intent(requireContext(), AdminActivity::class.java))
        }
        vistas.opcionChat.setOnClickListener {
            (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_chat)
        }
        vistas.opcionSalir.setOnClickListener { confirmarSalida() }

        Animaciones.aparecerConRebote(vistas.iniciales, 60)
        Animaciones.entradaEscalonada(200, vistas.nombre, vistas.correo, vistas.tipoUsuario)
    }

    override fun onResume() {
        super.onResume()
        cargarResumen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vistas = null
    }

    private fun cargarResumen() {
        val usuario = app.usuario ?: return
        lifecycleScope.launch {
            val pedidos = app.repositorioCarrito.pedidos(usuario.uid)
            if (_vistas == null) return@launch
            vistas.totalPedidos.text = pedidos.size.toString()
            vistas.totalGastado.text = Formato.monedaCorta(pedidos.sumOf { it.total })
        }
    }

    private fun confirmarSalida() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.salir_sesion)
            .setMessage(R.string.bienvenido_detalle)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.salir_sesion) { _, _ ->
                app.repositorioUsuarios.cerrarSesion()
                app.usuario = null
                (activity as? PrincipalActivity)?.irALogin()
            }
            .show()
    }
}
