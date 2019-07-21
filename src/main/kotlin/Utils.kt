val TWO_PI: Float = (Math.PI * 2f).toFloat()
val HALF_PI: Float = (Math.PI / 2f).toFloat()

fun normalizeAngle(angle: Float): Float {
    // reduce the angle
    var result = angle % TWO_PI

    //force it to be the positive remainder, so that 0 <= angle < 360
    result = (result + TWO_PI) % TWO_PI

    //force into the minimum absolute value residue class, so that-180 < angle <= 180
    if (result > Math.PI) {
        result -= TWO_PI
    }
    return result
}

        fun f(v: Double): String {
            return String.format("%.2f", v)
        }

        fun f(v: Float): String {
            return String.format("%.2f", v)
        }

        fun Number.f(): String {
            return String.format("%.2f", this)
        }

fun Float.asPi(): Number {
    return this / Math.PI
}

fun Double.asPi(): Number {
    return this / Math.PI
}

fun <E> List<E>.fori(function: (E) -> Unit) {
    var i = 0
    while (i < size) {
        function(get(i))
        i++;
    }
}



fun <E> List<E>?.isNullOrEmpty(): Boolean {
    if (this == null) {
        return true
    }

    return this.isEmpty()
}

@Suppress("NOTHING_TO_INLINE")
inline fun String?.nonEmptyOr(s: String) = if (this.isNullOrEmpty()) {
    s
} else {
    this
}

inline fun <T> Boolean?.then(function: () -> T): T? {
    return if (this != null && this) {
        function()
    } else {
        null
    }
}

inline fun <T> T?.elze(function: () -> T): T {
    return if (this == null) {
        function()
    } else {
        this
    }
}


inline fun <T> Boolean?.then(mainFun: () -> T, elseFun: () -> T): T {
    return if (this == true) {
        mainFun()
    } else {
        elseFun()
    }
}

inline fun ignoreTryCatch(function: () -> Unit) {
    try {
        function()
    } catch (e: Throwable) {
        //ignore
    }
}



