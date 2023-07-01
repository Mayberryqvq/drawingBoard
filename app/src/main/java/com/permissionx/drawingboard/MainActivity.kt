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
            //使用这种方式启动协程，当lifecycle被销毁时，协程一并销毁
            lifecycleScope.launch {
                val uri = saveImage(bitmap)
                //创建一个分享图片的Intent。通过Intent.ACTION_SEND指定动作为发送，使用putExtra将图片Uri添加到Intent中，设置类型为image/jpg
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/jpg"
                }
                //启动一个选择器界面，允许用户选择分享图片的目标应用程序。用户可以从列表中选择一个应用程序来分享图片
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
            //创建一个File对象，用于存放摄像头拍下的图片，存放在手机SD卡的应用关联缓存目录下
            outputImage = File(externalCacheDir, "output_image.jpg")
            if (outputImage.exists()) outputImage.delete()
            outputImage.createNewFile()
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //安卓7.0后，直接使用本地真实路径的URI被认为是不安全的。FileProvider是一种特殊的ContentProvider，可以选择性地将封装过的uri共享给外部，提高了应用安全性
                FileProvider.getUriForFile(this, "com.permissionx.drawingboard.fileprovider", outputImage)
            } else {
                //目标图片的本地真实路径
                Uri.fromFile(outputImage)
            }
            //隐式Intent，系统会找出能响应该Intent的Activity去启动，会打开照相机程序
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
                    //将照片解析成bitmap，设置到ImageView中显示出来
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