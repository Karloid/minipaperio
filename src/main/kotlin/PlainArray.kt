class PlainArray<T> internal constructor(val cellsWidth: Int, val cellsHeight: Int, init: (Int) -> T) {

    val array: MutableList<T>

    init {
        array = MutableList(this.cellsWidth * cellsHeight, init)
    }

    internal operator fun get(x: Int, y: Int): T? {
        return if (!inBounds(x, y)) {
            null
        } else {
            array[y * cellsWidth + x]
        }
    }

    inline fun getFast(x: Int, y: Int): T {
        return array[y * cellsWidth + x]
    }

    internal fun add(x: Int, y: Int, `val`: T) {
        if (!inBounds(x, y)) {
            return
        }
        array[y * cellsWidth + x] = `val`
    }

    internal operator fun set(x: Int, y: Int, `val`: T) {
        if (!inBounds(x, y)) {
            return
        }
        array[y * cellsWidth + x] = `val`
    }

    fun inBounds(x: Int, y: Int): Boolean {
        return !(x < 0 || x >= cellsWidth || y < 0 || y >= cellsHeight)
    }

    fun fori(block: (x: Int, y: Int, v: T) -> Unit) {
        for (y in 0 until cellsHeight) {
            for (x in 0 until cellsWidth) {
                block(x, y, getFast(x, y))
            }
        }
    }
}
