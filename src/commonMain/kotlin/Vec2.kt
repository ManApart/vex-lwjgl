import com.soywiz.korma.geom.Point

class Vec2(val x: Float = 0f, val y: Float = 0f) {
    constructor(x: Int = 0, y: Int = 0) : this(x.toFloat(), y.toFloat())

    operator fun div(scalar: Double) = Vec2((x / scalar).toFloat(), (y / scalar).toFloat())
}


fun Point.toVector(): Vec2 {
    return Vec2(x.toFloat(), y.toFloat())
}