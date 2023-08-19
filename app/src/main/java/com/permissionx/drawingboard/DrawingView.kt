package com.permissionx.drawingboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView: View {
    //默认颜色
    private var mColor = Color.BLACK
    //默认笔刷大小
    private var strokeSize = 10f
    //默认画笔
    private val mPen = Paint().apply {
        color = mColor
        strokeWidth = strokeSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    //路径对象
    private var mPath:Path? = null
    //保存路径的可变数组
    private val pathList = mutableListOf<CustomPath>()
    //自定义View至少要重写一个构造函数，一般重写这2个即可
    constructor(context: Context): super(context) {}
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        pathList.forEach {
            mPen.color = it.color
            mPen.strokeWidth = it.size
            canvas?.drawPath(it.path, mPen)
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
    //将字符串转化为16进制的颜色资源
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
    //橡皮擦功能：将画笔颜色设置为白色即可，不过有个坏处就是会把背景的颜色也给一起涂掉
    fun erase() {
        mColor = Color.WHITE
    }

}