import org.json.JSONObject

class ActiveBonus(jsonObject: JSONObject) {
    val type = jsonObject.getString("type").toBonusType()
    val points = jsonObject.getInt("ticks")
}

fun String.toBonusType(): BonusType {
    return BonusType.fromString(this)
}
