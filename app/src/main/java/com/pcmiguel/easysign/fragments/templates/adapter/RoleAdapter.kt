package com.pcmiguel.easysign.fragments.templates.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R

class RoleAdapter(private val list: List<String>) :
    RecyclerView.Adapter<RoleAdapter.ItemViewHolder>() {

    private lateinit var mListener : onItemClickListener
    private lateinit var mOptionsListener : onOptionsClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    interface onOptionsClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    fun onOptionsClickListener(listener : onOptionsClickListener) {
        mOptionsListener = listener
    }

    class ItemViewHolder(itemView: View, listener : onItemClickListener, optionsListener: onOptionsClickListener) : RecyclerView.ViewHolder(itemView) {

        val role : TextView = itemView.findViewById(R.id.role)
        val optionsBtn : View = itemView.findViewById(R.id.optionsBtn)

        init {

            itemView.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)
            }

            optionsBtn.setOnClickListener {
                optionsListener.onItemClick(absoluteAdapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_role, parent, false)
        return ItemViewHolder(view, mListener, mOptionsListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]

        holder.role.text = item


    }

    fun getItem(position: Int) : String {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}