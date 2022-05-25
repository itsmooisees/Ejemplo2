package com.example.ejemplo2

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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

    private lateinit var titulo: String
    private val usuarios: MutableList<String> = ArrayList()
    private lateinit var opiniones: MutableList<String>
    private lateinit var stringOpiniones: String
    private var index = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opiniones, container, false)

        rVopiniones()

        listenersOpinion()

        borraOpinion()

        return binding.root
    }


    private fun rVopiniones() {
        val opinionesList: MutableList<Opinion> = ArrayList()

        titulo = args.titulo

        binding.opinionesRecyclerView.layoutManager = LinearLayoutManager(activity)

        messagesListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                opinionesList.clear()

                binding.apply {
                    snapshot.children.forEach { child ->
                        if (titulo == child.child("titulo").getValue<String>()) {
                            usuarios.clear() //No debería ser necesario limpiar la lista de usuarios que han comentado, ya que estos no tienen la capacidad de eliminar las valoraciones,
                            //simplemente se van a ir añadiendo usuarios a la lista, pero por si acaso elimino yo alguna opinión pues para que se reflejen los cambios en la lista

                            var contador = -1

                            key = child.key!!
                            stringOpiniones = child.child("comentarios").getValue<String>()!!

                            opiniones = stringOpiniones.split("|").toMutableList()
                            opiniones.removeLast()

                            if (opiniones.size != 0) {
                                tVaviso.visibility = View.GONE

                                for (i in 0 until opiniones.size step 3) {
                                    val opinion = Opinion(opiniones[i], opiniones[i + 2])
                                    opinionesList.add(opinion)

                                    usuarios.add(opiniones[i + 1])
                                }

                                usuarios.forEach { email ->
                                    contador++

                                    if (email == user.email) {
                                        index = contador * 3

                                        buttonCreaOpin.visibility = View.INVISIBLE
                                        //buttonCreaOpin.text = activity?.getString(R.string.tick)

                                        iVpapeleraOpi.visibility = View.VISIBLE

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

    private fun listenersOpinion() {

        binding.apply {
            eTopin.imeOptions = EditorInfo.IME_ACTION_SEND
            eTopin.setRawInputType(InputType.TYPE_CLASS_TEXT)

            buttonCreaOpin.setOnClickListener {
                aniadeOpinion()
            }

            eTopin.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    aniadeOpinion()
                    return@OnKeyListener true
                }
                false
            })

        }
    }


    private fun aniadeOpinion() {
        val opinion = binding.eTopin.text.toString()

        if (opinion.isNotEmpty()) {
            stringOpiniones += user.displayName + "|" + user.email + "|" + opinion + "|"
            myRef.child(key).child("comentarios").setValue(stringOpiniones)

            Toast.makeText(activity, R.string.aniadida, Toast.LENGTH_SHORT).show()
            binding.eTopin.setText("")

        } else {
            Toast.makeText(activity, R.string.nadaOpi, Toast.LENGTH_SHORT).show()
        }
    }


    private fun borraOpinion() {
        binding.iVpapeleraOpi.setOnClickListener {
            var stringElim = ""

            val alertDialog = AlertDialog.Builder(requireActivity())

            alertDialog.apply {
                setTitle(getString(R.string.confirm))

                setMessage(getString(R.string.elimOpi) + " $titulo?")

                setPositiveButton(getString(R.string.siVal)) { _, _ ->
                    for (i in 0..2) {
                        opiniones.removeAt(index)
                    }

                    opiniones.forEach { indice ->
                        stringElim += "$indice|"
                    }

                    myRef.child(key).child("comentarios").setValue(stringElim)

                    binding.apply {
                        iVpapeleraOpi.visibility = View.GONE

                        buttonCreaOpin.visibility = View.VISIBLE

                        eTopin.isEnabled = true
                        eTopin.hint = getString(R.string.escrOpi)
                    }
                }

                setNegativeButton(getString(R.string.noVal)) { _, _ ->
                    Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
                }
            }.create().show()
        }

    }

}