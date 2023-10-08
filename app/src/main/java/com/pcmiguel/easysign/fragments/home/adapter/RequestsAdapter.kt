package com.pcmiguel.easysign.fragments.home.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.services.ApiInterface
import com.squareup.picasso.Picasso

class RequestsAdapter(private val list: List<ApiInterface.SignatureRequest>) :
    RecyclerView.Adapter<RequestsAdapter.ItemViewHolder>() {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }


    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class ItemViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.title)
        val statusBackground : LinearLayout = itemView.findViewById(R.id.statusBackground)
        val status : TextView = itemView.findViewById(R.id.status)
        val image : ImageView = itemView.findViewById(R.id.image)
        val date : TextView = itemView.findViewById(R.id.date)

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

        val owner = item.requesterEmailAddress ?: ""
        val photoUrl = App.instance.preferences.getString("ProfilePhotoUrl", "")

        holder.title.text = item.title ?: ""

        if (item.createdAt != null) {
            holder.date.text = Utils.formatDate(item.createdAt!!)
        }

        if (photoUrl != "" && owner == App.instance.preferences.getString("Email", "")) {

            Picasso.get()
                .load(photoUrl)
                .placeholder(R.drawable.profile_template)
                .error(R.drawable.profile_template)
                .into(holder.image, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: java.lang.Exception?) {

                    }

                })

        }

        /*if (item.signatures != null) {

            val signers = item.signatures?.toMutableList() ?: mutableListOf()
            val stringBuilder = StringBuilder()

            for ((index, signer) in signers.withIndex()) {

                val email = signer.signerEmailAddress ?: ""
                var name = ""
                if (email == App.instance.preferences.getString("Email", "")) {
                    name = "Me"
                }
                else {
                    name = signer.signerName ?: ""
                }

                // Append the name to the StringBuilder
                stringBuilder.append(name)

                // Append a comma if it's not the last signer and there's more than one signer
                if (index < signers.size - 1 && signers.size > 1) {
                    stringBuilder.append(", ")
                }
            }

            // Set the concatenated names to the TextView
            holder.nameSigners.text = stringBuilder.toString()

        }*/

        val isComplete = item.isComplete
        val isDeclined = item.isDeclined
        val hasError = item.hasError

        //completed
        if (isComplete) {
            holder.status.text = "Completed"
            holder.statusBackground.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.statusBackground.context, R.color.completed))
            holder.status.setTextColor(holder.statusBackground.resources.getColor(R.color.complted2))
        }
        else {

            if (item.signatures != null) {

                val signers = item.signatures?.toMutableList() ?: mutableListOf()

                var needsAction = false
                for (signer in signers) {
                    if (signer.signerEmailAddress == App.instance.preferences.getString("Email", "") && signer.statusCode == "awaiting_signature") {
                        needsAction = true
                        break
                    }
                }

                if (needsAction) {
                    //needs action
                    holder.status.text = "Needs action"
                    holder.statusBackground.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(holder.statusBackground.context, R.color.need_action))
                    holder.status.setTextColor(holder.statusBackground.resources.getColor(R.color.need_action2))
                }
                else {
                    //Waiting for other
                    holder.status.text = "Waiting for other"
                    holder.statusBackground.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(holder.statusBackground.context, R.color.waiting))
                    holder.status.setTextColor(holder.statusBackground.resources.getColor(R.color.waiting2))
                }

            }
            else {
                //Waiting for other
                holder.status.text = "Waiting for other"
                holder.statusBackground.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.statusBackground.context, R.color.waiting))
                holder.status.setTextColor(holder.statusBackground.resources.getColor(R.color.waiting2))
            }

        }

        //rejected
        if (isDeclined) {
            holder.status.text = "Rejected"
            holder.statusBackground.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.statusBackground.context, R.color.rejected))
            holder.status.setTextColor(holder.statusBackground.resources.getColor(R.color.rejected2))
        }


    }

    fun getItem(position: Int): ApiInterface.SignatureRequest {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

}