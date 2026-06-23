package com.example.memberlog

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValidatorTest {

    @Test
    fun email_missingAtSign_returnsFalse() {
        assertFalse(Validator.isValidEmail("ivan.primjer.com"))
    }

    @Test
    fun email_correctFormat_returnsTrue() {
        assertTrue(Validator.isValidEmail("ivan@primjer.com"))
    }

    @Test
    fun password_tooShort_returnsFalse() {
        assertFalse(Validator.isValidPassword("12345"))
    }

    @Test
    fun password_enoughCharacters_returnsTrue() {
        assertTrue(Validator.isValidPassword("123456"))
    }
}
