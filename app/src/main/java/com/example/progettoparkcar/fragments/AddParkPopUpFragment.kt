package com.example.progettoparkcar.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.progettoparkcar.R
import com.example.progettoparkcar.databinding.FragmentAddParkPopUpBinding
import com.example.progettoparkcar.utils.ToDoData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText

class AddParkPopUpFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentAddParkPopUpBinding
    private lateinit var listener: DialogBtnClickListener
    private var currentLocation : LatLng? = null
    var toDoData : ToDoData? = null
    companion object {
        fun newInstance(toDoData: ToDoData): AddParkPopUpFragment {
            val fragment = AddParkPopUpFragment()
            fragment.toDoData = toDoData
            return fragment
        }
    }
    fun setListener(listener: DialogBtnClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddParkPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        registerEvent()
    }

    private fun registerEvent() {
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()

            val currentLocation = LatLng(40.748817, -73.985428)
            Log.d("AddParkPopUpFragment", "Button clicked: Task: $todoTask, Location: $currentLocation")


            if (todoTask.isNotBlank() && currentLocation != null) {
                listener.onSaveTask(todoTask, binding.todoEt, currentLocation)
            } else {
                Toast.makeText(context, "Campi vuoti o posizione non valida", Toast.LENGTH_SHORT).show()
            }
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }

    interface DialogBtnClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText, location: LatLng)
    }
    override fun onMapReady(map:GoogleMap) {


        val latLng = LatLng(28.7041, 77.1025)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f))

        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Posizione")
        markerOptions.snippet("La macchina è qui")
        markerOptions.alpha(3f)
        markerOptions.zIndex(1f)
         markerOptions.flat(true)
        markerOptions.visible(true)
         map?.addMarker(markerOptions)
    }

}
