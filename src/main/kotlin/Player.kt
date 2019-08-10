import org.json.JSONArray
import org.json.JSONObject

class Player(val id: String, jsonObject: JSONObject) {
    var score: Int
    val pos: Point2D
    val lines: List<Point2D>
    val activeBonuses: List<ActiveBonus>
    val direction: Direction
    val territory: List<Point2D>

    init {
        score = jsonObject.getInt("score")
        pos = jsonObject.getJSONArray("position").toPoint2D()
        lines = jsonObject.getJSONArray("lines").toPoint2DList()
        activeBonuses = jsonObject.getJSONArray("bonuses").toActiveBonusesList()
        direction = (jsonObject.isNull("direction").then { "left" } ?: jsonObject.getString("direction")).toDirection()
        territory = jsonObject.getJSONArray("territory").toPoint2DList()
    }

    override fun toString(): String {
        return "Player(id='$id', pos=$pos, direction=$direction)"
    }
}

private fun String.toDirection(): Direction {
    return Direction.fromString(this)
}

private fun JSONArray.toActiveBonusesList(): List<ActiveBonus> {
    return List(this.count()) { ActiveBonus(getJSONObject(it)) }
}

private fun JSONArray.toPoint2DList(): List<Point2D> {
    return List(this.count()) { getJSONArray(it).toPoint2D() }
}

fun JSONArray.toPoint2D(): Point2D {
    return Point2D(getInt(0) / cell_width, getInt(1) / cell_width)
}
