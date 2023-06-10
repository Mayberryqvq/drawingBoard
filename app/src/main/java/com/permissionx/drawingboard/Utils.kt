package com.permissionx.drawingboard

import android.content.Context

//dp -> px
fun convert(context: Context, dp: Int): Float {
    return context.resources.displayMetrics.density * dp
}