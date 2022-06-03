package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.ejemplo2.databinding.FragmentUserBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)

        val user = Firebase.auth.currentUser!! //Obtenemos el usuario actual

        binding.apply {
            tVuserFb.text = user.displayName //Setteamos en el primer tv el usuario
            tVemailFb.text = user.email //Y en el segundo tv el email, para mostrarle esa información al usuario

            //Si le da al botón de modificar avanzamos al fragment modifica
            buttonMod.setOnClickListener {
                findNavController().navigate(R.id.action_userFragment_to_modificaFragment)
            }

            //Si le da al botón de eliminar, avanzamos al fragment elimina
            buttonElim.setOnClickListener {
                findNavController().navigate(R.id.action_userFragment_to_eliminaFragment)
            }
        }

        return binding.root
    }

}