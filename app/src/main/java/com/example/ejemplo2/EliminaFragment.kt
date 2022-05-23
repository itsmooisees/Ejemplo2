package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.ejemplo2.databinding.FragmentEliminaBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EliminaFragment : Fragment() {

    private lateinit var binding: FragmentEliminaBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_elimina, container, false)

        eliminacion()

        return binding.root
    }


    /**
     * Método para eliminar un usuario de la aplicación según lo que haya seleccionado el usuario
     */
    private fun eliminacion() {
        val user = Firebase.auth.currentUser!! //Obtenemos el usuario que está logeado

        binding.apply {
            buttonElimina.setOnClickListener {
                //Cuando suceda algo con los radiobutton
                when {
                    //Si el rbsi está marcado, entonces será que el usuario quiere borrarlo, con lo cual llamamos a la funcion para ello
                    radioButtonSi.isChecked -> {
                        user.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Si se ha podido completar la tarea con éxito, que debería ser así siempre, informamos al usuario con un mensajito y finalizamos la activity, cerrándose así la aplicación
                                Toast.makeText(activity, R.string.elimBien, Toast.LENGTH_SHORT).show()
                                activity?.finish()
                            } else {
                                //Solo me ha petado una vez el if y ya no recuerdo por qué
                                //Por mucho que he intentado volver a petarlo no he podido, pero se queda este filtro aquí por si aca
                                //Actualizo un tiempo después, no sé por qué pero me ha vuelto a petar y pasar por aquí. No ha sido una vez, siempre que le daba no me dejaba, hasta que cerré la aplicación y la volví a abrir y ahí ya sí me dejó, así que el filtro este se queda xd
                                Toast.makeText(activity, R.string.noElim, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    //Si el rbno está marcado, querrá decir que el usuario no quiere eliminar la cuenta, con lo cual le informamos con un mensajito y volvemos a la feed
                    radioButtonNo.isChecked -> {
                        Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_eliminaFragment_to_feedFragment)
                    }

                    //En caso de que no se haya seleccionado ninguna de las dos opciones, simplemente se informará al usuario de que tiene que marcar algo, y no se hará nada
                    else -> {
                        Toast.makeText(activity, R.string.selec, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

}