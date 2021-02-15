package com.guavas.cz3002.data.assignment

/**
 * Holds data about which security guard is assigned to which toilet location.
 *
 * @property user The security guard uid.
 * @property location The toilet location.
 */
data class Assignment(
    var user: String = "",
    var location: String? = null
)
