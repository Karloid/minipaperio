import org.json.JSONObject

class Bonus(jsonObject: JSONObject) {
    val type = jsonObject.getString("type").toBonusType()
    val pos = jsonObject.getJSONArray("position").toPoint2D()
}
