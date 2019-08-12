import org.json.JSONObject

class Bonus(jsonObject: JSONObject) {
    val type = jsonObject.getString("type").toBonusType()
    val pos = jsonObject.getJSONArray("position").toPoint2D()
    override fun toString(): String {
        return "Bonus(type=$type, pos=$pos)"
    }
}
