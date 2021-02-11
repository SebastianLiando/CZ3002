package com.guavas.cz3002.data.violation

import android.text.format.DateUtils
import androidx.lifecycle.asLiveData
import com.guavas.cz3002.utils.Gender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.*

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
    var id: String = "",
    var location: String = "",
    @get:JvmName("getIsTrue") var isTrue: Boolean = false,
    var verifiedBy: String? = null,
    var imageId: String = "",
    var locationGender: Int = -1,
    var detectedGender: Int = -1,
    var timestamp: Float = -1f,
) {
    /** Returns `true` if this violation is a false positive. */
    val isFalsePositive
        get() = !isTrue && isVerified

    /** Returns `true` if this violation has been verified. */
    val isVerified
        get() = verifiedBy != null

    /** The gender of the toilet. */
    val locationGenderEnum
        get() = mapIntToGender(locationGender)

    /** The gender of the person entering the toilet. */
    val detectedGenderEnum
        get() = mapIntToGender(detectedGender)

    private fun mapIntToGender(value: Int) =
        when (value) {
            0 -> Gender.FEMALE
            1 -> Gender.MALE
            else -> throw IllegalStateException("Unknown gender! Got value $value")
        }

    /** The time that has passed since the violation happened. */
    val elapsedTime
        get() = flow {
            while (true) {
                val span = DateUtils.getRelativeTimeSpanString(
                    timestamp.toLong(),
                    Date().time,
                    DateUtils.MINUTE_IN_MILLIS
                )

                emit(span.toString())

                delay(1000)
            }
        }.distinctUntilChanged()
            .asLiveData()
}
