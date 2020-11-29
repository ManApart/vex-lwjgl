package player

import input.Controller
import physics.Rectangle
import worldMap.Exit

class WorldMapPlayer(origin: Exit) {
    val currentExit = origin
    val goalExit = origin
    val bounds = Rectangle(origin.bounds.x, origin.bounds.y, 0.6f, 0.8f)


    fun update(deltaTime: Float) {
        processKeys()
//        updateState(deltaTime)
//        body.update(deltaTime, xMaxVelocity(), yMaxVelocity())

    }

    private fun processKeys() {
        if (Controller.jump.isFirstPressed()) {
            if (currentExit == goalExit){
                Vex.enterLevel(currentExit)
            }
        }
    }


}