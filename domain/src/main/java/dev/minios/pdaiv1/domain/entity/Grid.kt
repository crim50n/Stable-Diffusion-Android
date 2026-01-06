package dev.minios.pdaiv1.domain.entity

enum class Grid(val size: Int) {
    Fixed1(1),
    Fixed2(2),
    Fixed3(3),
    Fixed4(4),
    Fixed5(5),
    Fixed6(6);

    companion object {
        fun fromSize(size: Int): Grid = entries.find { it.size == size } ?: Fixed3

        fun zoomIn(current: Grid): Grid {
            val index = entries.indexOf(current)
            return if (index > 0) entries[index - 1] else current
        }

        fun zoomOut(current: Grid): Grid {
            val index = entries.indexOf(current)
            return if (index < entries.lastIndex) entries[index + 1] else current
        }
    }
}
