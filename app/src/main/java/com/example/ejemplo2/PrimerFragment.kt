package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ejemplo2.databinding.FragmentPrimerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PrimerFragment : Fragment() {

    private lateinit var binding: FragmentPrimerBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_primer, container, false)

        iniciaSesion()

        //Si le da al tv para crear cuenta, navegamos a ese fragment
        binding.tVcrea.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_primerFragment_to_creaFragment)
        }

        return binding.root
    }

    //Método para iniciar sesión en la bbdd y así poder acceder a la app
    private fun iniciaSesion() {
        binding.apply {
            //Al pulsar el botón
            buttonEntrar.setOnClickListener { view: View ->
                //Recogemos los datos del et
                val email = eTemail.text.toString()
                val contra = eTpass.text.toString()

                when {
                    //Cuando el email o la contraseña estén vacíos solo informaremos de que hay que rellenar to
                    email.isEmpty() || contra.isEmpty() -> {
                        Toast.makeText(activity, R.string.vacios, Toast.LENGTH_SHORT).show()
                    }

                    //Si los dos están rellenos, podemos continuar con el inicio de sesión
                    else -> {
                        //Informamos de que tiene que esperar un poco y deshabilitamos el botón para que no pete la app si le da seguidamente
                        Toast.makeText(activity, R.string.espere, Toast.LENGTH_SHORT).show()
                        buttonEntrar.isEnabled = false

                        //Si ambos están rellenos, llamamos al auth pasándole los parámetros para que inicie la sesión
                        activity?.let {
                            auth.signInWithEmailAndPassword(email, contra).addOnCompleteListener(it) { task ->
                                    if (task.isSuccessful) {
                                        //En caso de que se haya iniciado sesión correctamente porque las credenciales son correctas, informamos y avanzamos al feed
                                        Toast.makeText(activity, R.string.inicio, Toast.LENGTH_SHORT).show()
                                        view.findNavController().navigate(R.id.action_primerFragment_to_feedFragment)
                                    } else {
                                        //En caso de que la tarea haya fallado, informamos de que hay algo mal y volvemos a activar el botón
                                        buttonEntrar.isEnabled = true
                                        Toast.makeText(activity, R.string.datosInc, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

}