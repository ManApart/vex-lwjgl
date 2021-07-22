package level

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.RGBA


class LevelMapBuilder {
    suspend fun createMap(template: LevelTemplate): LevelMap {
        return LevelMap(template.id, loadBinary(Resources.getLevel(template)))
    }

    private fun loadBinary(image: Bitmap): Array<Array<Tile>> {
        val map = Array(image.width) { Array(image.height) { DEFAULT_TILE } }
        for (x in 0 until image.width) {
            val maxY = map[x].size
            for (y in 0 until image.height) {
//                val type = fromInt(image.getRGB(x, y))
                val tileY = maxY - 1 - y
                map[x][tileY] = buildTile(x, tileY, image.getRgba(x, y))
            }
        }
        return map
    }

    private fun buildTile(x: Int, y: Int, color: RGBA) :Tile {
        return Tile(from(color), x, y, color.b)
    }

}