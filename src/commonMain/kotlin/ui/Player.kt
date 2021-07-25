package ui

import clamp
import com.soywiz.klock.TimeSpan
import com.soywiz.korev.GameButton
import com.soywiz.korev.GameStick
import com.soywiz.korev.Key
import com.soywiz.korge.box2d.body
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.input.gamepad
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import level.LevelMap
import level.Tile
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import physics.Rectangle
import player.PlayerState
import ui.level.TILE_SIZE
import kotlin.math.abs

const val MAX_X_VEL = 2f
const val MAX_X_AIR_VEL = 6.0
const val MAX_Y_VEL = 10.0
const val GRAVITY = 20.0
private const val ACCELERATION = .2f
private const val FRICTION = 1.0

private const val JUMP_VELOCITY = 6f
private const val WALL_JUMP_KICKOFF_VELOCITY = 5f
private const val WALL_JUMP_KICKOFF_VELOCITY_Y = 6f
private const val JUMP_TIME = .1
private const val WALL_JUMP_TIME = .1

private const val DASH_VELOCITY = 10f
private const val DASH_TIME = 150.0

class Player(private val map: LevelMap) : Container() {
    private lateinit var rigidBody: Body
    private var state = PlayerState.FALLING
    private var stateTime = 0.0

    private var goingRight = true
    private var hasDoubleJump = false
    private var grounded = false
    private var touchingWallLeft = false
    private var touchingWallRight = false

    fun init(spawnTile: Tile) {
        position(spawnTile.x * TILE_SIZE, spawnTile.y * TILE_SIZE)
        solidRect(0.9 * TILE_SIZE, 1.5 * TILE_SIZE, Colors.PINK).xy(-TILE_SIZE / 2, -TILE_SIZE)

        addTriggers()

        registerBodyWithFixture(
            type = BodyType.DYNAMIC,
            density = 2,
            friction = 1,
            fixedRotation = true,
            shape = CircleShape(0.225),
            restitution = 0
        )
        this@Player.rigidBody = this.body!!

        setupControls()
        addOnUpdate()
    }

    private fun addOnUpdate() {
        addUpdater { dt ->
            stateTime += dt.milliseconds
            when (state) {
                PlayerState.DASHING -> {
                    if (stateTime > DASH_TIME) {
                        val newState = if (grounded) PlayerState.RUNNING else PlayerState.FALLING
                        setPlayerState(newState)
                    } else {
                        rigidBody.linearVelocityX = if (goingRight) DASH_VELOCITY else -DASH_VELOCITY
                    }
                }
            }
        }
    }

    private fun addTriggers() {
        val groundRect = Rectangle(-0.9f * TILE_SIZE / 2, TILE_SIZE / 2f, 0.9f * TILE_SIZE, .3f * TILE_SIZE)
        Trigger(this, groundRect, map, ::onGroundContact, ::onLeaveGround, true)

        Trigger(
            this,
            Rectangle(TILE_SIZE / 2.7f, TILE_SIZE / 3f, TILE_SIZE / 1.5f, .3f * TILE_SIZE),
            map,
            { touchingWallRight = true },
            { touchingWallRight = false },
            true,
            Colors.RED
        )
        Trigger(
            this,
            Rectangle(-TILE_SIZE * 1f, TILE_SIZE / 3f, TILE_SIZE / 1.5f, .3f * TILE_SIZE),
            map,
            { touchingWallLeft = true },
            { touchingWallLeft = false },
            true,
            Colors.YELLOWGREEN
        )
    }

    private fun setupControls() {
        gamepad {
            down(0, GameButton.BUTTON0) {
                jump()
            }
        }

        keys {
            justDown(Key.SPACE) {
                jump()
            }
            justDown(Key.Z) {
                dash(false)
            }
            justDown(Key.X) {
                dash(true)
            }
        }
        addUpdaterWithViews { views: Views, dt: TimeSpan ->
            var dx = 0f
            val scale = dt.milliseconds.toFloat() / 20
            with(views.input) {
                val buttons = connectedGamepads.firstOrNull()
                val stickAmount = buttons?.get(GameStick.LEFT)?.x ?: 0.0
                when {
                    abs(stickAmount) > .1f -> dx = (stickAmount * ACCELERATION * scale).toFloat()
                    keys[Key.RIGHT] -> dx = ACCELERATION * scale
                    keys[Key.LEFT] -> dx = -ACCELERATION * scale
                }
            }
            if (dx != 0f) goingRight = dx > 0
            rigidBody.linearVelocityX = clamp(rigidBody.linearVelocityX + dx, -MAX_X_VEL, MAX_X_VEL)
        }
    }

    private fun jump() {
        when {
            grounded -> {
                rigidBody.linearVelocityY = -JUMP_VELOCITY
                setPlayerState(PlayerState.JUMPING)
            }
            touchingWallRight -> {
                rigidBody.linearVelocityX = -WALL_JUMP_KICKOFF_VELOCITY
                rigidBody.linearVelocityY = -WALL_JUMP_KICKOFF_VELOCITY_Y
                setPlayerState(PlayerState.JUMPING)
            }
            touchingWallLeft -> {
                rigidBody.linearVelocityX = WALL_JUMP_KICKOFF_VELOCITY
                rigidBody.linearVelocityY = -WALL_JUMP_KICKOFF_VELOCITY_Y
                setPlayerState(PlayerState.JUMPING)
            }
            hasDoubleJump -> {
                hasDoubleJump = false
                rigidBody.linearVelocityY = -JUMP_VELOCITY
                setPlayerState(PlayerState.JUMPING)
            }
        }
    }

    private fun dash(right: Boolean = true) {
        if (state == PlayerState.DASHING) return

        setPlayerState(PlayerState.DASHING)
        if (right) {
            goingRight = true
            rigidBody.linearVelocityX = DASH_VELOCITY
        } else {
            goingRight = false
            rigidBody.linearVelocityX = -DASH_VELOCITY
        }
    }

    private fun setPlayerState(state: PlayerState) {
        if (this.state != state) {
            println("${this.state} -> $state")
            this.state = state
            this.stateTime = 0.0
        }
    }

    private fun onLeaveGround() {
        grounded = false
    }

    private fun onGroundContact() {
        grounded = true
        hasDoubleJump = true
        if (state == PlayerState.FALLING) setPlayerState(PlayerState.RUNNING)
    }

}