package com.pcmiguel.easysign.fragments.createdocument.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.pcmiguel.easysign.R
import com.squareup.picasso.Picasso

class PhotoAdapter(
    var context: Context,
    var arrayList: ArrayList<Uri>
) : BaseAdapter() {

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view : View = View.inflate(context, R.layout.grid_item_photo_list, null)

        var image: ImageView = view.findViewById(R.id.image_photo)
        val deleteBtn : View = view.findViewById(R.id.deleteBtn)

        var photoItem : Uri = arrayList[position]

        if (photoItem != null) {

            Log.d("photoItem", photoItem.path.toString())

            Picasso.get()
                .load(photoItem)
                .into(image)

        }

        return view


    }

}