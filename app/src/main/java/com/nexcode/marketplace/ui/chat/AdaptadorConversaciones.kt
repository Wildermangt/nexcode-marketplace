package com.nexcode.marketplace.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ItemConversacionBinding
import com.nexcode.marketplace.dominio.Conversacion

/** Bandeja de conversaciones del vendedor, ordenada por el ultimo mensaje. */
class AdaptadorConversaciones(
    private val alAbrir: (Conversacion) -> Unit
) : ListAdapter<Conversacion, AdaptadorConversaciones.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemConversacionBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) = Casilla(
        ItemConversacionBinding.inflate(LayoutInflater.from(padre.context), padre, false)
    )

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val conversacion = getItem(posicion)
        val vistas = casilla.vistas

        val nombre = conversacion.compradorNombre.ifBlank { "Comprador" }
        vistas.nombre.text = nombre
        vistas.inicial.text = nombre.trim().take(1).uppercase()
        vistas.ultimo.text = conversacion.ultimoMensaje
        vistas.fecha.text = Formato.cuando(conversacion.fecha)

        vistas.sinLeer.visibility =
            if (conversacion.sinLeerVendedor > 0) View.VISIBLE else View.GONE
        vistas.sinLeer.text = conversacion.sinLeerVendedor.toString()

        vistas.tarjeta.setOnClickListener { alAbrir(conversacion) }
    }

    private companion object {
        val COMPARADOR = object : DiffUtil.ItemCallback<Conversacion>() {
            override fun areItemsTheSame(anterior: Conversacion, nuevo: Conversacion) =
                anterior.id == nuevo.id

            override fun areContentsTheSame(anterior: Conversacion, nuevo: Conversacion) =
                anterior == nuevo
        }
    }
}
