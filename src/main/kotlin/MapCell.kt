class MapCell {
    lateinit var pos: Point2D

    var lines: Player? = null
    var territory: Player? = null
    var bonus: ActiveBonus? = null
}
