package com.fidit.memberlog.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PasswordHashTest {

    @Test
    fun sha256_isDeterministic() {
        assertEquals(PasswordHash.sha256("tajna123"), PasswordHash.sha256("tajna123"))
    }

    @Test
    fun sha256_differsForDifferentInput() {
        assertNotEquals(PasswordHash.sha256("a"), PasswordHash.sha256("b"))
    }

    @Test
    fun sha256_matchesKnownVector() {
        assertEquals(
            "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
            PasswordHash.sha256("admin")
        )
    }
}
