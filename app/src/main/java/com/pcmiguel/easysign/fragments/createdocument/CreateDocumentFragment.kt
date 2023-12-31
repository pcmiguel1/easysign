package com.pcmiguel.easysign.fragments.createdocument

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentCreateDocumentBinding
import com.pcmiguel.easysign.fragments.createdocument.adapter.PhotoAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.libraries.scanner.activity.ScanActivity
import com.pcmiguel.easysign.libraries.scanner.constants.ScanConstants
import com.pcmiguel.easysign.libraries.scanner.util.ScanUtils
import java.io.*

class CreateDocumentFragment : Fragment() {

    private var binding: FragmentCreateDocumentBinding? = null

    private lateinit var gridView : GridView
    private var photos : ArrayList<Uri> = ArrayList()
    private lateinit var photoAdapter: PhotoAdapter

    private lateinit var loadingDialog: LoadingDialog

    private var newImage = false
    private var images : MutableList<File> = mutableListOf()

    private var noRecipients = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentCreateDocumentBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        // add an empty for the add photo button
        if (photos.isEmpty()) {
            val emptyUri: Uri = Uri.EMPTY
            photos.add(emptyUri)
        }

        if (arguments != null && requireArguments().containsKey("imageUri")) {
            val imageUri = arguments?.getParcelable<Uri>("imageUri")
            if (imageUri != null) {
                photos.add(imageUri)
            }
        }

        if (arguments != null && requireArguments().containsKey("newImage")) {

            newImage = arguments?.getBoolean("newImage") ?: false

        }

        if (arguments != null && requireArguments().containsKey("imagesUri")) {

            val imagesUri = arguments?.getSerializable("imagesUri") as? ArrayList<File>
            if (imagesUri != null) images.addAll(imagesUri)

        }

        if (arguments != null && requireArguments().containsKey("noRecipients")) {

            noRecipients = arguments?.getBoolean("noRecipients") ?: false

        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Create Document", requireActivity())

        loadingDialog = LoadingDialog(requireContext())

        gridView = binding!!.fotos
        photoAdapter = PhotoAdapter(requireContext(), photos!!)
        gridView.adapter = photoAdapter

        photoAdapter.setOnDeleteClickListener(object : PhotoAdapter.onDeleteClickListener {
            override fun onDeleteClick(position: Int) {

                val item = photoAdapter.getItem(position)

                val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_delete_photo, null)

                val builder = AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
                    .setCancelable(false)

                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val cancelBtn = mDialogView.findViewById<View>(R.id.cancelBtn)
                val deleteBtn = mDialogView.findViewById<View>(R.id.deleteBtn)

                cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }

                deleteBtn.setOnClickListener {

                    dialog.dismiss()

                    photos.removeAt(position)
                    photoAdapter.notifyDataSetChanged()

                }

                dialog.show()

            }

        })

        photoAdapter.setOnItemClickListener(object : PhotoAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_options_documents3, null)

                val builder = AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
                    .setCancelable(true)

                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val scanButton = mDialogView.findViewById<View>(R.id.scanBtn)
                val photoButton = mDialogView.findViewById<View>(R.id.photoBtn)


                scanButton.setOnClickListener {

                    dialog.dismiss()

                    //val intent = Intent(requireContext(), Scanner::class.java)
                    //startActivity(intent)
                    val intent = Intent(requireContext(), ScanActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE)

                }

                photoButton.setOnClickListener {

                    dialog.dismiss()

                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQUEST_CODE_GALLERY)

                }

                dialog.show()

            }

        })

        val documentName = binding!!.documentName

        binding!!.nextBtn.setOnClickListener {

            if (documentName.text.isNotEmpty() && photos.size > 1) {

                val pdfFile =  generatePdfFromUris(documentName.text.toString())
                if (pdfFile != null) {

                    requireArguments().remove("imageUri")
                    requireArguments().remove("imagesUri")

                    val bundle = Bundle()
                    bundle.putSerializable("pdfFile", pdfFile)
                    bundle.putBoolean("noRecipients", noRecipients)
                    if (newImage && images.isNotEmpty()) bundle.putSerializable("pdfsFile", ArrayList(images))
                    findNavController().navigate(R.id.action_createDocumentFragment_to_addDocumentsFragment, bundle)
                }

            }
            else {
                var erro = ""
                if (documentName.text.isEmpty()) erro = "Please fill in the document's name."
                if (photos.size <= 1) erro = "Please select at least one image."
                Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun generatePdfFromUris(pdfFileName: String) : File? {

        try {

            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()

            val pdfFile = File(storageDir, "$pdfFileName.pdf")

            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            val photosE : ArrayList<Uri> = ArrayList()
            photosE.addAll(photos)
            photosE.removeAt(0)

            for (uri in photosE) {
                val image = createImageFromUri(requireContext(), uri, PageSize.A4)
                document.add(image)
            }

            document.close()
            pdfWriter.close()

            return pdfFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    @Throws(IOException::class)
    private fun createImageFromUri(context: Context, uri: Uri, pageSize: PageSize): Image {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val scaledBitmap = scaleBitmap(bitmap, pageSize)

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageData = ImageDataFactory.create(byteArrayOutputStream.toByteArray())

        return Image(imageData)
    }

    private fun scaleBitmap(bitmap: Bitmap, pageSize: PageSize): Bitmap {
        val pageWidth = pageSize.width
        val pageHeight = pageSize.height

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val scaleWidth = pageWidth / bitmapWidth.toFloat()
        val scaleHeight = pageHeight / bitmapHeight.toFloat()

        val scaleFactor = if (scaleWidth > scaleHeight) scaleWidth else scaleHeight

        val newWidth = bitmapWidth * scaleFactor
        val newHeight = bitmapHeight * scaleFactor

        return Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {

            val selectedImageUri = data?.data

            if (selectedImageUri != null) {

                photos.add(selectedImageUri)
                photoAdapter.notifyDataSetChanged()

            }

        }
        else if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {

            if (data != null && data.extras != null) {

                val filePath = data.getStringExtra(ScanConstants.SCANNED_RESULT)
                val baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                val uri = Utils.bitmapToUri(requireContext(), baseBitmap)
                if (uri != null) {
                    photos.add(uri)
                    photoAdapter.notifyDataSetChanged()
                }


            }

        }

    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 334
        private const val REQUEST_CODE = 101
    }


}