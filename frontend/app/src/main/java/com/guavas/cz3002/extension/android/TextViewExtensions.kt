package com.guavas.cz3002.extension.android

import android.widget.TextView

val TextView.textString
    get() = text?.toString() ?: ""