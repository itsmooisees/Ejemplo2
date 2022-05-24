package com.example.ejemplo2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplo2.adapter.OpinionesAdapter
import com.example.ejemplo2.databinding.FragmentOpinionesBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class OpinionesFragment : Fragment() {

    private lateinit var binding: FragmentOpinionesBinding
    private val args by navArgs<OpinionesFragmentArgs>()

    private val database = Firebase.database
    private val myRef = database.getReference("juegos")
    private lateinit var messagesListener: ValueEventListener
    private val user = Firebase.auth.currentUser!!
    private lateinit var key: String

    private val opinionesList: MutableList<Opinion> = ArrayList()
    private val usuarios: MutableList<String> = ArrayList()
    private lateinit var stringOpiniones: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opiniones, container, false)

        rVopiniones()

        aniadeOpinion()

        return binding.root
    }


    private fun rVopiniones() {
        binding.opinionesRecyclerView.layoutManager = LinearLayoutManager(activity)

        messagesListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                opinionesList.clear()

                binding.apply {
                    val titulo = args.titulo

                    snapshot.children.forEach { child ->
                        if (titulo == child.child("titulo").getValue<String>()) {
                            usuarios.clear() //No debería ser necesario limpiar la lista de usuarios que han comentado, ya que estos no tienen la capacidad de eliminar las valoraciones,
                            //simplemente se van a ir añadiendo usuarios a la lista, pero por si acaso elimino yo alguna opinión pues para que se reflejen los cambios en la lista

                            key = child.key!!
                            stringOpiniones = child.child("comentarios").getValue<String>()!!

                            val opiniones = stringOpiniones.split("|").toMutableList()
                            opiniones.removeLast()

                            if (opiniones.size != 0) {
                                tVaviso.visibility = View.GONE

                                for (i in 0 until opiniones.size step 3) {
                                    val opinion = Opinion(opiniones[i], opiniones[i + 2])
                                    opinionesList.add(opinion)

                                    usuarios.add(opiniones[i + 1])
                                }

                                usuarios.forEach { email ->
                                    if (email == user.email) {
                                        buttonCreaOpin.isEnabled = false
                                        buttonCreaOpin.text = activity?.getString(R.string.tick)

                                        eTopin.isEnabled = false
                                        eTopin.hint = activity?.getString(R.string.yaOpi) //En estos dos casos sigue reventando así que tengo que ponerle la movida del activity?
                                    }
                                }

                            } else {
                                tVaviso.visibility = View.VISIBLE
                            }
                        }
                    }

                    opinionesRecyclerView.adapter = OpinionesAdapter(opinionesList)
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }

        }

        myRef.addValueEventListener(messagesListener)
    }

    private fun aniadeOpinion() {

        binding.apply {
            buttonCreaOpin.setOnClickListener {

                val opinion = eTopin.text.toString()

                if (opinion.isNotEmpty()) {
                    stringOpiniones += user.displayName + "|" + user.email + "|" + opinion + "|"
                    myRef.child(key).child("comentarios").setValue(stringOpiniones)

                    Toast.makeText(activity, R.string.aniadida, Toast.LENGTH_SHORT).show()
                    eTopin.setText("")

                } else {
                    Toast.makeText(activity, R.string.nadaOpi, Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

}