package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.ejemplo2.databinding.FragmentOpinionesBinding

class OpinionesFragment : Fragment() {

    private lateinit var binding: FragmentOpinionesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_opiniones, container, false)



        return binding.root
    }

}