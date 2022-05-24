package com.example.ejemplo2.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemplo2.Opinion
import com.example.ejemplo2.databinding.ItemOpinionBinding

class OpinionesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemOpinionBinding.bind(view)

    @SuppressLint("SetTextI18n")
    fun pinta(opinion: Opinion) {
        binding.apply {
            tVusuOpin.text = opinion.user + " escribió:"
            tVopinion.text = opinion.comentario
        }
    }

}