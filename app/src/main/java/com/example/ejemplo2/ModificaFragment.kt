package com.example.ejemplo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.ejemplo2.databinding.FragmentModificaBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class ModificaFragment : Fragment() {

    private lateinit var binding: FragmentModificaBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modifica, container, false)

        modificacion()

        return binding.root
    }


    /**
     * Método para modificar el usuario y la contraseña del usuario actual en la bbdd
     */
    private fun modificacion() {
        val user = Firebase.auth.currentUser!! //Obtenemos el usuario actual

        binding.apply {
            eTuserMod.hint = user.displayName //Establecemos como hint del primer et el usuario actual, para que quede mejor y recuerde cuál es su usuario
            //También se le settea un hint a los et de la contraseña, pero lo hago en los attributes del xml

            //Cuando pulsamos el botón
            buttonModifica.setOnClickListener {
                //Recogemos los datos de los et
                val userMod = eTuserMod.text.toString()
                val contraMod = eTcontraMod.text.toString()
                val confMod = eTconfMod.text.toString()

                //Patrón para la contraseña, que tendrá que tener de nuevo como mínimo 8 caracteres
                val passwordRegex = Pattern.compile("^" +
                        ".{8,}" +
                        "$"
                )

                //Si el usuario no está vacío es porque lo quiere modificar
                if (userMod.isNotEmpty()) {
                    //Establecemos el displayname con lo que ha introducido el usuario
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userMod
                    }

                    //Y llamamos a updateprofile para que cambie el usuario
                    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful)
                                //Si la tarea se hace bien, que se supone que es siempre, simplemente informamos al usuario y se habrá actualizado correctamente el usuario
                                Toast.makeText(activity, R.string.userMod, Toast.LENGTH_SHORT).show()
                            else
                                //Aquí nonca me ha petado la aplicación, pero por si acaso voy a poner un toast por si en algún momento pasa, para que el usuario se informa
                                Toast.makeText(activity, R.string.errorUsu, Toast.LENGTH_SHORT).show()
                        }
                }
                
                //Si la contraseña no está vacía entramos por aquí. Si está vacía simplemente sudamos
                if (contraMod.isNotEmpty()) {
                    buttonModifica.isEnabled = false //Deshabilitamos el botón para que el usuario no pete la aplicación si le vuelve a dar seguidamente

                    //Un buen when para comprobar cositas
                    when {
                        //En caso de que lo que se ha introducido en el primer campo de la contraseña no coincida con el patrón que hemos establecido antes
                        !passwordRegex.matcher(contraMod).matches() -> {
                            //No se cambia la contraseña ya que no cumple con el patrón, informamos y volvemos a habilitar el botón
                            Toast.makeText(activity, R.string.passDeb, Toast.LENGTH_SHORT).show()
                            buttonModifica.isEnabled = true
                        }

                        //En caso de que la contraseña no coincida con la confirmación de esta
                        contraMod != confMod -> {
                            //No se cambia la contraseña ya que no coincide, informamos y volvemos a habilitar el botón
                            Toast.makeText(activity, R.string.passNo, Toast.LENGTH_SHORT).show()
                            buttonModifica.isEnabled = true
                        }

                        //Si no pasa nada de lo anterior, será que la contraseña cumple con el patrón y coincide con la confirmación, y podremos actualizarla
                        else -> {
                            //Llamamos a updatepassword pasándole la contraseña
                            user.updatePassword(contraMod).addOnCompleteListener { task ->
                                    if (task.isSuccessful)
                                        //Si la tarea es exitosa, informamos de que se ha modificado bien
                                        Toast.makeText(activity, R.string.contraMod, Toast.LENGTH_SHORT).show()
                                    else
                                        //Si no se puede realizar (no sé pq), informamos de que no se puede cambiar ahora
                                        Toast.makeText(activity, R.string.errorContra, Toast.LENGTH_SHORT).show()

                                    buttonModifica.isEnabled = true //Pase lo que pase, vovlemos a activar el botón
                                }
                        }
                    }

                }

                //En caso de que ambos estén vacíos, informamos de que no se va a hacer nada
                if (userMod.isEmpty() && contraMod.isEmpty())
                    Toast.makeText(activity, R.string.nada, Toast.LENGTH_SHORT).show()

            }

        }
    }
}