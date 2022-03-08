package minesweeper

class MineSweeperGame(val numOfMines: Int = 10, val fieldWidth: Int = 10, val fieldHeight: Int = 10) {

    private val field = GameField(numOfMines, fieldWidth, fieldHeight)
    var isRunning = true
        private set

    init {
        display()
        ask()
    }

    private inner class GameField(val numOfMines: Int, val fieldWidth: Int, val fieldHeight: Int) {
        val CLOSED: Int = 0
        val OPENED: Int = 1
        val MINED: Int = 2
        val FIRED: Int = 3
        val MARKED: Int = 4
        private val markedCells = mutableSetOf<Cell>()
        private val minedCells = mutableSetOf<Cell>()

        val cells = List(fieldHeight) { List(fieldWidth) { Cell() } }
        val positions = mutableSetOf<Int>()

        init {
            while (positions.size < numOfMines) {
                positions.add((0..fieldWidth * fieldHeight - 1).random())
            }

//            println(positions)
            positions.forEach { addMine(it / fieldHeight, it % fieldWidth) }


        }

        private inner class Cell(state: Int = CLOSED) {
            var state = state
                private set
            private var nearHowManyMines = 0

            fun open(): Boolean {
                if (state == CLOSED) {
                    state = OPENED
                    return true
                } else if (state == MINED) {
                    state = FIRED
                    return true
                }
                return false
            }

            fun addNearMine() {
                nearHowManyMines++
            }

            override fun toString(): String {
                return when (state) {
                    OPENED -> ""
                    CLOSED -> if (nearHowManyMines > 0) "$nearHowManyMines" else "."
                    MINED -> "."
                    FIRED -> "@"
                    MARKED -> "*"
                    else -> "!"
                }
            }

            fun putMine(): Boolean {
                if (state == CLOSED) {
                    state = MINED
                    return true
                }
                return false
            }

            fun toggleMark(): Boolean {
                return if (state != MARKED) {
                    state = MARKED
                    true
                } else {
                    state = CLOSED
                    false
                }
            }

            fun isNearMine(): Boolean {
                return nearHowManyMines > 0 && state != MINED
            }


        }


        fun addMine(row: Int, col: Int) {
            cells[row][col].putMine()
            minedCells.add(cells[row][col])
            val neighbors = getNeighboursOf(row, col)
            neighbors.forEach {
                it.addNearMine()
            }
        }

        private fun getNeighboursOf(row: Int, col: Int): List<Cell> {
            val list = mutableListOf<Cell>()
            if (row > 0) {
                list.add(cells[row - 1][col])
                if (col > 0) {
                    list.add(cells[row - 1][col - 1])
                }
                if (col < fieldWidth - 1) {
                    list.add(cells[row - 1][col + 1])
                }
            }
            if (row < fieldHeight - 1) {
                list.add(cells[row + 1][col])
                if (col > 0) {
                    list.add(cells[row + 1][col - 1])
                }
                if (col < fieldWidth - 1) {
                    list.add(cells[row + 1][col + 1])
                }
            }
            if (col > 0) {
                list.add(cells[row][col - 1])
            }
            if (col < fieldWidth - 1) {
                list.add(cells[row][col + 1])
            }
            return list
        }

        fun draw() {
            val maxDigits = fieldHeight.toString().length
            println("${" ".repeat(maxDigits)}│${(1..fieldWidth).joinToString("")}│")
            println("${"—".repeat(maxDigits)}│${"—".repeat(fieldWidth)}│")
            cells.forEachIndexed { index, row ->
                println("${index + 1}│${row.joinToString("")}│")
            }
            println("${"—".repeat(maxDigits)}│${"—".repeat(fieldWidth)}│")

        }

        fun toggleMarkCell(col: Int, row: Int) {
            val marked = cells[row][col].toggleMark()
            if (marked) {
                markedCells.add(cells[row][col])
            } else {
                markedCells.remove(cells[row][col])
            }
        }

        fun isCellNearMine(col: Int, row: Int): Boolean {
            return cells[row][col].isNearMine()
        }

        fun checkWin(): Boolean {
            return minedCells == markedCells
        }
    }

    private fun ask() {
        print("Set/delete mine marks (x and y coordinates): ")

    }


    fun display() {
        field.draw()
    }


    fun play(col: Int, row: Int): Boolean {

        if (field.isCellNearMine(col, row)) {
            println("There is a number here!")

        } else {

            field.toggleMarkCell(col, row)
            display()
            if (field.checkWin()) {
                println("Congratulations! You found all the mines!")
                isRunning = false
                return isRunning
            }

        }
        ask()



        return isRunning
    }
}

fun main() {
    print("How many mines do you want on the field? >")
    val howMany = readln().toInt()
    val game = MineSweeperGame(numOfMines = howMany, 9, 9)

    while (game.isRunning) {

        val (col, row) = readln().split(" ").map { it.toInt() }
        game.play(col - 1, row - 1)
    }

}
