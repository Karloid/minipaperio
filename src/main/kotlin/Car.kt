import org.json.JSONArray

class Car(carParam: JSONArray, private val world: World) {

    var side: Int = 1 // слева = +1, справа = -1

    var x: Float = 0f
    var y: Float = 0f
    var angle: Float = 0f

    var wheel = WheelPair()

    var speed: Point2D = Point2D(0.0, 0.0)

    var angleSpeed: Float = 0f


    fun getMirroredX(): Float {
        if (side == 1) {
            return x
        }

        return MAP_WIDTH - x
    }

    fun point(): Point2D {
        return Point2D(x.toDouble(), y.toDouble())
    }



    fun processPre(car: Car) {
        speed = point().sub(car.point())
        angleSpeed = angle - car.angle;
    }



    init {
        val pos = carParam.getJSONArray(0)
        x = pos.getFloat(0)
        y = pos.getFloat(1)

        angle = normalizeAngle(carParam.getFloat(1))
        side = carParam.getInt(2)

        wheel.rear = Wheel(carParam.getJSONArray(3))
        wheel.front = Wheel(carParam.getJSONArray(4))
    }


    inner class WheelPair {
        var rear: Wheel? = null
        var front: Wheel? = null
    }

    inner class Wheel(wheelParam: JSONArray) {
        var x: Float = 0f
        var y: Float = 0f
        var angle: Float = 0f

        init {
            x = wheelParam.getFloat(0)
            y = wheelParam.getFloat(1)
            angle = wheelParam.getFloat(2)
        }
    }
}