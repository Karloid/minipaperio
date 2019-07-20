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



