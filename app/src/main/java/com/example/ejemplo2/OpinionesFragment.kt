package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplo2.adapter.OpinionesAdapter
import com.example.ejemplo2.databinding.FragmentOpinionesBinding

class OpinionesFragment : Fragment() {

    private lateinit var binding: FragmentOpinionesBinding

    private val args by navArgs<OpinionesFragmentArgs>()

    private val opinionesList: MutableList<Opinion> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opiniones, container, false)

        rVopiniones()

        return binding.root
    }


    private fun rVopiniones() {
        binding.opinionesRecyclerView.layoutManager = LinearLayoutManager(activity)

        val listaComments = args.comentarios.split("|").toMutableList()
        listaComments.removeLast()

        if (listaComments.size != 0) {
            for (i in 0 until listaComments.size step 2) {
                val comentario = Opinion(listaComments[i], listaComments[i + 1])
                opinionesList.add(comentario)
            }

        } else {
            binding.tVaviso.visibility = View.VISIBLE
        }

        binding.opinionesRecyclerView.adapter = OpinionesAdapter(opinionesList)
    }

}