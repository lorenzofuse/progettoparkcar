package com.example.progettoparkcar.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoparkcar.databinding.EachparkBinding
import com.example.progettoparkcar.databinding.FragmentAddParkPopUpBinding

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

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteParkBtnClicked(this)
                }

                binding.editTask.setOnClickListener {
                    listener?.onEditParkBtnClicked(this)
                }
            }
        }
    }

    interface ToDoAdapterInterface {
        fun onDeleteParkBtnClicked(toDoData: ToDoData)
        fun onEditParkBtnClicked(toDoData: ToDoData)
    }

}