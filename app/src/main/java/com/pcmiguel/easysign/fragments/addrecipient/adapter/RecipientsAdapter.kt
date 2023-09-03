package com.pcmiguel.easysign.fragments.addrecipient.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.fragments.adddocuments.model.Document
import com.pcmiguel.easysign.fragments.addrecipient.model.Recipient
import com.pcmiguel.easysign.services.ApiInterface
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.lang.Math.*

class RecipientsAdapter(private val list: List<Recipient>) :
    RecyclerView.Adapter<RecipientsAdapter.ItemViewHolder>() {

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

    fun onOptionsClickListener(listener: onOptionsClickListener) {
        mOptionsListener = listener
    }

    class ItemViewHolder(itemView: View, listener: onItemClickListener, optionsListener: onOptionsClickListener) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.name)
        val size : TextView = itemView.findViewById(R.id.email)
        val status : TextView = itemView.findViewById(R.id.status)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipient, parent, false)
        return ItemViewHolder(view, mListener, mOptionsListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]

    }

    fun getItem(position: Int): Recipient {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}