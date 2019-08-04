class Point2D {
    var x: Int = 0
    var y: Int = 0

    val isNull: Boolean
        get() = this.x or this.y == 0

    @JvmOverloads
    constructor(x: Int = 0, y: Int = x) {
        this.x = x
        this.y = y
    }

    constructor(vect: Point2D) {
        this.x = vect.x
        this.y = vect.y
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || !(other is Point2D)) {
            return false
        }
        return this.x == other.x && this.y == other.y
    }

    operator fun set(x: Int, y: Int): Point2D {
        this.x = x
        this.y = y
        return this
    }

    fun set(a: Point2D): Point2D {
        this.x = a.x
        this.y = a.y
        return this
    }

    fun add(a: Point2D): Point2D {
        this.x += a.x
        this.y += a.y
        return this
    }

    fun sub(a: Point2D): Point2D {
        this.x -= a.x
        this.y -= a.y
        return this
    }

    fun mult(a: Int): Point2D {
        this.x *= a
        this.y *= a
        return this
    }

    operator fun div(a: Int): Point2D {
        this.x /= a
        this.y /= a
        return this
    }

    fun negate(): Point2D {
        this.x = -this.x
        this.y = -this.y
        return this
    }

    fun normalize(): Point2D {
        if (isNull)
            return this

        val absx = Math.abs(this.x)
        val absy = Math.abs(this.y)
        if (absx > absy) {
            this.x /= absx
            this.y = 0
        } else if (absx < absy) {
            this.x = 0
            this.y /= absy
        } else {
            this.x /= absx
            this.y /= absy
        }
        return this
    }

    fun manhattanDistance(): Int {
        return Math.abs(x) + Math.abs(y)
    }

    fun manhattanDistance(a: Point2D): Int {
        return Math.abs(this.x - a.x) + Math.abs(this.y - a.y)
    }

    fun tchebychevDistance(): Int {
        return Math.max(x, y)
    }

    fun tchebychevDistance(a: Point2D): Int {
        return Math.max(Math.abs(this.x - a.x), Math.abs(this.y - a.y))
    }

    fun euclidianDistance2(): Double {
        return (x * x + y * y).toDouble()
    }

    fun euclidianDistance2(a: Point2D): Double {
        return Math.pow((this.x - a.x).toDouble(), 2.0) + Math.pow((this.y - a.y).toDouble(), 2.0)
    }

    fun eucDist(): Double {
        return Math.sqrt(eucDist())
    }

    fun eucDist(a: Point2D): Double {
        return Math.sqrt(euclidianDistance2(a))
    }

    override fun toString(): String {
        return "[$x:$y]"
    }

    fun dirTo(pos: Point2D): Direction {
        return when {
            pos.x - 1 == x -> Direction.RIGHT
            pos.x + 1 == x -> Direction.LEFT
            pos.y + 1 == y -> Direction.DOWN
            pos.y - 1 == y -> Direction.UP
            else -> {
                MainKt.myDebugLog("unable to find dir for $this $pos")
                Direction.UP
            }
        }
    }

    fun applyDir(direction: Direction): Point2D {
        return this.add(when (direction) {
            Direction.LEFT -> LEFT
            Direction.UP -> UP
            Direction.RIGHT -> RIGHT
            Direction.DOWN -> DOWN
        })
    }

    companion object {

        fun add(a: Point2D, b: Point2D): Point2D {
            return Point2D(a).add(b)
        }

        fun sub(a: Point2D, b: Point2D): Point2D {
            return Point2D(a).sub(b)
        }

        fun mult(a: Point2D, b: Int): Point2D {
            return Point2D(a).mult(b)
        }

        fun div(a: Point2D, b: Int): Point2D {
            return Point2D(a).div(b)
        }

        val UP = Point2D(0, 1)
        val RIGHT = Point2D(1, 0)
        val DOWN = Point2D(0, -1)
        val LEFT = Point2D(-1, 0)
    }

}
