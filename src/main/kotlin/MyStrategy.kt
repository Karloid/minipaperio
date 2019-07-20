//TODO plhillmap bus ifs for jump in the middle

//TODO keep best angle for buggy too
//test

class MyStrategy : Strategy {

    private var debugMessage: String = ""

    private lateinit var w: World
    private lateinit var move: Move


    val ON_AIR_PAUSE = 0

    var tick = 0

    var matchCounter = 0


    private lateinit var m: MatchConfig

    private var maxMinButtonY: Float = -1f
    private var desiredAngleForBus: Float = 0f

    private var isBus: Boolean = false

    private var s = State()

    override fun onMatchStarted(matchConfig: MatchConfig) {
        tick = 0
        matchCounter++
        this.m = matchConfig

        isBus = m.carId == 2

        debugMessage += "\n" + m.buttonPoly

        s = State()
    }

    override fun onNextTick(world: World, move: Move) {
        pretick(move, world)

        simpleStrategy()
    }


    private fun simpleStrategy() {

        //move.d(" map_id ${match.mapId}  car_id ${match.carId} tick ${tick} my side ${getMySide()}")

        val myCar = w.myCar
        s.myCarAngle = myCar.angle

        printCarInfo("my", myCar)
        printCarInfo("enemy", w.enemyCar)

       // move.d("enemyXY ${w.enemyCar.x.f()} - ${w.enemyCar.y.f()}")


        when (m.carType) {
            CarType.Bus -> {

                when (m.mapType) {
                    MapType.PillMap -> {
                    }
                    MapType.PillHubbleMap -> {
                        doBusPillHubbleMap { doBusStart() }
                        return
                    }
                    MapType.PillHillMap -> {
                        doPillHillMapStrat { doBusStart() }
                        return
                    }
                    MapType.PillCarcassMap -> {
                        doPillCarcassJump { doBusStart() }
                        return
                    }
                    MapType.IslandMap -> {
                    }
                    MapType.IslandHoleMap -> {
                    }
                }
                doBusStart()
                return
            }
            CarType.Buggy, CarType.SquareWheelsBuggy -> {

                when (m.mapType) {
                    MapType.PillMap -> {
                    }
                    MapType.PillHubbleMap -> {
                    }
                    MapType.PillHillMap -> {
                        doPillHillMapStrat { doSimpleAngleStrat(if (m.carType == CarType.Buggy) 1f else 0.7f) }
                        return
                    }
                    MapType.PillCarcassMap -> {
                        if (m.carType != CarType.SquareWheelsBuggy) {
                            doPillCarcassJump {
                                s.allowedToAttack = false
                                doSimpleAngleStrat(1f)
                            }
                            return
                        } else {
                            doPillCarcassJumpSquare()
                            return
                        }
                    }
                    MapType.IslandMap -> {
                        @Suppress("NON_EXHAUSTIVE_WHEN")
                        when (m.carType) {
                            CarType.Buggy -> {
                                doRushSmartIsland2()
                                return
                            }
                            CarType.SquareWheelsBuggy -> {
                                doRushIsland()
                                return
                            }
                        }

                    }
                    MapType.IslandHoleMap -> {
                    }
                }

                doSimpleAngleStrat(0.7f)
            }
        }
    }

    fun printCarInfo(prefix: String, myCar: Car) {
        move.d("$tick ${prefix}XY ${myCar.x.f()} - ${myCar.y.f()} a: ${s.myCarAngle.f()} as PI: ${myCar.angle.asPi().f()} " +
                "angleSpeed ${myCar.angleSpeed} speed ${myCar.speed}")
    }

    private fun doRushSmartIsland2() {

        if (tick < 60 || w.myCar.point().distance(w.enemyCar.point()) > 300 && Math.abs(w.myCar.angle) > 0.05) {
            move.set(0)
            return
        }

        if (Math.abs(w.myCar.angle) > 0.75) {
            doSimpleAngleStrat(0.7f)
            return
        }

        if ((w.myCar.side == 1 && w.myCar.x > w.enemyCar.x)
        || (w.myCar.side == -1 && w.myCar.x < w.enemyCar.x)) {
            doSimpleAngleStrat(0.7f)
            return
        }

        val cmd = 1 * w.myCar.side

        move.set(cmd)
    }

    private fun doRushSmartIsland() {
        s.needRush =  s.needRush || w.myCar.point().distance(w.enemyCar.point()) < 300
                || (tick > 80 && w.enemyCar.speed.length() > 1 && Math.abs(w.enemyCar.angle) < 0.1)

        if (tick < 200 && !s.needRush) {
            move.set(0)
            return
        }

        if (Math.abs(w.myCar.angle) > 0.75) {
            doSimpleAngleStrat(0.7f)
        }

        val cmd = 1 * w.myCar.side

        move.set(cmd)
    }

    private fun doPillCarcassJumpSquare() {
        if (tick > 200) {
            move.set(0)
            return
        }

        var cmd = -1 * w.myCar.side
        if (tick < 5) {
            move.set(cmd)
            return
        } else if (tick in 85..95 /*|| tick in 95..100*/) {
            move.set(cmd * -1)
            return
        } else if (tick > 150) {
            move.set(cmd * -1)
            return
        } else if (tick > 50) {
            move.set(cmd)
            return
        }

        move.set(0)
    }
    
    private fun doBusPillHubbleMap(onSuccess: () -> Unit) {
        if (tick < 30) {
            move.set(0)
        } else if (tick < 155) {
            var cmd = -1
            cmd *= w.myCar.side

            move.set(cmd)
        } else if (tick < 200) {
            doSimpleAngleStrat(1f)
        } else {
            onSuccess()
        }
    }

    private fun doRushIsland() {
        val x = w.myCar.getMirroredX()
        if (x < MAP_WIDTH / 2) {
            doSimpleAngleStrat(0.11f)
            return
        }
        val abs = Math.abs(w.myCar.angle)
        doSimpleAngleStrat(Math.max(0.7f, Math.min(abs, 1f)))
        return
        //var desiredAngle = (HALF_PI * 0.2) * getMySide()

        var cmd = 1 * getMySide()// main direction

        /* if (abs(w.myCar.angleSpeed) > 0.5) {
             cmd *= -1
         }*/


        val isCloseToPerfectAngle = Math.abs(0) < HALF_PI / 2


        move.set(cmd)
    }

    private fun doPillCarcassJump(onComplete: () -> Unit) {
        val x = w.myCar.getMirroredX()
        var cmd: Int
        val stopOnX = if (isBus) 400 else if (m.carType == CarType.Buggy) 322 else 300
        s.reach120x = isBus || s.reach120x || x > 345
        s.reach440x = (w.myCar.y > 420 && x > stopOnX && s.reach120x) || s.reach440x
        if (tick < 30) {
            cmd = 0
        } else if (s.reach440x) {
            run(onComplete)
            return
        } else if (s.reach120x) {
            cmd = -1
        } else {
            cmd = 1
        }

        move.set(cmd * w.myCar.side)
    }

    private fun doPillHillMapStrat(afterReach: () -> Any) {
        val x = w.myCar.getMirroredX()
        var cmd = 1
        val accelerationX = if (isBus) 120 else 270
        val stopOnX =  if (isBus) 444 else 400

        s.reach120x = x < accelerationX || s.reach120x

        s.reach440x = (s.reach120x && x > stopOnX) || s.reach440x
        if (tick < 30) {
            cmd = 0
        } else if (s.reach440x) {
            s.allowedToAttack = false
            run(afterReach)
            return
        } else if (s.reach120x) {
            cmd = 1
        } else {
            cmd = -1
        }

        move.set(cmd * w.myCar.side)

    }

    fun doSimpleAngleStrat(angleKoeff: Float) {
        var desiredAngle = (HALF_PI * angleKoeff) * getMySide()

        var cmd = 1

        val delta = s.myCarAngle - desiredAngle
        if (delta > 0) {
            cmd *= -1
        }

        val isCloseToPerfectAngle = Math.abs(delta) < HALF_PI / 2

        if (tick < ON_AIR_PAUSE && isCloseToPerfectAngle && tick % 5 != 0 && !isBus) {
            cmd *= -1
        }



        if (tick > 50 && tick % 5 != 0 && !isBus && s.allowedToAttack) {
            val myX = w.myCar.x
            val enemyX = w.enemyCar.x

            var leftCmd = -1
            var rightCmd = 1
            if (Math.abs(s.myCarAngle) > 1) {  //whut
                //move.d("strange switch on")
                rightCmd = leftCmd
                leftCmd = 1
            }
            cmd = if (myX > enemyX) leftCmd else rightCmd
        }

        move.set(cmd)
    }

    fun doBusStart() {
        val myCarAngle = w.myCar.angle

        var desiredAngle = (HALF_PI * 1) * getMySide()
        val minButtonY = getMinButtonY(w.myCar)
        if (minButtonY > maxMinButtonY) {
            maxMinButtonY = minButtonY
            desiredAngleForBus = myCarAngle
            move.d("found best angle ${myCarAngle.f()} which y ${maxMinButtonY}")
        }

        if (tick > 80) {
            // move.d("set desiredAngle from ${desiredAngle.f()} to ${desiredAngleForBus}")
            desiredAngle = desiredAngleForBus

            var cmd = 1

            val delta = myCarAngle - desiredAngle
            if (delta > 0) {
                cmd *= -1
            }

            if (Math.abs(w.myCar.angleSpeed) > 0.00436248
                    && ((cmd < 0 && w.myCar.angleSpeed < 0) || (cmd > 0 && w.myCar.angleSpeed > 0))) {
                cmd *= -1
            }

            move.set(cmd)

            return
        }
        var cmd = 1

        val delta = myCarAngle - desiredAngle
        if (delta > 0) {
            cmd *= -1
        }
        move.set(cmd)
    }

    private fun getMinButtonY(myCar: Car): Float {
        return m.buttonPoly.map {
            var point = it
            if (getMySide() == -1){
                point = Point2D(-it.x, it.y)
            }
            val rotated = point.rotate(myCar.angle.toDouble())
            rotated.y
        }.min()!!.toFloat()
    }

    private fun getRotatedButtonPoly(myCar: Car): List<Point2D> {

        return m.buttonPoly.map { it.rotate(myCar.angle.toDouble()) }
    }


    fun pretick(move: Move, world: World) {
        this.move = move;
        if (tick != 0) {
            world.processPre(this.w)
        }
        this.w = world;
        tick++

        if (!debugMessage.isEmpty()) {
            move.d(debugMessage)
            debugMessage = ""
        }
    }

    fun getMySide() = w.myCar.side


    override fun onParsingError(message: String) {
        debugMessage = message
    }

}

class State {

    //pillHill
    var reach120x: Boolean = false
    var reach440x: Boolean = false
    var myCarAngle: Float = 0f
    var allowedToAttack: Boolean = true
    var needRush: Boolean = false
}
