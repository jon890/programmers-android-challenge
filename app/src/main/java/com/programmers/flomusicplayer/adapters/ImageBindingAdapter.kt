package com.programmers.flomusicplayer.adapters

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageBindingAdapter {
    // JvmStatic => Java의 Static에 대응한다
    @BindingAdapter("app:imageUrl", "app:placeholder")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?, placeholder: Drawable) {
        if (url == null) return

        Glide.with(imageView.context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .into(imageView)
    }
}