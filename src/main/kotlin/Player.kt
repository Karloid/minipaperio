import org.json.JSONArray
import org.json.JSONObject

class Player(id: String, jsonObject: JSONObject) {
    var score: Int
    val position: Point2D
    val lines: List<Point2D>
    val activeBonuses: List<ActiveBonus>
    val direction: Direction
    val territory: List<Point2D>

    init {
        score = jsonObject.getInt("score")
        position = jsonObject.getJSONArray("position").toPoint2D()
        lines = jsonObject.getJSONArray("lines").toPoint2DList()
        activeBonuses = jsonObject.getJSONArray("bonuses").toActiveBonusesList()
        direction = jsonObject.getString("direction").toDirection()
        territory = jsonObject.getJSONArray("territory").toPoint2DList()
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

private fun JSONArray.toPoint2D(): Point2D {
    return Point2D(getInt(0), getInt(1))
}
