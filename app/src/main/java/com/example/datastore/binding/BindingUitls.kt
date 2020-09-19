package com.example.datastore.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageResource")
fun ImageView.setImageUrl(resourceID: Int) {
    if (resourceID != -1)
        Glide.with(context).load(resourceID).into(this)
}
