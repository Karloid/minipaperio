enum class BonusType(val raw: String) {
    SAW("saw")
    ;

    companion object {
        fun fromString(s: String): BonusType {
            return cValues.firstOrNull { it.raw == s } ?: SAW
        }

        val cValues = values()
    }
}

//“saw”, “s”, “n”
