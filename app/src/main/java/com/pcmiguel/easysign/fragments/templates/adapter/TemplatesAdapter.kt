package com.pcmiguel.easysign.fragments.templates.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.services.ApiInterface

class TemplatesAdapter(
    private val list: List<ApiInterface.Templates>,
    private val selectTemplate : Boolean
) : RecyclerView.Adapter<TemplatesAdapter.ItemViewHolder>() {

    private lateinit var mEditListener : onEditClickListener
    private lateinit var mDeleteListener : onDeleteClickListener
    private lateinit var mPreviewListener : onPreviewClickListener
    private var selectedItemPosition = RecyclerView.NO_POSITION

    interface onEditClickListener {
        fun onItemClick(position: Int)
    }

    interface onDeleteClickListener {
        fun onItemClick(position: Int)
    }

    interface onPreviewClickListener {
        fun onItemClick(position: Int)
    }

    fun onEditClickListener(listener: onEditClickListener) {
        mEditListener = listener
    }

    fun onDeleteClickListener(listener: onDeleteClickListener) {
        mDeleteListener = listener
    }

    fun onPreviewClickListener(listener: onPreviewClickListener) {
        mPreviewListener = listener
    }

    fun setSelectedItem(position: Int) {
        val previousSelectedItem = selectedItemPosition
        selectedItemPosition = position
        notifyItemChanged(previousSelectedItem)
        notifyItemChanged(selectedItemPosition)
    }


    class ItemViewHolder(itemView: View, editListener: onEditClickListener, deleteListener: onDeleteClickListener, previewListener : onPreviewClickListener) : RecyclerView.ViewHolder(itemView) {

        val title : TextView = itemView.findViewById(R.id.title)
        val editBtn : View = itemView.findViewById(R.id.editBtn)
        val deleteBtn : View = itemView.findViewById(R.id.deleteBtn)
        val previewBtn : View = itemView.findViewById(R.id.previewBtn)
        val checkBtn : ImageView = itemView.findViewById(R.id.check)

        init {

            editBtn.setOnClickListener {
                editListener.onItemClick(absoluteAdapterPosition)
            }

            deleteBtn.setOnClickListener {
                deleteListener.onItemClick(absoluteAdapterPosition)
            }

            previewBtn.setOnClickListener {
                previewListener.onItemClick(absoluteAdapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
        return ItemViewHolder(view, mEditListener, mDeleteListener, mPreviewListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = list[position]

        holder.title.text = item.title ?: ""

        if (selectTemplate) {
            holder.checkBtn.visibility = View.VISIBLE
            holder.previewBtn.visibility = View.VISIBLE
            holder.editBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE

            if (position == selectedItemPosition) {
                holder.checkBtn.setImageResource(R.drawable.checkbox)
            }
            else {
                holder.checkBtn.setImageResource(R.drawable.unchecked_checkbox)
            }

            holder.itemView.setOnClickListener {
                setSelectedItem(position)
            }

        }

    }

    fun getSelectedItem() : ApiInterface.Templates? {
        return if (selectedItemPosition != RecyclerView.NO_POSITION) {
            list.getOrNull(selectedItemPosition)
        } else null
    }

    fun getItem(position: Int) : ApiInterface.Templates {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}