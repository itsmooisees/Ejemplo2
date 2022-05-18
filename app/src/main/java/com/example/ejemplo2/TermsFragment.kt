package com.example.ejemplo2

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.ejemplo2.databinding.FragmentTermsBinding

class TermsFragment : Fragment() {

    private lateinit var binding: FragmentTermsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terms, container, false)

        //Fragment sencillito en el que prácticamente to es vista, de forma que en el código simplemente le pongo un movementmethod al tv de los términos
        binding.tVterminos.movementMethod = ScrollingMovementMethod()

        return binding.root
    }
}