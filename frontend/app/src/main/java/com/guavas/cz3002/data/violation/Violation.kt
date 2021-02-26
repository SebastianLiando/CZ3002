package com.guavas.cz3002.data.violation

import com.google.firebase.database.Exclude
import com.guavas.cz3002.utils.Gender

/**
 * Holds violation details.
 *
 * @property id Violation id.
 * @property location The location of violation.
 * @property isTrue `true` if the violation is truly happening. Use [isFalsePositive] instead to check false positive.
 * @property verifiedBy The user uid that verifies the violation. If it has not been verified, this value will be `null`.
 * @property imageId The image name in firebase storage for this violation.
 * @property locationGender The gender of the toilet. Use [locationGenderEnum] instead.
 * @property detectedGender The gender of the person entering the toilet. Use [detectedGenderEnum] instead.
 * @property timestamp The time the violation happened in milliseconds since epoch.
 */
data class Violation(
    @get:Exclude var id: String = "",
    var location: String = "",
    @get:JvmName("getIsTrue") var isTrue: Boolean = false,
    var verifiedBy: String? = null,
    var imageId: String = "",
    var locationGender: Int = -1,
    var detectedGender: Int = -1,
    var timestamp: Double = -1.0,
) {
    /** Returns `true` if this violation is a false positive. */
    @get:Exclude
    val isFalsePositive
        get() = !isTrue && isVerified

    /** Returns `true` if this violation has been verified. */
    @get:Exclude
    val isVerified
        get() = verifiedBy != null

    /** The gender of the toilet. */
    @get:Exclude
    val locationGenderEnum
        get() = mapIntToGender(locationGender)

    /** The gender of the person entering the toilet. */
    @get:Exclude
    val detectedGenderEnum
        get() = mapIntToGender(detectedGender)

    /** Timestamp adjusted to Java's timestamp value. */
    @get:Exclude
    val adjustedTimestamp
        get() = (timestamp * 1000).toLong()

    private fun mapIntToGender(value: Int) =
        when (value) {
            0 -> Gender.FEMALE
            1 -> Gender.MALE
            else -> throw IllegalStateException("Unknown gender! Got value $value")
        }
}

/**
 * Data for displaying list of violations.
 *
 * @property violation The violation data.
 * @property timeString The elapsed time.
 */
data class ViolationListItem(val violation: Violation, val timeString: String)
