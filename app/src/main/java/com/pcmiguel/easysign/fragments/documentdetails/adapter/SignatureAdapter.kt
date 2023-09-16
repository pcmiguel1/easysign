package com.pcmiguel.easysign.fragments.documentdetails.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.services.ApiInterface

class SignatureAdapter(private val list: List<ApiInterface.Signature>) :
    RecyclerView.Adapter<SignatureAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.name)
        val email : TextView = itemView.findViewById(R.id.email)
        val status : TextView = itemView.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signature, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.signerName ?: ""
        holder.email.text = item.signerEmailAddress ?: ""
        holder.status.text = when (item.statusCode) {

            "signed" -> "Signed"
            "awaiting_signature" -> "Needs to Sign"
            "declined" -> "Declined"
            "expired" -> "Expired"

            else -> ""
        }

    }

    fun getItem(position: Int): ApiInterface.Signature {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}