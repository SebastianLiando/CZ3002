package com.guavas.cz3002.extension.android

import androidx.fragment.app.Fragment

/** Closes the activity the fragment is attached to. */
fun Fragment.closeActivity() = requireActivity().finish()

/** Programmatically presses the back button. */
fun Fragment.pressBackButton() = requireActivity().onBackPressed()