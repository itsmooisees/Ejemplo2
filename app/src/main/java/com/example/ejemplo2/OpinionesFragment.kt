package com.example.ejemplo2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplo2.adapter.OpinionesAdapter
import com.example.ejemplo2.databinding.FragmentOpinionesBinding
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

    private val opinionesList: MutableList<Opinion> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opiniones, container, false)

        rVopiniones()

        return binding.root
    }


    private fun rVopiniones() {
        binding.opinionesRecyclerView.layoutManager = LinearLayoutManager(activity)

        messagesListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                opinionesList.clear()

                val titulo = args.titulo

                snapshot.children.forEach { child ->
                    if (titulo == child.child("titulo").getValue<String>()){
                        val opiniones = child.child("comentarios").getValue<String>()!!.split("|").toMutableList()
                        opiniones.removeLast()

                        if (opiniones.size != 0) {
                            for (i in 0 until opiniones.size step 2) {
                                val opinion = Opinion(opiniones[i], opiniones[i + 1])
                                opinionesList.add(opinion)
                            }

                        } else {
                            binding.tVaviso.visibility = View.VISIBLE
                        }
                    }
                }

                binding.opinionesRecyclerView.adapter = OpinionesAdapter(opinionesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }

        }

        myRef.addValueEventListener(messagesListener)

    }

}