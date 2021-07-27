package com.streamamg.amg_playkit

import android.graphics.Bitmap
import com.squareup.picasso.Transformation


class CustomImageTransform(val width: Int) : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        //val size = Math.min(source.width, source.height)
        if (source.height > 0) {
            val ratio: Double = source.width.toDouble() / source.height.toDouble()
            val newHeight = width / ratio
//            val x: Int = (source.width - width) / 2
//            val y: Int = (source.height - newHeight) / 2
            val result = Bitmap.createBitmap(source, 0, 0, width, newHeight.toInt())
            if (result != source) {
                source.recycle()
            }
            return result
        }
        return source
    }

    override fun key(): String {
        return "CustomImageTransform" + width;
    }
}