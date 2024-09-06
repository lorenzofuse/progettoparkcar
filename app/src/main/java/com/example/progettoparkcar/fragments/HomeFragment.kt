package com.example.progettoparkcar.fragments

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progettoparkcar.R
import com.example.progettoparkcar.databinding.FragmentHomeBinding
import com.example.progettoparkcar.utils.ToDoAdapter
import com.example.progettoparkcar.utils.ToDoData
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import javax.security.auth.login.LoginException

class HomeFragment : Fragment(), AddParkPopUpFragment.DialogBtnClickListener, ToDoAdapter.ToDoAdapterInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var popUpFragment: AddParkPopUpFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>
    private var isEditing = false

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
        getDataFromFirebase()
        registerEvents()

    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Park")
            .child(auth.currentUser?.uid.toString())

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        adapter.setListener(this)
        binding.mainRecyclerView.adapter = adapter
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

        binding.btnLogout.setOnClickListener {
             if (auth.currentUser != null) {
                auth.signOut()
                Toast.makeText(context, "Logout effettuato", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_homeFragment_to_signInFragment)
            } else {
                 Toast.makeText(context, "Nessun utente attualmente loggato", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onSaveTask(todo: String, todoEt: TextInputEditText, location: LatLng) {

        val taskData = mapOf(
            "Park" to todo,
            "Posizione" to mapOf(
                "latitudine" to location.latitude,
                "longitudine" to location.longitude
            )
        )

        val taskRef = if (isEditing) {
            // Usare il taskId esistente per aggiornare il park
            databaseRef.child(popUpFragment.toDoData!!.taskId)
        } else {
            // Usare push solo per creare un nuovo park
            databaseRef.push()
        }

        taskRef.setValue(taskData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val message =
                    if (isEditing) {
                        "Park aggiornato correttamente"
                    } else {
                        "Park salvato correttamente"
                    }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                todoEt.text = null

                if (!isEditing) {
                    val updateToDoData = ToDoData(popUpFragment.toDoData?.taskId ?: "", todo, location)
                    val index = mList.indexOfFirst { it.taskId == updateToDoData.taskId }
                    if (index != -1) {
                        mList[index] = updateToDoData
                        adapter.notifyItemChanged(index)
                    }
                    isEditing = false
                } else {
                    taskRef.key?.let { key ->
                        val newToDoData = ToDoData(key, todo, location)
                        mList.add(newToDoData)
                        adapter.notifyItemInserted(mList.size - 1)
                    }
                }
            } else {
                Toast.makeText(context, "Errore nel salvataggio: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
            popUpFragment.dismiss()
        }
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear per assicurarsi che la lista contenga i dati appena recuperati
                mList.clear()
                //scorro ogni elem/figlio
                for (taskSnapshot in snapshot.children) {

                    //estraggo i dati
                    val taskId = taskSnapshot.key
                    val taskTitle = taskSnapshot.child("Park").getValue(String::class.java)
                    val locationSnapshot = taskSnapshot.child("Posizione")

                    val location = LatLng(
                        locationSnapshot.child("latitudine").getValue(Double::class.java) ?: 0.0,
                        locationSnapshot.child("longitudine").getValue(Double::class.java) ?: 0.0,
                    )

                    if (taskId != null && taskTitle != null) {
                        val todoTask = ToDoData(taskSnapshot.key!!, taskTitle, location)
                        mList.add(todoTask)
                    }
                }
                //avvisa l'adapter che i dati sono stati aggiornati
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDeleteParkBtnClicked(toDoData: ToDoData) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Cancellazione effettuata", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditParkBtnClicked(toDoData: ToDoData) {
        isEditing = true
        popUpFragment = AddParkPopUpFragment.newInstance(toDoData)
        popUpFragment.setListener(this)
        popUpFragment.show(
            childFragmentManager,
            "AddParkPopUpFragment"
        )
    }

    override fun onMapClicked(toDoData: ToDoData) {
        //creo uri per usare lat long
        val uri = Uri.parse("geo:${toDoData.location?.latitude},${toDoData.location?.longitude}")
        //visualizzo pos geo
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    override fun onShareLocationClicked(toDoData: ToDoData) {
        val location = toDoData.location
        if (location != null) {
            val address = getAddressFromLocation(requireContext(), location.latitude, location.longitude)
            val shareMessage =
                if (address != "Indirizzo non trovato") {
                    "La macchina è parcheggiata qui: $address (Latitudine: ${location.latitude}, Longitudine: ${location.longitude})"
                } else {
                    "Indirizzo non trovato"
                }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND  //per inviare dati
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }

            if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Condividi posizione tramite"))
            } else {
                Toast.makeText(context, "Nessuna app disponibile per condividere la posizione", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0) ?: "Indirizzo non trovato"
            } else {
                "Indirizzo non trovato"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Errore nell'ottenere l'indirizzo"
        }
    }
}
