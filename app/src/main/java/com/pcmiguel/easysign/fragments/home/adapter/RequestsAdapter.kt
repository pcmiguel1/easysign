package com.pcmiguel.easysign.fragments.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.services.ApiInterface
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.lang.Math.*

class RequestsAdapter(private val list: List<ApiInterface.Requests>) :
    RecyclerView.Adapter<RequestsAdapter.ItemViewHolder>() {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }


    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class ItemViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.name)
        val category : ImageView = itemView.findViewById(R.id.category)

        init {

            itemView.setOnClickListener {

                listener.onItemClick(absoluteAdapterPosition)

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return ItemViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]

    }

    fun getItem(position: Int): ApiInterface.Requests {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}