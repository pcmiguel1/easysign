package com.pcmiguel.easysign.fragments.createdocument

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.bumptech.glide.Glide
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentCreateDocumentBinding
import com.pcmiguel.easysign.fragments.createdocument.adapter.PhotoAdapter
import com.pcmiguel.easysign.fragments.createdocument.model.Photo

class CreateDocumentFragment : Fragment() {

    private var binding: FragmentCreateDocumentBinding? = null

    private lateinit var gridView : GridView
    private var photos : ArrayList<Uri> = ArrayList()
    private lateinit var photoAdapter: PhotoAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentCreateDocumentBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        val imageUri = arguments?.getParcelable<Uri>("imageUri")
        if (imageUri != null) {

            photos.add(imageUri)

        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Create Document", requireActivity())

        gridView = binding!!.fotos
        photoAdapter = PhotoAdapter(requireContext(), photos!!)
        gridView.adapter = photoAdapter

    }

    private fun setDataList() : ArrayList<Uri> {

        var arrayList : ArrayList<Uri> = ArrayList()



        return arrayList

    }

}