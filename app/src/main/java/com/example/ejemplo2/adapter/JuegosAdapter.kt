package com.example.ejemplo2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemplo2.Juego
import com.example.ejemplo2.R

//JuegosAdapter tiene que recibir la lista de juegos que se quiera mostrar, escrito como MutableList<dataClass que sea>
//Entre los segundos <> va el ViewHolder del rv
class JuegosAdapter(private val juegosList: MutableList<Juego>) : RecyclerView.Adapter<JuegosViewHolder>() {

    //Este método crea la vista de cada ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuegosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return JuegosViewHolder(layoutInflater.inflate(R.layout.item_juego, parent, false))
    }


    //Este método va a pasar por cada uno de los items para llamar al pinta del juegosviewholder, pasandole ese item
    override fun onBindViewHolder(holder: JuegosViewHolder, position: Int) {
        val item = juegosList[position]
        holder.pinta(item)
    }


    //En este método hay que devolver el número de items que tiene la lista, su tamaño
    override fun getItemCount(): Int = juegosList.size

    //La de arriba es una forma mucho más simplificada de escribir una función cuando solo se va a hacer una línea de return
    /*override fun getItemCount(): Int {
        return juegosList.size
    }*/

}