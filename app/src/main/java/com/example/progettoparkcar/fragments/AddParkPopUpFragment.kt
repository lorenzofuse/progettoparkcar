package com.example.progettoparkcar.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.example.progettoparkcar.R
import com.example.progettoparkcar.databinding.FragmentAddParkPopUpBinding
import com.example.progettoparkcar.utils.ToDoData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText

class AddParkPopUpFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentAddParkPopUpBinding
    private lateinit var listener: DialogBtnClickListener
    private var currentLocation: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    companion object {
        fun newInstance(toDoData: ToDoData): AddParkPopUpFragment {
            val fragment = AddParkPopUpFragment()
            fragment.toDoData = toDoData
            return fragment
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    var toDoData: ToDoData? = null

    fun setListener(listener: DialogBtnClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocation()

        binding = FragmentAddParkPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Richiedere i permessi se non sono già stati concessi
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    onLocationReceived()
                }
            }
    }

    private fun onLocationReceived() {
        currentLocation?.let {
            if (::map.isInitialized) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                map.addMarker(MarkerOptions().position(it).title("La tua posizione"))
            }
        }
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

            val locationToSave = currentLocation
            Log.d("AddParkPopUpFragment", "Button clicked: Task: $todoTask, Location: $locationToSave")

            if (todoTask.isNotBlank() && locationToSave != null) {
                listener.onSaveTask(todoTask, binding.todoEt, locationToSave)
            } else {
                Toast.makeText(context, "Campi vuoti o posizione non valida", Toast.LENGTH_SHORT).show()
            }
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Inizializza la variabile `map`
        map = googleMap

        // Se `currentLocation` è già disponibile, aggiorna la mappa
        currentLocation?.let {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            map.addMarker(MarkerOptions().position(it).title("La tua posizione"))
        } ?: run {
            // Imposta una posizione predefinita se `currentLocation` non è ancora disponibile
            val defaultLatLng = LatLng(28.7041, 77.1025) // Posizione predefinita
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10f))
            map.addMarker(
                MarkerOptions()
                    .position(defaultLatLng)
                    .title("Posizione Predefinita")
                    .snippet("La macchina è qui")
                    .alpha(1f)
                    .zIndex(1f)
                    .flat(true)
                    .visible(true)
            )
        }
    }

    interface DialogBtnClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText, location: LatLng)
    }

    // Gestione dei permessi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation() // Ottieni la posizione una volta ottenuto il permesso
            } else {
                Toast.makeText(requireContext(), "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
