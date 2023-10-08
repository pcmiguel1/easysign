package com.pcmiguel.easysign

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.blongho.country_data.Country

class CountryAdapter(context: Context, items: List<Country>) :
    ArrayAdapter<Country>(context, R.layout.language_spinner_dropdown_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.language_spinner_dropdown_item, parent, false)
        val flagImage: ImageView = view.findViewById(R.id.flag_image)
        val languageName: TextView = view.findViewById(R.id.language_name)
        val item = getItem(position)
        if (item != null) {
            flagImage.setImageResource(item.flagResource) // Adjust this according to your Country class
            languageName.text = item.name // Adjust this according to your Country class
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.language_spinner_dropdown_item, parent, false)
        val flagImage: ImageView = view.findViewById(R.id.flag_image)
        val languageName: TextView = view.findViewById(R.id.language_name)
        val item = getItem(position)
        if (item != null) {
            flagImage.setImageResource(item.flagResource) // Adjust this according to your Country class
            languageName.text = item.name // Adjust this according to your Country class
        }

        // Adjust the layout parameters as needed
        view.findViewById<View>(R.id.layout_container).layoutParams.height = 100

        return view
    }
}