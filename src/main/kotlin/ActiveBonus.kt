import org.json.JSONObject

class ActiveBonus(jsonObject: JSONObject) {
   val type = jsonObject.getString("type").toBonusType()
}

private fun String.toBonusType(): BonusType {
    return BonusType.fromString(this)
}
