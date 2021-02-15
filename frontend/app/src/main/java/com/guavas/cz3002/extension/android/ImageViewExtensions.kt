package com.guavas.cz3002.extension.android

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey

/**
 * Loads image from the given url using the Glide library.
 *
 * @param url The image URL.
 * @param fallback The fallback image if Glide fails to load the image.
 */
@BindingAdapter(value = ["srcGlideUrl", "fallbackId"])
fun ImageView.glideUrlSrc(url: Uri?, fallback: Drawable) {
    Glide.with(context)
        .load(url)
        .signature(ObjectKey(url ?: ""))
        .placeholder(fallback)
        .error(fallback)
        .fallback(fallback)
        .into(this)
}

/**
 * Sets the color for this image.
 *
 * @param color The color tint to apply.
 */
@BindingAdapter("tintInt")
fun ImageView.tintColor(color: Int?) = color?.let { setColorFilter(color) } ?: clearColorFilter()