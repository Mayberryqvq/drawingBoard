package com.permissionx.drawingboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import kotlinx.android.synthetic.main.activity_main.*

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

}