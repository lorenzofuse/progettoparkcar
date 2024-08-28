package com.example.progettoparkcar.utils

import com.google.android.gms.maps.model.LatLng

data class ToDoData (val taskId:String, val task: String, val location: LatLng?){
}