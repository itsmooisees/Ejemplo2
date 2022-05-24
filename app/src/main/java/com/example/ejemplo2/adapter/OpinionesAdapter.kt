package com.example.ejemplo2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemplo2.Opinion
import com.example.ejemplo2.R

class OpinionesAdapter (private val opinionesList: MutableList<Opinion>) : RecyclerView.Adapter<OpinionesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return OpinionesViewHolder(layoutInflater.inflate(R.layout.item_opinion, parent, false))
    }

    override fun onBindViewHolder(holder: OpinionesViewHolder, position: Int) {
        val item = opinionesList[position]
        holder.pinta(item)
    }

    override fun getItemCount(): Int = opinionesList.size

}