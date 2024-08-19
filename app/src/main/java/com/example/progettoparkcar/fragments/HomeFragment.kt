package com.example.progettoparkcar.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.progettoparkcar.databinding.FragmentHomeBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(), AddParkPopUpFragment.DialogBtnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var popUpFragment: AddParkPopUpFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Park")
            .child(auth.currentUser?.uid.toString())
    }

    private fun registerEvents() {
        binding.addpark.setOnClickListener {
            popUpFragment = AddParkPopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(
                childFragmentManager,
                "AddParkPopUpFragment"
            )
        }
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText, location: LatLng) {
        Log.d("HomeFragment", "Salvando task: $todo con posizione: $location")

        // Salva il park su Firebase
        databaseRef.push().setValue(mapOf("task" to todo, "location" to location)).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Park salvato correttamente", Toast.LENGTH_SHORT).show()
                todoEt.text = null
            } else {
                Log.e("HomeFragment", "Errore nel salvataggio: ${it.exception?.message}")
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popUpFragment.dismiss()
        }
    }
}
