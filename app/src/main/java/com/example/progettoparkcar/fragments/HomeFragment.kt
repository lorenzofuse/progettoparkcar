package com.example.progettoparkcar.fragments

import android.content.Intent
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

class HomeFragment : Fragment(), AddParkPopUpFragment.DialogBtnClickListener,
    ToDoAdapter.ToDoAdapterInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var popUpFragment: AddParkPopUpFragment
    private lateinit var adapter : ToDoAdapter
    private lateinit var mList:MutableList<ToDoData>
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
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Park")
            .child(auth.currentUser?.uid.toString())

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager=LinearLayoutManager(context)
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
    }



    override fun onSaveTask(todo: String, todoEt: TextInputEditText, location: LatLng) {
        Log.d("HomeFragment", "Salvando il park: $todo con posizione: $location")


        val taskData = mapOf(
            "Park" to todo,
            "Posizione" to mapOf("latitudine" to location.latitude, "longitudine" to location.longitude)
        )


        val taskRef = if(isEditing){
            databaseRef.child(popUpFragment.toDoData!!.taskId)
        }else{
            databaseRef.push()
        }


        taskRef.push().setValue(taskData).addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val message = if(isEditing) "Park aggiornato correttamente" else "Park salvato correttamente"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                todoEt.text = null
                if(isEditing){
                    val updateToDoData = ToDoData(popUpFragment.toDoData!!.taskId, todo, location)
                    val index = mList.indexOfFirst { it.taskId == updateToDoData.taskId }
                    if(index!= -1){
                        mList[index] = updateToDoData
                        adapter.notifyItemChanged(index)
                    }
                    isEditing=false
                }
            } else {
                Log.e("HomeFragment", "Errore nel salvataggio: ${task.exception?.message}")
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popUpFragment.dismiss()
        }
    }

    private fun getDataFromFirebase(){
        databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for(taskSnapshot in snapshot.children){
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
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onDeleteParkBtnClicked(toDoData: ToDoData) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Cancellazione effettuata", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onEditParkBtnClicked(toDoData: ToDoData) {
        isEditing=true
        popUpFragment = AddParkPopUpFragment.newInstance(toDoData)
        popUpFragment.setListener(this)
        popUpFragment.show(
            childFragmentManager,
            "AddParkPopUpFragment"
        )
    }




    override fun onMapClicked(toDoData: ToDoData) {
        val uri = Uri.parse("geo:${toDoData.location?.latitude},${toDoData.location?.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}
