package com.guavas.cz3002.extension.android

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

val TextView.textString
    get() = text?.toString() ?: ""

@BindingAdapter("timestampText")
fun TextView.dateText(timestamp: Long?) {
    timestamp ?: return

    val formatter = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT)
    text = formatter.format(Date(timestamp))
}