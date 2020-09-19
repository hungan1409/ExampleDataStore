package com.example.datastore.extension

import android.os.SystemClock
import android.view.View

fun View.safeClick(listener: View.OnClickListener?, blockInMillis: Long = 500) {
    var lastClickTime: Long = 0
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < blockInMillis) {
            return@setOnClickListener
        }
        lastClickTime = SystemClock.elapsedRealtime()
        listener?.onClick(this)
    }
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}
