package com.nexcode.marketplace.ui.detalle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ItemOpinionBinding
import com.nexcode.marketplace.dominio.Calificacion

/** Lista de opiniones de un producto: autor, estrellas, comentario y fecha. */
class AdaptadorOpiniones :
    ListAdapter<Calificacion, AdaptadorOpiniones.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemOpinionBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) = Casilla(
        ItemOpinionBinding.inflate(LayoutInflater.from(padre.context), padre, false)
    )

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val opinion = getItem(posicion)
        val vistas = casilla.vistas

        val nombre = opinion.usuarioNombre.ifBlank { "Comprador" }
        vistas.autor.text = nombre
        vistas.inicial.text = nombre.trim().take(1).uppercase()
        vistas.estrellas.marcar(opinion.estrellas)
        vistas.fecha.text = Formato.fechaCorta(opinion.fecha)

        vistas.comentario.text = opinion.comentario
        vistas.comentario.visibility =
            if (opinion.comentario.isBlank()) View.GONE else View.VISIBLE
    }

    private companion object {
        val COMPARADOR = object : DiffUtil.ItemCallback<Calificacion>() {
            override fun areItemsTheSame(anterior: Calificacion, nuevo: Calificacion) =
                anterior.usuarioId == nuevo.usuarioId

            override fun areContentsTheSame(anterior: Calificacion, nuevo: Calificacion) =
                anterior == nuevo
        }
    }
}
