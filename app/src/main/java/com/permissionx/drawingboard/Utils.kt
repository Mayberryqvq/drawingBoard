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
//默认为false，表示brushContainer是不显示状态。
var brushContainerFlag = false
//默认为false，表示floatingActionButton是关闭状态
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
    //保存图片是耗时操作，所以用suspend修饰，当执行到该函数时，切换到IO线程进行
    withContext(Dispatchers.IO) {
        //创建一个ContentValues对象，用于存储保存图片的相关信息，如文件名、MIME类型和保存路径等
        val content = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "pic.jpg")
            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        //通过调用contentResolver的insert方法将图片的信息插入到MediaStore.Images表中，并获取返回的图片Uri
        val imageUri = MyApplication.context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
        //通过调用contentResolver的openOutputStream方法打开指定Uri的输出流，以便将Bitmap写入文件中
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
    //创建一个ExifInterface对象，用于读取图像文件的Exif信息
    val exif = ExifInterface(outputImage.path)
    //获取图像的旋转方向信息，第一个参数是表示旋转方向的属性标签，第2个参数是默认值，表示无需旋转
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
        else -> bitmap
    }
}

fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
    //创建一个Matrix对象，用于进行图像变换操作
    val matrix = Matrix()
    //对Matrix对象按指定角度进行旋转
    matrix.postRotate(degree.toFloat())
    //创建一个新的旋转后的Bitmap对象
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    //回收原始Bitmap对象的资源
    bitmap.recycle()
    //返回旋转后的Bitmap对象
    return rotatedBitmap
}