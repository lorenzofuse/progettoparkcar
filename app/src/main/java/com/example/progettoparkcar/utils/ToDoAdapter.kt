package com.example.progettoparkcar.utils

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoparkcar.databinding.EachparkBinding
import com.example.progettoparkcar.databinding.FragmentAddParkPopUpBinding
import java.util.Locale

class ToDoAdapter(private val list: MutableList<ToDoData>) :
    RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    private var listener: ToDoAdapterInterface? = null

    fun setListener(listener: ToDoAdapterInterface) {
        this.listener = listener
    }


    inner class ToDoViewHolder(val binding: EachparkBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = EachparkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.task

                val location = this.location
                if (location != null) {
                    val geocoder = Geocoder(binding.root.context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    val address = if (addresses!!.isNotEmpty()) {
                        val streetAddress = addresses[0].getAddressLine(0)
                        "$streetAddress"
                    } else {
                        "Indirizzo non trovato"
                    }
                    binding.locationText.text = address
                } else {
                    binding.locationText.text = "Posizione non disponibile"
                }

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteParkBtnClicked(this)
                }

                binding.editTask.setOnClickListener {
                    listener?.onEditParkBtnClicked(this)
                }



                binding.viewMapTask.setOnClickListener {
                    listener?.onMapClicked(this)
                }

                binding.share.setOnClickListener {
                    listener?.onShareLocationClicked(this)
                }
            }
        }
    }


    interface ToDoAdapterInterface {
        fun onDeleteParkBtnClicked(toDoData: ToDoData)
        fun onEditParkBtnClicked(toDoData: ToDoData)
        fun onMapClicked(toDoData: ToDoData)
        fun onShareLocationClicked(toDoData: ToDoData)
    }


}