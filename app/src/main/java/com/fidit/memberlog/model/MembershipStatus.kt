package com.fidit.memberlog.model

enum class MembershipStatus(val label: String) {
    ACTIVE("Aktivan"),
    INACTIVE("Neaktivan"),
    HONORARY("Počasni");

    companion object {
        fun from(value: String): MembershipStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}
