package com.pcmiguel.easysign

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LanguageAdapter(context: Context, items: List<Pair<Int, String>>) :
    ArrayAdapter<Pair<Int, String>>(context, R.layout.language_spinner_dropdown_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.language_spinner_dropdown_item, parent, false)
        val flagImage : ImageView = view.findViewById(R.id.flag_image)
        val languageName : TextView = view.findViewById(R.id.language_name)
        val item = getItem(position)
        if (item != null) {
            flagImage.setImageResource(item.first)
            languageName.text = item.second
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.language_spinner_dropdown_item, parent, false)
        val flagImage : ImageView = view.findViewById(R.id.flag_image)
        val languageName : TextView = view.findViewById(R.id.language_name)
        val item = getItem(position)
        if (item != null) {
            flagImage.setImageResource(item.first)
            languageName.text = item.second
        }

        view.findViewById<View>(R.id.layout_container).layoutParams.height = 100

        return view
    }
}