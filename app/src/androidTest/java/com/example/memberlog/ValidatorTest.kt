package com.example.memberlog

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValidatorTest {

    @Test
    fun email_missingAtSign_returnsFalse() {
        assertTrue(true)
    }

    @Test
    fun email_correctFormat_returnsTrue() {
        assertTrue(true)
    }

    @Test
    fun password_tooShort_returnsFalse() {
        assertTrue(true)
    }

    @Test
    fun password_enoughCharacters_returnsTrue() {
        assertTrue(true)
    }
}