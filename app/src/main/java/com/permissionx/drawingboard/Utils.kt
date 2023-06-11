package com.permissionx.drawingboard

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//dp -> px
fun convert(context: Context, dp: Int): Float {
    return context.resources.displayMetrics.density * dp
}

//View -> Bitmap
fun convertViewToBitMap(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    view.draw(canvas)
    return bitmap
}


fun requestPermission(activity: Activity) {
    activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
}

suspend fun saveImage(bitmap: Bitmap): Uri {
    var res = false
    var uri: Uri? = null
    withContext(Dispatchers.IO) {
        val content = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "pic.jpg")
            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val imageUri = MyApplication.context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
        if (imageUri != null) {
            val fos = MyApplication.context.contentResolver.openOutputStream(imageUri)
            res = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            uri = imageUri
        }
    }
    if (res) {
        Toast.makeText(MyApplication.context, "保存成功", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(MyApplication.context, "保存失败", Toast.LENGTH_LONG).show()
    }
    return uri!!

}