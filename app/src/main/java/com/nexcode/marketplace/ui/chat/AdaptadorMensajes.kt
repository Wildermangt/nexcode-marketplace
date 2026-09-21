package com.nexcode.marketplace.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ItemMensajeOtroBinding
import com.nexcode.marketplace.databinding.ItemMensajePropioBinding
import com.nexcode.marketplace.dominio.Mensaje

/**
 * Burbujas de la conversacion.
 *
 * Hay dos disenos: el mensaje propio (a la derecha, con el degradado de marca)
 * y el del interlocutor (a la izquierda, sobre fondo claro). Cual se usa lo
 * decide el UID de quien tiene la sesion abierta, de modo que el mismo
 * adaptador sirve para el comprador y para el vendedor.
 */
class AdaptadorMensajes(
    private val uidPropio: String
) : ListAdapter<Mensaje, RecyclerView.ViewHolder>(COMPARADOR) {

    class CasillaPropia(val vistas: ItemMensajePropioBinding) :
        RecyclerView.ViewHolder(vistas.root)

    class CasillaOtro(val vistas: ItemMensajeOtroBinding) :
        RecyclerView.ViewHolder(vistas.root)

    override fun getItemViewType(posicion: Int): Int =
        if (getItem(posicion).autorId == uidPropio) TIPO_PROPIO else TIPO_OTRO

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): RecyclerView.ViewHolder {
        val inflador = LayoutInflater.from(padre.context)
        return if (tipo == TIPO_PROPIO) {
            CasillaPropia(ItemMensajePropioBinding.inflate(inflador, padre, false))
        } else {
            CasillaOtro(ItemMensajeOtroBinding.inflate(inflador, padre, false))
        }
    }

    override fun onBindViewHolder(casilla: RecyclerView.ViewHolder, posicion: Int) {
        val mensaje = getItem(posicion)
        when (casilla) {
            is CasillaPropia -> {
                casilla.vistas.texto.text = mensaje.texto
                casilla.vistas.hora.text = Formato.hora(mensaje.fecha)
            }
            is CasillaOtro -> {
                casilla.vistas.autor.text = mensaje.autorNombre
                casilla.vistas.texto.text = mensaje.texto
                casilla.vistas.hora.text = Formato.hora(mensaje.fecha)
            }
        }
    }

    private companion object {
        const val TIPO_PROPIO = 1
        const val TIPO_OTRO = 2

        val COMPARADOR = object : DiffUtil.ItemCallback<Mensaje>() {
            override fun areItemsTheSame(anterior: Mensaje, nuevo: Mensaje) =
                anterior.id == nuevo.id

            override fun areContentsTheSame(anterior: Mensaje, nuevo: Mensaje) =
                anterior == nuevo
        }
    }
}
