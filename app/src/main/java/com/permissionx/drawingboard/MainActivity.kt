package com.permissionx.drawingboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushBtnEvent()
        brushSizeEvent()
        floatingActionBtnEvent()
        colorBtnEvent()
        eraseBtnEvent()
        undoBtnEvent()
        saveBtnEvent()
        pickBtnEvent()
        cameraBtnEvent()
    }

    private fun brushSizeEvent() {
        arrayOf(minSizeBtn, midSizeBtn, largeSizeBtn, giantSizeBtn).forEach {
            it.setOnClickListener { _ ->
                showOrHideBrushContainer(false)
                drawingView.changeStrokeSize(it.tag.toString().toFloat())
            }
        }
    }

    private fun brushBtnEvent() {
        brushBtn.setOnClickListener {
            showOrHideBrushContainer(!brushContainerFlag)
        }
    }

    private fun showOrHideBrushContainer(flag : Boolean) {
        var startY = 0f
        var endY = 1f
        if (flag) {
            startY =1f
            endY = 0f
            brushSizeContainer.visibility = View.VISIBLE
        } else {
            brushSizeContainer.visibility = View.GONE
        }
        val anim = TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_SELF, startY, Animation.RELATIVE_TO_SELF, endY).apply {
            duration = 300
            fillAfter = true
            interpolator = BounceInterpolator()
        }
        brushSizeContainer.startAnimation(anim)
        brushContainerFlag = !brushContainerFlag
        arrayOf(minSizeBtn, midSizeBtn, largeSizeBtn, giantSizeBtn).forEach {
            it.isEnabled = brushContainerFlag
        }
    }

    private fun floatingActionBtnEvent() {
        floatingActionButton.setOnClickListener {
            var space = 0f
            space = if (colorFlag) convert(this, 60) else -convert(this, 60)
            val colorBtn = arrayOf(redBtn, orangeBtn, yellowBtn, greenBtn, cyanBtn, blueBtn, purpleBtn)
            colorBtn.forEach {
                val index = colorBtn.indexOf(it)
                it.animate()
                    .translationYBy(space * (index + 1))
                    .setDuration(300)
                    .setInterpolator(BounceInterpolator())
                    .start()
            }
            colorFlag = !colorFlag
        }
    }

    private fun colorBtnEvent() {
        arrayOf(redBtn, orangeBtn, yellowBtn, greenBtn, cyanBtn, blueBtn, purpleBtn).forEach {
            it.setOnClickListener { _ ->
                drawingView.changeColor(it.tag.toString())
            }
        }
    }

    private fun eraseBtnEvent() {
        eraseBtn.setOnClickListener {
            drawingView.erase()
        }
    }

    private fun undoBtnEvent() {
        undoBtn.setOnClickListener {
            drawingView.undo()
        }
    }

    private fun saveBtnEvent() {
        saveBtn.setOnClickListener {
            val bitmap = captureScreenWindow(this)
            saveToAlbum(bitmap!!)
        }
    }

    private fun saveToAlbum(bitmap: Bitmap) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch {
                val uri = saveImage(bitmap)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/jpg"
                }
                startActivity(Intent.createChooser(shareIntent, "share"))
            }

        } else {
            requestPermission(this)
        }
    }

    private fun pickBtnEvent() {
        pickBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
        }
    }

    private fun cameraBtnEvent() {
        cameraBtn.setOnClickListener {
            outputImage = File(externalCacheDir, "output_image.jpg")
            if (outputImage.exists()) outputImage.delete()
            outputImage.createNewFile()
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "com.permissionx.drawingboard.fileprovider", outputImage)
            } else {
                Uri.fromFile(outputImage)
            }
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, takePhoto)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    bg.setImageBitmap(rotateIfRequired(bitmap))
                }
            }
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        val bitmap = getBitmapFromUri(uri)
                        bg.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

}