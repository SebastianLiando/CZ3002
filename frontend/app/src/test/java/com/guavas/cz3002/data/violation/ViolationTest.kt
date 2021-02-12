package com.guavas.cz3002.data.violation

import com.guavas.cz3002.utils.Gender
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class ViolationTest {
    private lateinit var subject: Violation

    @Before
    fun setup() {
        subject = Violation()
    }

    @Test
    fun `isFalsePositive means it has been rejected by the security guard`() {
        subject.verifiedBy = "Some ID"
        subject.isTrue = false

        assertThat(subject.isFalsePositive, `is`(true))
    }

    @Test
    fun `isVerified means that a security guard has approved or reject the violation`() {
        subject.verifiedBy = null
        assertThat(subject.isVerified, `is`(false))

        subject.verifiedBy = "Someone"
        assertThat(subject.isVerified, `is`(true))
    }

    @Test
    fun `0 means that the gender is FEMALE`() {
        subject.locationGender = 0
        subject.detectedGender = 0

        assertThat(subject.locationGenderEnum, `is`(Gender.FEMALE))
        assertThat(subject.detectedGenderEnum, `is`(Gender.FEMALE))
    }

    @Test
    fun `1 means that the gender is MALE`() {
        subject.locationGender = 1
        subject.detectedGender = 1

        assertThat(subject.locationGenderEnum, `is`(Gender.MALE))
        assertThat(subject.detectedGenderEnum, `is`(Gender.MALE))
    }
}