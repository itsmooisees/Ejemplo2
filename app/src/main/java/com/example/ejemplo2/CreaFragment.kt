package com.example.ejemplo2

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ejemplo2.databinding.FragmentCreaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class CreaFragment : Fragment() {

    private lateinit var binding: FragmentCreaBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Autenticamos al usuario
        auth = Firebase.auth
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_crea, container, false)

        //Llamamos al método para crear una cuenta, al método de activar o desactivar el botón, y al método de los términos
        crearCuenta()
        activaBoton()
        clickTexto()

        return binding.root
    }


    /**
     * Método para crear una cuenta con los datos que ha introducido el usuario, en caso de que to esté en orden
     */
    private fun crearCuenta() {
        binding.apply {
            //Ponemos un listener al botón para que haga la lógica cuando lo pulsamos
            buttonCrear.setOnClickListener { view: View ->
                //Recogemos los datos de los cuatro campos
                val usu = eTuser.text.toString()
                val email = eTemailCrea.text.toString()
                val pass = eTpassCrea.text.toString()
                val confirmaPass = eTconfirma.text.toString()

                //Creamos un patrón sencillito para la contraseña. Ésta ha de tener un mínimo de 8 caracteres
                val passwordRegex = Pattern.compile("^" +
                        ".{8,}" +
                        "$"
                )

                //Nos creamos un cuando
                when {
                    //Si todos los campos están vacíos informamos de que hay que rellenarlos
                    usu.isEmpty() && email.isEmpty() && pass.isEmpty() && confirmaPass.isEmpty() -> {
                        Toast.makeText(activity, R.string.vacios, Toast.LENGTH_LONG).show()
                    }

                    //Si el usuario está vacío, se informa
                    usu.isEmpty() -> {
                        Toast.makeText(activity, R.string.userVac, Toast.LENGTH_SHORT).show()
                    }

                    //Si el email está vacío o si lo que se ha introducido no cumple con el patrón para los email, se informa
                    email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Toast.makeText(activity, R.string.emailNo, Toast.LENGTH_SHORT).show()
                    }

                    //Si la contraseña está vacía o si lo introducido no coincide con el patrón escrito arriba, se informa
                    pass.isEmpty() || !passwordRegex.matcher(pass).matches() -> {
                        Toast.makeText(activity, R.string.passDeb, Toast.LENGTH_SHORT).show()
                    }

                    //Si lo contenido en los dos campos de las contraseñas no coincide entre sí, se informa
                    pass != confirmaPass -> {
                        Toast.makeText(activity, R.string.passNo, Toast.LENGTH_SHORT).show()
                    }

                    //Si no ha ocurrido nada de lo anterior, to estará en orden y se puede proceder a insertarlo en la bd
                    else -> {
                        //Avisamos de que tiene que esperar un poco para que cargue la info y deshabilitamos el botón para que no pueda volver a darle mientras se procesa y así no pueda petar la aplicación
                        //Porque si no deshabilito el botón y el usuario vuelve a pulsar mientras está cargando, la aplicación mete tremenda reventada
                        Toast.makeText(activity, R.string.espere, Toast.LENGTH_SHORT).show()
                        buttonCrear.isEnabled = false

                        //Llamamos al método de auth para crear una cuenta
                        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(requireActivity()) { task ->
                            //En caso de que la tarea sea exitosa, quiere decir que se ha registrado correctamente. Entonces, procederemos a guardar el nombre del usuario
                            if (task.isSuccessful) {
                                //Obtenemos el usuario actual, que es el recién creado y que no tiene el nombre del usuario metido
                                val user = Firebase.auth.currentUser!!

                                //Hacemos un userprofile... en el cual setteamos el displayName con lo que el usuario ha escrito en el edittext
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = usu
                                }

                                //Llamamos al método updateProfile para meter el usuario
                                user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                                    //En caso de que la tarea sea exitosa, informamos de que to está correcto y avanzamos al feed
                                    if (task.isSuccessful) {
                                        Toast.makeText(activity, R.string.creada, Toast.LENGTH_SHORT).show()
                                        view.findNavController().navigate(R.id.action_creaFragment_to_feedFragment)
                                    } else {
                                        //Este else se supone que es por si falla algo con el usuario, para volver a habilitar el botón, pero no sé que puede fallar
                                        //Abajo sí tiene sentido, ya que puede fallar al crear el correo como explico en el comentario, ero aquí no sé cómo puede fallar (estaría bien COMPROBAR)
                                        buttonCrear.isEnabled = true
                                    }
                                }

                            } else {
                                //En caso de que no se pueda crear el correo se informará de que ya existe y se volverá a habilitar el botón de crear para que el usuario pueda volver a crear
                                buttonCrear.isEnabled = true
                                Toast.makeText(activity, R.string.existe, Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
            }
        }
    }


    /**
     * Función para activar o descativar el botón
     */
    private fun activaBoton() {
        binding.apply {
            //Listener para habilitar o deshabilitar el botón crear en función de si está marcado o no el checkbox
            checkBoxTerm.setOnClickListener {
                buttonCrear.isEnabled = checkBoxTerm.isChecked
            }
        }
    }


    /**
     * Método para añadir un clickable al texto
     */
    private fun clickTexto() {
        //Creamos un spannablestring del texto al que queremos añadirle un click
        val spannableString = SpannableString(getString(R.string.checkBox))

        //Después creamos un clickablespan para definir qué va a pasar cuando se clique en el texto
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                //El click va a hacer que naveguemos al fragment de los términos
                p0.findNavController().navigate(R.id.action_creaFragment_to_termsFragment)
            }
        }

        //Al spannablestring hay que añadirle el clickablespan para que se le pueda hacer click, dónde va a comenzar y dónde va a terminar la zona del texto que se quiere clicar, y el último argumento ns qué es xdd
        spannableString.setSpan(clickableSpan, 22, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //Setteamos el spannablestring al tv, junto con un buffertype y obtenemos la instancia del linkmovementmethod
        binding.tVterm.setText(spannableString, TextView.BufferType.SPANNABLE)
        binding.tVterm.movementMethod = LinkMovementMethod.getInstance()
    }

}