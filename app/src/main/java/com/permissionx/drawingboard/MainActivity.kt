package com.permissionx.drawingboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var brushContainerFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushBtnEvent()
        brushSizeEvent()
    }

    private fun brushSizeEvent() {
        arrayOf(minSizeBtn, midSizeBtn, largeSizeBtn, giantSizeBtn).forEach {
            it.setOnClickListener {
                showOrHideBrushContainer(false)
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
        val Anim = TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
            Animation.RELATIVE_TO_SELF, startY, Animation.RELATIVE_TO_SELF, endY).apply {
            duration = 300
            fillAfter = true
            interpolator = BounceInterpolator()
        }
        brushSizeContainer.startAnimation(Anim)
        brushContainerFlag = !brushContainerFlag
        arrayOf(minSizeBtn, midSizeBtn, largeSizeBtn, giantSizeBtn).forEach {
            it.isEnabled = brushContainerFlag
        }
    }
}