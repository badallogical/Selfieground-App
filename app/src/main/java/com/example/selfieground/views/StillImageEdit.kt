package com.example.selfieground.views

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.*
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfieground.R
import com.example.selfieground.adapters.BackgroundListAdapter
import com.example.selfieground.adapters.BackgroundListOnClick
import com.example.selfieground.databinding.FragmentStillImageEditBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.nio.ByteBuffer


class StillImageEdit : Fragment(), BackgroundListOnClick {

    lateinit var binding : FragmentStillImageEditBinding

    private var preview : ImageView? = null
    private var imageUri : Uri? = null;

    private val REQUEST_CHOOSE_IMAGE = 1001
    private val REQUEST_IMAGE_CAPTURE = 1002
    private val PERMISSION_REQUESTS = 1

    private lateinit var img_list : List<Bitmap>;
    private lateinit var imageBitmap : Bitmap;

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentStillImageEditBinding.inflate(layoutInflater)

        preview = binding.preview

        // Handle take photo action
        binding.takePhoto.setOnClickListener{ view: View ->
            val popup_menu = PopupMenu(context, view)
            popup_menu.menuInflater.inflate(R.menu.get_photo_menu, popup_menu.menu)

            popup_menu.setOnMenuItemClickListener { menu_item: MenuItem ->
                if( menu_item.itemId == R.id.from_gallary ){
                    startChooseImageIntent()
                    return@setOnMenuItemClickListener true;
                }
                else if( menu_item.itemId == R.id.from_camera ){
                    startCameraIntent()
                    return@setOnMenuItemClickListener true;
                }
                false
            }

            popup_menu.show()
        }


//        img_list = listOf<Bitmap>(
//                resources.getDrawable(R.drawable.bg1).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg2).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg3).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg4).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg5).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg6).toBitmap(90,120,Bitmap.Config.ARGB_8888),
//                resources.getDrawable(R.drawable.bg7).toBitmap(90,120,Bitmap.Config.ARGB_8888) )


        Thread( Runnable {
            img_list = listOf<Bitmap>(
                resources.getDrawable(R.drawable.bg1).toBitmap( resources.getDrawable(R.drawable.bg1).intrinsicWidth,resources.getDrawable(R.drawable.bg1).intrinsicHeight, Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg2).toBitmap( resources.getDrawable(R.drawable.bg2).intrinsicWidth,resources.getDrawable(R.drawable.bg2).intrinsicHeight, Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg3).toBitmap( resources.getDrawable(R.drawable.bg3).intrinsicWidth,resources.getDrawable(R.drawable.bg3).intrinsicHeight,Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg4).toBitmap( resources.getDrawable(R.drawable.bg4).intrinsicWidth,resources.getDrawable(R.drawable.bg4).intrinsicHeight,Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg5).toBitmap( resources.getDrawable(R.drawable.bg5).intrinsicWidth,resources.getDrawable(R.drawable.bg5).intrinsicHeight,Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg6).toBitmap( resources.getDrawable(R.drawable.bg6).intrinsicWidth,resources.getDrawable(R.drawable.bg6).intrinsicHeight,Bitmap.Config.ARGB_8888),
                resources.getDrawable(R.drawable.bg7).toBitmap( resources.getDrawable(R.drawable.bg7).intrinsicWidth,resources.getDrawable(R.drawable.bg7).intrinsicHeight,Bitmap.Config.ARGB_8888) )
        }).start()


        // Handle background choose action
        binding.chooseBg.setOnClickListener{
            binding.bgConfig.isVisible = true
            binding.imageSetup.isVisible = false
            binding.bgList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL ,false)
            binding.bgList.adapter = BackgroundListAdapter( context, img_list, this )
        }

        binding.cancel.setOnClickListener{
            binding.imageSetup.isVisible = true
            binding.bgConfig.isVisible = false
        }

        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }

        return binding.root;
    }

    private fun startCameraIntent() {

        // for image quality purpose
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = context?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }

    private fun startChooseImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                REQUEST_CHOOSE_IMAGE
        )
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            imageBitmap = MediaStore.Images.Media.getBitmap(
                    context?.contentResolver, imageUri);

            preview?.setImageBitmap(imageBitmap)


        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            // In this case, imageUri is returned by the chooser, save it.
                Log.i("StillImageEdit", "File impored")
            val imageUriPath = data?.data?.path;
            imageBitmap = MediaStore.Images.Media.getBitmap( context?.contentResolver, data?.data)

            preview?.setImageBitmap(imageBitmap)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processImage(imageBitmap: Bitmap, bg_imageBitmap : Bitmap) {

        // create Segmentor
        val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()

        val segmenter = Segmentation.getClient(options)
        Log.d("MainActivity", "SegmenterProcessor created with option: $options")

        // Prepare Input Image
        val image = InputImage.fromBitmap(imageBitmap, 0)

        Log.i("StillImageEdit", "image Height = ${image.height} , image Width = ${image.width} ")
        preview?.setImageBitmap(imageBitmap)

        // Process Image
        segmenter.process(image).addOnCompleteListener{ result ->
            val mask = result.result.buffer
            val maskHeight = result.result.height
            val maskWidth = result.result.width

            Log.i("StillImageEdit", "before scale Height = ${bg_imageBitmap.width} , image Width = ${bg_imageBitmap.width} ")
            val scaled_bg_iamge = bg_imageBitmap.scale( imageBitmap.width, imageBitmap.height)

            val bitmap = Bitmap.createBitmap(
                    maskColorsFromByteBuffer(mask, maskWidth, maskHeight, scaled_bg_iamge)!!, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)

            val destBitmap = Bitmap.createBitmap(
                imageBitmap.width,
                imageBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas( destBitmap );
            val srcRect = Rect(0,0,imageBitmap.width, imageBitmap.height)
            canvas.drawBitmap(imageBitmap, srcRect, srcRect, null );
            canvas.drawBitmap( bitmap, srcRect,srcRect, null);
            preview?.setImageBitmap(destBitmap)

            Log.i("StillImageEdit", "mask Height = ${maskHeight} , mask Width = ${maskWidth} ")
        }



    }

    /** Converts byteBuffer floats to ColorInt array that can be used as a mask.  */
    @ColorInt
    private fun maskColorsFromByteBuffer(byteBuffer: ByteBuffer, maskWidth: Int, maskHeight: Int, bg_imageBitmap: Bitmap): IntArray? {
        @ColorInt val colors = IntArray(maskWidth * maskHeight)
        @ColorInt var bg_imageColor = IntArray( maskWidth * maskHeight)

        // fetch the pixel array from bitmap
        bg_imageBitmap.getPixels(bg_imageColor,0,bg_imageBitmap.width,0,0,maskWidth,maskHeight);
        Log.i("StillImageEdit", "scaled bgImage Height = ${bg_imageBitmap.width} , mask Width = ${bg_imageBitmap.height} ")

        // merge the mask and image
        for (i in 0 until maskWidth * maskHeight) {
            val backgroundLikelihood = 1 - byteBuffer.float
            if (backgroundLikelihood > 0.9) {
                colors[i] = Color.argb(160, bg_imageColor[i].red, bg_imageColor[i].green, bg_imageColor[i].blue)

            } else if (backgroundLikelihood > 0.2) {
                // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
                // when backgroundLikelihood is 0.9, the alpha is 128.
                // +0.5 to round the float value to the nearest int.
                val alpha = (182.9 * backgroundLikelihood - 36.6 + 0.5).toInt()
                colors[i] = Color.argb(alpha, bg_imageColor[i].red, bg_imageColor[i].green, bg_imageColor[i].blue)
            }
            else{
                colors[i] = Color.argb(0,255,255,255);
            }
        }
        return colors
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = requireActivity().packageManager
                .getPackageInfo(requireActivity().packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Permission granted: $permission")
            return true
        }
        Log.i("MainActivity", "Permission NOT granted: $permission")
        return false
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(requireContext(), it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(requireContext(), it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    requireActivity(), allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS
            )
        }
    }

    override fun onClick( position : Int ) {
        processImage(imageBitmap, img_list.get(position))
        Log.i("StillImageEdit","called listner")
    }
}

