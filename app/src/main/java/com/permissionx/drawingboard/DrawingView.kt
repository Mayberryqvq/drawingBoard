package com.permissionx.drawingboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView: View {

    private var mColor = Color.BLACK
    private var strokeSize = 10f
    private val mPen = Paint().apply {
        color = mColor
        strokeWidth = strokeSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    private var mPath:Path? = null
    private val pathList = mutableListOf<CustomPath>()

    constructor(context: Context): super(context) {}
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (pathList.size > 0) {
            pathList.forEach {
                mPen.color = it.color
                mPen.strokeWidth = it.size
                canvas?.drawPath(it.path, mPen)
            }
        }
        if (mPath != null) {
            mPen.color = mColor
            mPen.strokeWidth = strokeSize
            canvas?.drawPath(mPath!!, mPen)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mPath = Path()
                mPath?.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                mPath?.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                pathList.add(CustomPath(mPath!!, mColor, strokeSize))
                mPath = null
            }

        }
        return true
    }

    fun changeColor(color: String) {
        mColor = Color.parseColor(color)
    }

    fun changeStrokeSize(size: Float) {
        strokeSize = size
    }

    fun undo() {
        if (pathList.size > 0) pathList.removeLast()
        invalidate()
    }

    fun erase() {
        mColor  = Color.WHITE
    }

}