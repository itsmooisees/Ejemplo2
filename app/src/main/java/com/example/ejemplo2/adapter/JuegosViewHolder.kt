package com.example.ejemplo2.adapter

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ejemplo2.FeedFragmentDirections
import com.example.ejemplo2.Juego
import com.example.ejemplo2.databinding.ItemJuegoBinding

//Tiene que heredar de RecyclerView.ViewHolder pasandole la vista que hemos recibido como parámetro
class JuegosViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemJuegoBinding.bind(view)

    //Con esta función que recibe un objeto juego pasado desde el adapter como item, le asignamos a los tv cada atributo del objeto, y usamos glide para pintar la foto convirtiendo el string de la url
    fun pinta(juego: Juego) {
        binding.apply {
            tVtituloJuego.text = juego.titulo
            tVgeneroJuego.text = juego.genero
            tVanioJuego.text = juego.anio.toString()
            Glide.with(iVjuego.context).load(juego.foto).into(iVjuego)

            //onclicklistener para que cuando el usuario pulse en el item, se avance a la pantalla del detalle
            itemView.setOnClickListener { view: View ->
                //En action van los valores de cada variable metidos en un objeto juego, pasandolo por directions para que le llegue al fragment detalle
                val action = FeedFragmentDirections.actionFeedFragmentToDetalleFragment(juego)
                view.findNavController().navigate(action) //Navegamos con el navcontroller pasándole el action
            }
        }


    }

}