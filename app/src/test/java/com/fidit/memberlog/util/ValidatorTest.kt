package com.fidit.memberlog.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
