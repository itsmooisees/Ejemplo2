package com.example.ejemplo2

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ejemplo2.databinding.FragmentDetalleBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class DetalleFragment : Fragment() {

    private lateinit var binding: FragmentDetalleBinding
    private val args by navArgs<DetalleFragmentArgs>() //Args que contiene el objeto juego concreto que le viene del feed fragment

    private val database = Firebase.database //Obtenemos la instancia de la bbdd del proyecto actual
    private val myRef = database.getReference("juegos") //Obtenemos la referencia de la tabla indicada
    private lateinit var messagesListener: ValueEventListener
    private var valorado = false //Variable un poco random para controlar si ya se ha valorado o no y parar el ondatachange ese, para que cuando el usuario cambie algo, las entradas no se actualicen infinitamente, se rallaba la bbdd
    private val user = Firebase.auth.currentUser //Obtenemos el usuario actual

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detalle, container, false)

        pintaDetalles()
        valoracion()

        return binding.root
    }


    /**
     * Función que va a mostrar gráficamente todos los campos del objeto con su información, que es la gracia del fragment
     */
    @SuppressLint("SetTextI18n")
    private fun pintaDetalles() {

        binding.apply {
            //Vamos setteando a cada tv su correspondiente dato de los args
            tVtituloDetalle.text = args.juego.titulo
            tVgeneroDetalle.text = args.juego.genero
            tVanioDetalle.text = args.juego.anio.toString()

            //Setteamos el movimiento del textview para que se pueda scrollear arriba y abajo
            tVdescrDetalle.movementMethod = ScrollingMovementMethod()
            tVdescrDetalle.text = args.juego.descr

            //Según lo que venga de personas, mostraremos un string por defecto si todavía no hay ninguna persona que haya valorado,
            //o si personas es mayor que 0 es porque alguien ya ha valorado y hacemos la media (si no hacía el filtro, si nadie había votado
            //salía NaN / 5, porque x/0 no se puede hacer, por eso es necesario el filtro en caso de que personas sea 0
            val personas = args.juego.personas!!

            if (personas > 0)
                tVvalFb.text = "%.2f".format(args.juego.conjunto!! / personas) + " / 5" //Redondeamos el número a las centésimas para que solo aparezcan dos decimales en vez de muchos cuando los haya
            else
                tVvalFb.text = "- / 5"

            //Aquí escuchamos constantemente cuando cambia la barra de valoración para settear en un tv su valor numérico
            ratingBarValDetalle.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, _, _ ->
                val valorEleg = ratingBarValDetalle.rating.toString() + " / 5"
                tVnumVal.text = valorEleg
            }

            //Aplicamos la imagen con el string que viene de args
            Glide.with(iVjuegoDetalle.context).load(args.juego.foto).apply(RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_broken_image)).into(iVjuegoDetalle)
            //OJO PARA MOSTRAR U OCULTAR ALGO: setVisibility(View.GONE/VISIBLE)
        }
    }


    /**
     * Método para controlar si un usuario ha valorado o no, y hacer una cosa u otra según su estado
     */
    private fun valoracion() {
        var existe = false //Variable para controlar si un usuario ya ha valorado un juego. Hago un foreach que recorre todos los usuarios que han valorado, y si alguno coincide con el actual, esta variable cambia a true y no entra al if

        //No sé exactamente si es necesario un onDataChange para obtener un datasnapshot, porque yo lo que quiero es obtener dicho datasnapshot para poder recorrer sus hijos
        //Entonces no sé si hay otro método que me dé este datasnapshot o ni siquiera sé si esta es la mejor forma de hacer lo que quier hacer, pero es la que sé y la cosa es que funciona xd
        messagesListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Si el usuario no ha valorado entrará aquí. Una vez que valore, ya no pasará por aquí y el ondatachange no actualizará nada
                if (!valorado) {
                    val titulo = args.juego.titulo!!

                    //Para cada hijo que haya en el datasnapshot
                    dataSnapshot.children.forEach { child ->
                        //En caso de que coincida el titulo del juego que viene de los args, que es el que el usuario ha pulsado, con el título de cada hijo de la bbdd que se recorre, entraremos en el if, porque es el juego en cuestión
                        if (titulo == child.child("titulo").getValue<String>()) {
                            eliminaJuego(titulo, child)
                            //Cada juego de la bbdd tiene dos campos, usuarios y valoracInd, que almacenan un string con todos los emails que han valorado un juego y las valoraciones que los usuarios han dado //Cada vez que un usuario valora, se introduce su correo en una posición y, en consecuencia, su valoración en la misma posición del otro campo
                            //En esas dos variables de abajo se almacenan ambos campos respectivamente y se splittean para así tener una lista de strings con cada email y cada valoración ordenado por su posición, de forma que luego podremos acceder con un índice a la valoración que ha dado el usuario x del foreach
                            val emails = args.juego.usuarios!!.split(",")
                            val valoraciones = args.juego.valoracInd!!.split(",")

                            var indice = 0 //Índice para acceder a la posición de valoraciones que le corresponda al email

                            //Para cada email que haya en la lista de emails del hijo
                            //Sería conveniente saber otra manera de recorrer todos los emails hasta que encuentre uno que coincida. Cuando coincida ya no recorrer más la lista ya que no va a volver a haber otra coincidencia, con lo cual es innecesario y un gasto de recursos.
                            emails.forEach { email ->

                                //Si el email actual del foreach coincide con el email del usuario actual que está usando la app, entonces querrá decir que el usuario ya ha valorado, con lo cual no podemos dejarla volver a valorar
                                if (email == user?.email) {
                                    existe = true //Cambiamos la variable existe a true para que no pueda acceder al if de abajo, y en consecuencia, no pueda volver a valorar
                                    val index = indice //Variable que tendrá el valor del índice cuando haya encontrado el juego y se lo pasará a confirmación, ya que si se le pasaba directamente índice, lo que le pasaba era el último número, cuando el índice había llegado al final

                                    binding.apply {
                                        ratingBarValDetalle.setIsIndicator(true) //Ponemos la ratingbar como indicator para que el usuario no pueda tocarla, ya que es simplemente visual en este caso para mostrar visualmente la valoración que dio en su momento el usuario
                                        ratingBarValDetalle.rating = valoraciones[index].toFloat() //Establecemos la valoración en la ratingbar, cogiéndola de la lista de valoraciones, por el índice que se ha ido incrementando a medida que iba buscando el email del usuario, y convirtiéndolo a float para poder asignarlo a la ratingbar

                                        buttonEnviar.text = activity?.getString(R.string.borra) //Setteamos el texto del botón a borrar, ya que existe la valoración y la función que hará entonces el botón será la de borrarla (tengo que poner lo de activity? porque si no había veces que petaba la app al hacer el getstring ns pq xd)

                                        //onclicklistener por si el usuario vuelve a pinchar si ya ha valorado, para informarle de que ya ha valorado, y que así quede mejor que simplemente deshabilitando el botón y que no haga nada
                                        buttonEnviar.setOnClickListener {
                                            confirmacion(titulo, emails, valoraciones, index, child)
                                        }
                                    }
                                }

                                indice++ //Aumentamos el índice por si el siguiente email coincide, acceder a la valoración correspondiente
                            }

                            //En caso de que el foreach termine y no haya coincidido ningún email, existe no se habrá puesto a true, lo que querrá decir que el usuario actual no ha valorado el juego seleccionado, así que le dejamos valorar
                            if (!existe) {
                                binding.apply {
                                    //onclicklistener para el botón enviar, para que mande la valoración
                                    buttonEnviar.setOnClickListener {
                                        //Serie de variables en las que vamos a guardar distintas cosas
                                        val conjunto = args.juego.conjunto!! + ratingBarValDetalle.rating //Recogemos el número del conjunto de valoraciones sumadas y le sumamos la valoración actual del usuario
                                        val personas = args.juego.personas!! + 1 //Recogemos el número de personas que han valorado ese juego y le sumamos uno
                                        val usuarios = args.juego.usuarios + user?.email + "," //Recogemos el string con todos los emails que han valorado y le añadimos el email del usuario actual más una coma para separar
                                        val valoracs = args.juego.valoracInd + ratingBarValDetalle.rating + "," //Recogemos el string con todas las valoraciones de los usuarios y le añadimos la valoración actual recogida del ratingbar más una coma para separar

                                        actualiza(conjunto, personas, usuarios, valoracs, child)

                                        ratingBarValDetalle.setIsIndicator(true) //Como ya ha valorado, ponemos la ratingbar como indicator pa que no la pueda tocar y se quede con su valoración. La próxima vez que entre al juego también sera indicator pero porque estará pasando por el if del foreach, ya que el email existe

                                        //Informamos al usuario de que se ha enviado la valoración
                                        Toast.makeText(activity, R.string.regVal, Toast.LENGTH_SHORT).show()

                                        //Deshabilito el botón para que el usuario no le pueda vovler a dar, aunque no pasaría nada ya que tenemos el filtro si ya ha valorado, aunque aquí pasa algo raro
                                        //Está deshabilitado porque si no lo hacía, al volver a pulsar en el botón me dice que la valoración se ha registrado pero no debería ya que 1 no puede registrarla y 2 ni siquiera la registra, es decir, no se hace to lo de registrar, pero me muestra el toast así que entiendo que está pasando por este if y no por el otro
                                        //Así que para evitar este "problema" simplemente lo deshabilito y fuera, solo pasa esto al valorar y quedarte en la página, una vez que sales y vuelves a entrar ya sí que entra por el otro if y sale el toast correspondiente. Pero me gustaría saber por qué pasa esto
                                        buttonEnviar.isEnabled = false
                                        buttonEnviar.text = getString(R.string.tick)

                                        valorado = true //Setteamos valorado a true para que el ondatachange no se raye y no cambie los valores infinitamente. Ns si esto es muy guarro pero es la forma que he encontrado para "detener" el ondatachange. Supongo que habrá otra función más adecuada pero no sé cuál es
                                    }
                                }
                            }

                        }

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }

        }

        myRef.addValueEventListener(messagesListener)
    }


    /**
     * Función para eliminar un juego de la bbdd. Solo lo puede eliminar el usuario que lo subió, y solo si lo han valorado menos de 3 personas (límite ampliable en el caso de que en un futuro haya muchos usuarios, osea nunca)
     */
    private fun eliminaJuego(titulo: String, child: DataSnapshot) {
        //Lo primero que hacemos es comprobar si el email que viene de los argumentos, que es el del usuario que lo ha subido, coincide con el email del usuario actual.
        //En caso de que coincida, será que es el usuario que lo ha subido, con lo cual mostraremos la visibilidad del botón para que el usuario pueda interactualr con él y borrar o no el juego. Si no coincide, no cambiará nada, con lo cual el botón seguirá oculto
        if (args.juego.uploader == user?.email) {
            binding.iVpapelera.visibility = View.VISIBLE //Lo setteamos a visible

            binding.iVpapelera.setOnClickListener { view -> //Y le ponemos un onclicklistener a la imagen, para que el usuario pueda clicarla
                val alertDialog = AlertDialog.Builder(requireActivity()) //Creamos un alertdialog para informar al usuario

                if (args.juego.personas!! > 3) { //En caso de que el número de personas que ha votado sea mayor a 3, el usuario no podrá eliminar el juego, ya que considero que han votado suficientes personas como para borrar sus valoraciones

                    alertDialog.apply {
                        setTitle(getString(R.string.error)) //Le ponemos un título de error

                        setMessage(getString(R.string.noElimJue)) //Mostramos un mensaje para informar al usuario de por qué no se borra el juego

                        setPositiveButton(getString(R.string.enten)) { _, _ -> } //Añadimos un botón para que el usuario simplemente lo pulse para cerrar el alertdialog, aunque también puede pulsar fuera del diálogo y tendrá el mismo efecto. No hace nada, simplemente cerrar
                    }.create().show()

                } else { //Si hay 3 personas o menos, entonces dejaremos borrar el juego al que lo publicó, informamos con un toast y navegamos de nuevo al feed, actualizándose este como siempre

                    alertDialog.apply {
                        setTitle((getString(R.string.confirm)))

                        setMessage(getString(R.string.seguroJue) + " $titulo" + getString(R.string.seguroJue2))

                        setPositiveButton(getString(R.string.siVal)) { _, _ ->
                            myRef.child(child.key!!).removeValue()
                            Toast.makeText(activity, R.string.elimJue, Toast.LENGTH_SHORT).show()
                            view.findNavController().navigate(R.id.action_detalleFragment_to_feedFragment)
                        }

                        setNegativeButton(getString(R.string.noVal)) { _, _ ->
                            Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
                        }
                    }.create().show()

                }
            }
        }
    }


    /**
     * Función para actualizar cambios en un hijo de la bbdd, ya sea para añadir o eliminar
     * Puede ser que exista una forma más simple de insertar en la bbdd sin tener que recurrir a un hashmap, pero de momento esto funciona perfectamente, si tengo tiempo lo miraré
     */
    @SuppressLint("SetTextI18n")
    private fun actualiza(conjunto: Float, personas: Int, usuarios: String, valoracs: String, child: DataSnapshot) {
        //Creamos un hashmap para guardar los pares clave valor que van a ser actualizados en la bbdd
        val cambios = HashMap<String, Any>()

        //Añadimos cada valor a una respectiva clave del hashmap, que coincide con la clave correspondiente en la bbdd
        cambios["conjunto"] = conjunto
        cambios["personas"] = personas
        cambios["usuarios"] = usuarios
        cambios["valoracInd"] = valoracs

        //myref tiene ya una referencia a juegos, con lo cual habrá que señalar al hijo (.child) y el nombre del hijo va a ser su key, que la obtenemos con el child.key, de forma que dentro de los paréntesis habrá una clave que será la del hijo correspondiente
        //Así, myref estará apuntando al hijo que corresponda, y con updatechildren le actualizamos mandándole los cambios necesarios (parece que solo se actualizan las claves que lleve el hashmap, las demás se quedan tal cual así que dabuty)
        myRef.child(child.key!!).updateChildren(cambios)

        //Setteamos en el tv la nueva valoración resultante de añadirle la del usuario, ya que la tenemos para mandarla a la bbdd pues también la setteamos
        binding.tVvalFb.text = "%.2f".format(conjunto / personas) + " / 5"
    }


    /**
     * Función para confirmar si el usuario quiere eliminar la valoración y, en caso afirmativo, realizar dicha eliminación
     */
    private fun confirmacion(titulo: String, emails: List<String>, valoraciones: List<String>, index: Int, child: DataSnapshot) {
        //AQUÍ SI ME SOBRA TIEMPO al final, sería idóneo darle una vuelta al tema de lo de conjunto y personas, ya que aquí tengo las dos listas separadas y con el final quitado, asi que para conjunto sería tan fácil como crear otra lista con los valores de la actual convertidos a float,
        //y coger la lista de los emails y por casa email (foreach) sumar uno a un contador, de forma que en dos variables quedarían almacenados el conjunto y las personas, pero sin necesidad de andar guardándolo en la bbdd. Como digo REVISARLO SI VEO QUE TAL

        //Probado lo de arriba ya dos veces y sigo sin sacarlo xdd, la primera me faltó organización sobre cómo lo iba a hacer y pasaban cosas como lo del NaN y otras
        //La segunda lo organicé mejor pero empezó a petar cuando pinchaba en un juego así que aborté misión. Volveré a intentarlo si veo que tal xdxd

        //Creamos un alertdialog para que el usuario confirme si quiere o no eliminar el juego, para dar mayor seguridad y profundidad a la app
        val alertDialog = AlertDialog.Builder(requireActivity())

        //Tanto los emails como las valoraciones que nos vienen ya previamente separadas por comas vienen como List, con lo cual no son modificables y hay que convertirlas a mutableList para poder eliminar la valoración y el usuario en cuestión
        val mutableEmails = emails.toMutableList()
        val mutableValoracs = valoraciones.toMutableList()

        //Como dividimos por comas y cada vez que añadimos un elemento lo hacemos con una coma al final, siempre vamos a tener un último elemento que va a ser vacío, ya que separando por comas, al final hay una coma que a su derecha tiene un campo vacío
        mutableEmails.removeLast()
        mutableValoracs.removeLast()

        alertDialog.apply {
            setTitle(getString(R.string.confirm)) //Setteamos el título al dialog

            setMessage("¿" + getString(R.string.seguro) + " $titulo?") //Setteamos el mensaje sobre el cual queremos que el usuario decida

            //Establecemos las acciones para el botón de respuesta positiva, setteándole en los paréntesis el texto que queremos que se muestre en cada botón
            setPositiveButton(getString(R.string.siVal)) { _, _ ->
                //Utilizamos las típicas variables conjunto y persona, pero en este caso le tenemos que restar el valor de la valoración en concreto que el usuario va a eliminar y restar 1 a la cantidad de personas que han votado
                val conjunto = args.juego.conjunto!! - mutableValoracs[index].toFloat()
                val personas = args.juego.personas!! - 1

                //Nos traemos la variable index de la llamada al método. Index contiene la posición en la que se ha encontrado el usuario que ha valorado, con lo cual es lo que utilizamos como índice para eliminar la valoración de su lista y el email de su lista
                //También la utilizamos para acceder a la valoración en la línea de arriba para restársela al conjunto
                mutableEmails.removeAt(index)
                mutableValoracs.removeAt(index)

                //Nos creamos dos variables mutables para poder ir concatenando cada email y cada valoración
                var email = ""
                var valoracs = ""

                //Dos foreach en los que se va accediendo a cada valor de las mutablelist y se va concatenando a un string, poniéndole la respectiva coma al final
                mutableEmails.forEach { em ->
                    email += "$em,"
                }

                mutableValoracs.forEach { valor ->
                    valoracs += "$valor,"
                }

                //Una vez que tenemos el conjunto actualizado habiéndole restado la valoración que se va a eliminar,
                //las personas habiéndole restado uno,
                //los emails habiéndole quitado el email del usuario actual y habiéndolo convertido a un único string,
                //y las valoraciones habiéndolas quitado la valoración concreta del usuario y habiéndolo convertido a un string,
                //llamamos al método actualiza y le pasamos las cuatro variables junto al hijo en concreto sobre el que se va a hacer la actualización, que es el del juego indicado y que viene del datasnapshot de enviavaloracion
                actualiza(conjunto, personas, email, valoracs, child)

                binding.apply {
                    //Ya que vamos a eliminar la varoración, es conveniente que esta no se quede ya reflejada gráficamente en la vista, ya que no es la realidad
                    //La realidad es que la valoración ya no existe, de forma que ese usuario no ha valorado, y poner ahí la valoración antigua hasta que se refresue el fragment no es lo más correcto, así que
                    ratingBarValDetalle.rating = 0f //establecemos el rating a 0, como si no hubiera valorado, ya que es la valoración que hay ahora (ninguna, no 0, pero bueno xd)
                    buttonEnviar.isEnabled = false //Deshabilitamos el botón para que no pueda volver a borrar y haga cosas raras (la ratingbar ya estaba deshabilitada de antes, con lo cual no puede tocarla hasta que se refresque el fragment y cambie to)
                    buttonEnviar.text = getString(R.string.tick) //En el botón establecemos como texto un tick, para que quede mejor y más correcto, ya que dejar como texto borrar cuando ya se ha hecho eso y encima no se puede pulsar el botón no queda muy bien
                }

                valorado = true //Ponemos la movida esta a true para que no vuelva a actualizar y haga cosas raras xdd

                //Informamos de que se ha eliminado la valoración
                Toast.makeText(activity, R.string.elimVal, Toast.LENGTH_SHORT).show()
            }

            //Le ponemos una acción al botón negativo para que simplemente muestre un toast informando de que no se ha eliminado
            setNegativeButton(getString(R.string.noVal)) { _, _ ->
                Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
            }
        }.create().show()
    }

}