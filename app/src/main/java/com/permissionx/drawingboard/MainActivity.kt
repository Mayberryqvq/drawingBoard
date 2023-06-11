package com.permissionx.drawingboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var brushContainerFlag = false
    private var colorFlag = false

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
            val bitmap = convertViewToBitMap(drawingView)
            saveToAlbum(bitmap)
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




}