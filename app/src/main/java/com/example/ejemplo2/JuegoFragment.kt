package com.example.ejemplo2

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ejemplo2.databinding.FragmentJuegoBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class JuegoFragment : Fragment() {

    private lateinit var binding: FragmentJuegoBinding
    private val database = Firebase.database

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_juego, container, false)

        binding.buttonAniade.setOnClickListener { view: View ->
            //Si la llamada a la función recogeDatos devuelve un true, entonces el juego se ha insertado correctamente y podemos volver al feed
            if (recogeDatos())
                view.findNavController().navigate(R.id.action_juegoFragment_to_feedFragment)
        }

        return binding.root
    }

    //Método para recoger los datos de los campos e introducirlos en la bbdd si se cumplen las condiciones
    private fun recogeDatos(): Boolean {
        val myRef = database.getReference("juegos")

        binding.apply {
            //Recogemos todos los datos de los et en variables
            val titulo = eTtitulo.text.toString()
            val genero = eTgenero.text.toString()
            val anio = eTanio.text.toString()
            val descr = eTdescr.text.toString()
            val foto = eTurl.text.toString()


            //When que devolverá true o false en función de lo que pase
            return when {
                //Si hay algún campo sin rellenar, se informa y no se introduce el juego
                titulo.isEmpty() || genero.isEmpty() || anio.isEmpty() || descr.isEmpty() || foto.isEmpty() -> {
                    Toast.makeText(activity, R.string.vacios, Toast.LENGTH_SHORT).show()
                    false
                }

                //Si el año introducido no está en ese rango no podemos permitir introducir el juego, ya que o no existían o no ha llegado todavía ese año xd
                //Con el in y los .. podemos especificar un rango de números, incluyendo los dos escritos. Si ponemos !in es al revés, si está fuera de ese intervalo
                anio.toInt() !in 1980..2022 -> {
                    //Informamos de que la fecha no está bien
                    Toast.makeText(activity, R.string.fechaInc, Toast.LENGTH_SHORT).show()
                    false
                }

                //Si lo que se ha introducido en foto no es una URL, se informa y no se introduce el juego
                !Patterns.WEB_URL.matcher(foto).matches() -> {
                    Toast.makeText(activity, R.string.noUrl, Toast.LENGTH_SHORT).show()
                    false
                }

                //Cuando no haya pasado nada de lo anterior, será que to esta correcto y pasará por aquí para crear un objeto juego e introducirlo
                else -> {
                    val juego = Juego(titulo, genero, anio.toInt(), descr, 0f, 0, foto, "", "")
                    myRef.child(myRef.push().key.toString()).setValue(juego) //Obtenemos un hijo y le metemos una key ¿generada automáticamente? y los datos correspondientes

                    //Informamos de que se ha añadido bien
                    Toast.makeText(activity, R.string.aniadido, Toast.LENGTH_SHORT).show()
                    true
                }
            }

        }
    }
}