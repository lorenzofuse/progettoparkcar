package com.example.progettoparkcar.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.progettoparkcar.R
import com.example.progettoparkcar.databinding.FragmentAddParkPopUpBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText

class AddParkPopUpFragment : DialogFragment() {

    private lateinit var binding:FragmentAddParkPopUpBinding
    private lateinit var listener : DialogBtnClickListener

    fun setListener(listener:DialogBtnClickListener){
        this.listener=listener
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddParkPopUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerEvent()
    }

    private fun registerEvent(){
        binding.todoNextBtn.setOnClickListener{
            val todoTask = binding.todoEt.text.toString()
            val currentLocation = LatLng(40.748817, -73.985428)
            if(todoTask.isNotEmpty()){
                listener.onSaveTask(todoTask, binding.todoEt, currentLocation) //agg var location
            }else{
                Toast.makeText(context, "Campi vuoti non ammessi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.todoClose.setOnClickListener{
            dismiss()
        }
    }
    interface DialogBtnClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText, location:LatLng)//, location: LatLng
    }


}
