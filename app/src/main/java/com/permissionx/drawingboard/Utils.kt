package com.permissionx.drawingboard

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

lateinit var outputImage: File
const val takePhoto =1
const val fromAlbum = 2
lateinit var imageUri: Uri
var brushContainerFlag = false
var colorFlag = false

//dp -> px
fun convert(context: Context, dp: Int): Float {
    return context.resources.displayMetrics.density * dp
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

fun getBitmapFromUri(uri: Uri) = MyApplication.context.contentResolver.openFileDescriptor(uri, "r")?.use {
    BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
}

fun captureScreenWindow(activity: Activity): Bitmap? {
    activity.window.decorView.setDrawingCacheEnabled(true)
    return activity.window.decorView.getDrawingCache()
}

fun rotateIfRequired(bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(outputImage.path)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
        else -> bitmap
    }
}

fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    bitmap.recycle()
    return rotatedBitmap
}