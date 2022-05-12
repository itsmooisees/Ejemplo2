package com.example.ejemplo2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplo2.adapter.JuegosAdapter
import com.example.ejemplo2.databinding.FragmentFeedBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private val database = Firebase.database //Obtenemos la instancia de la bbdd del proyecto actual
    private val myRef = database.getReference("juegos") //Obtenemos la referencia de la tabla indicada
    private lateinit var messagesListener: ValueEventListener
    private val juegosList: MutableList<Juego> = ArrayList() //Creamos una MutableList (ArrayList) para guardar todos los juegos que haya
    private var titulosFb = "" //Variable para guardar la concatenación de títulos
    private val user = Firebase.auth.currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false)

        creaRecyclerView()

        binding.apply {
            buttonUsuario.text = user?.displayName //Setteamos en el botón el nombre del usuario

            //onclicklistener para pasar de la feed al fragment de nuevo juego
            buttonJuego.setOnClickListener { view: View ->
                val action = FeedFragmentDirections.actionFeedFragmentToJuegoFragment(titulosFb) //Le adjuntamos el argumento que queremos pasar al fragment juego
                view.findNavController().navigate(action)
            }

            //onclicklistener para pasar de la feed al fragment de usuario
            buttonUsuario.setOnClickListener { view: View ->
                view.findNavController().navigate(R.id.action_feedFragment_to_userFragment)
            }
        }

        return binding.root
    }


    /**
     * Función mediante la cual vamos a generar el recyclerview con todos los juegos existentes
     */
    private fun creaRecyclerView() {
        binding.juegosRecyclerView.layoutManager = LinearLayoutManager(activity) //Cogemos el recyclerView de la vista y le asignamos el tipo de layout que va a utilizar para mostrar los items
        //Con un linearLayout, se irán mostrando linealmente los items de uno en uno. Se pueden utilizar otros layouts para mostrarlos en grupo de dos, tres...

        //El ValueEventListener escucha eventos cuando cambian datos en una ubicación
        messagesListener = object : ValueEventListener {

            //Esta función se supone que recibe un conjunto de datos que es lo que está contenido en la bbdd
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                juegosList.clear()
                //Hacemos un foreach para cada registro que haya en el conjunto
                dataSnapshot.children.forEach { child ->
                    val currentTit = child.child("titulo").getValue<String>()//Obtenemos el título del hijo actual
                    titulosFb += "$currentTit," //Lo concatenamos al string de los títulos

                    //Creamos un objeto juego con cada dato del registro en concreto
                    val juego = Juego(currentTit, //Obtenemos el dato por su clave, y lo obtenemos según su tipo
                        child.child("genero").getValue<String>(),
                        child.child("anio").getValue<Int>(),
                        child.child("descr").getValue<String>(),
                        child.child("conjunto").getValue<Float>(),
                        child.child("personas").getValue<Int>(),
                        child.child("foto").getValue<String>(),
                        child.child("usuarios").getValue<String>(),
                        child.child("valoracInd").getValue<String>(),
                        child.child("uploader").getValue<String>())

                    //Añadimos ese objeto a la MutableList
                    juegosList.add(juego)

                }
                //Le setteamos al adapter del recyclerView el adaptador que hemos creado antes, pasándole a este la MutableList con todos los juegos
                binding.juegosRecyclerView.adapter = JuegosAdapter(juegosList)
                binding.iVcarga.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }

        }

        myRef.addValueEventListener(messagesListener)

    }

}