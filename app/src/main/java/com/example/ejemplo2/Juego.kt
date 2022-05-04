package com.example.ejemplo2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//Está como parcelable ya que se supone que es necesario para poder pasar un objeto como parámetro entre fragments
@Parcelize
data class Juego(
    var titulo: String?,
    var genero: String?,
    var anio: Int?,
    var descr: String?,
    var conjunto: Float?,
    var personas: Int?,
    var foto: String?,
    var usuarios: String?,
    var valoracInd: String?
) : Parcelable
