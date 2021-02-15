package com.guavas.cz3002.extension.android

import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

/**
 * Sets an error message to the layout.
 *
 * @param error The error message to be displayed.
 */
@BindingAdapter("errorText")
fun TextInputLayout.errorText(error: String?) {
    error?.let { if (it.isNotBlank()) setError(it) else setError(null) } ?: setError(null)
}

/**
 * Removes the error notification on the [TextInputLayout] when the text in the edit text changes.
 *
 * @param cancel `true` to enable the feature.
 */
@BindingAdapter("cancelErrorOnEdit")
fun TextInputLayout.cancelErrorOnEdit(cancel: Boolean) {
    if (cancel) {
        editText?.let { it.doOnTextChanged { _, _, _, _ -> error = null } }
    }
}