package com.pcmiguel.easysign.fragments.addrole.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.services.ApiInterface

class SignerRoleAdapter(private val list : List<ApiInterface.SignerRole>)
    : RecyclerView.Adapter<SignerRoleAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val role : TextView = itemView.findViewById(R.id.role)
        val name : EditText = itemView.findViewById(R.id.name)
        val email : EditText = itemView.findViewById(R.id.email)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signer_to_role, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]

        holder.role.text = item.role ?: ""

        holder.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                list[holder.absoluteAdapterPosition].nameSigner = s.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        holder.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                list[holder.absoluteAdapterPosition].emailSigner = s.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

    }

    fun getItem(position: Int) : ApiInterface.SignerRole {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getAllItems() : List<ApiInterface.SignerRole> {
        return list
    }

    fun validateItems(): Boolean {

        val emailSet = HashSet<String>() //store unique emails
        var isValid = true

        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}"

        for (position in 0 until itemCount) {
            val item = getItem(position)

            val name = item.nameSigner
            val email = item.emailSigner

            //Check if name is empty
            if (name.isNullOrEmpty()) {
                isValid = false
            }

            //Check if email is empty, repeated and valid
            if (email.isNullOrEmpty() || emailSet.contains(email) || !email.matches(emailPattern.toRegex())) {
                isValid = false
            }
            else {
                emailSet.add(email)
            }

        }
        return isValid

    }


}