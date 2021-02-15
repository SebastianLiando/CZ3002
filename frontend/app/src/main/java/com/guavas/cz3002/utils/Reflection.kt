package com.guavas.cz3002.utils

import androidx.databinding.ViewDataBinding

/**
 * Tries to bind an object if it is declared in the binding.
 *
 * @param method The generated method name for setting the object.
 * @param argClass The class type for the object to set.
 * @param binding The binding object.
 * @param value The object to bind.
 */
fun <T : ViewDataBinding> tryAssignBinding(
    method: String,
    argClass: Class<*>,
    binding: T,
    value: Any
) {
    val methodNames = binding::class.java.methods.map { it.name }

    if (method in methodNames) {
        binding::class.java.getMethod(method, argClass).invoke(binding, value)
    }
}