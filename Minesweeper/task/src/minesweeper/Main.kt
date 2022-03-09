package minesweeper

class MineSweeperGame(val numOfMines: Int = 10, val fieldWidth: Int = 10, val fieldHeight: Int = 10) {

    private val field = GameField(numOfMines, fieldWidth, fieldHeight)
    var isRunning = true
        private set
    private var isFirstFreeCommand = true

    init {
        display()
        ask()
    }

    private class GameField(val numOfMines: Int, val fieldWidth: Int, val fieldHeight: Int) {

        private val markedCells = mutableSetOf<Cell>()
        private val minedCells = mutableSetOf<Cell>()

        val cells = List(fieldHeight) { row -> List(fieldWidth) { col -> Cell(row, col) } }
        val positions = mutableSetOf<Int>()

        init {
            while (positions.size < numOfMines) {
                positions.add((0..fieldWidth * fieldHeight - 1).random())
            }

//            println(positions)
            positions.forEach { addMine(it / fieldHeight, it % fieldWidth) }

            minedCells.forEach {  println(it.summary()) }

        }

        private class Cell(val row: Int, val col: Int) {
            val cellId: Int

            enum class OpenState { OPENED, CLOSED }
            enum class MarkState { MARKED, UNMARKED }
            enum class MineState { MINED, UNMINED, FIRED }

            companion object {
                val CLOSED: Int = 0
                val OPENED: Int = 1
                val MINED: Int = 2
                val FIRED: Int = 3
                val MARKED: Int = 4

                var id = 0
            }

            init {
                cellId = id++
            }

            var openState = OpenState.CLOSED
                private set
            var markState = MarkState.UNMARKED
                private set
            var mineState = MineState.UNMINED
                private set


            private var nearHowManyMines = 0
            private var nearHowManyMarked = 0

            fun open() {
                if (mineState == MineState.MINED) {
                    mineState = MineState.FIRED
//                    return true
                }
                if (openState == OpenState.CLOSED) {
                    openState = OpenState.OPENED
//                    return true
                }
//                return false
            }

            fun addNearMine() {
                nearHowManyMines++
            }
            fun removeNearMine() {
                nearHowManyMines--
            }
            fun addNearMarked() {
                nearHowManyMarked++
            }

            fun removeNearMarked() {
                nearHowManyMarked--
            }

            override fun toString(): String {
                return when (openState) {
                    OpenState.OPENED -> {
                        when (mineState) {
                            MineState.FIRED -> "X"
                            else -> when (isNearMine()) {
                                true -> nearHowManyMines.toString()
                                false -> "/"
                            }
                        }
//                        if (nearHowManyMines > 0) nearHowManyMines.toString() else "/"
                    }

                    else -> when (markState) {
                        MarkState.MARKED -> "*"
                        else -> "."
                    }
//                    CLOSED, MINED -> "."
//                    FIRED -> "X"
//                    MARKED -> "*"
//                    else -> "!"
                }
            }

            fun summary(): String {
                return "row:${row + 1} ,col:${col + 1}, nearHowManyMarked:$nearHowManyMarked, nearHowManyMine:$nearHowManyMines"
            }

            fun putMine() {
//                if (state == CLOSED) {
//                    state = MINED
//                    return true
//                }
//                return false
                mineState = MineState.MINED
            }
            fun removeMine() {
                mineState = MineState.UNMINED
            }
            fun toggleMark(): Boolean {
                return if (markState == MarkState.UNMARKED) {
                    markState = MarkState.MARKED
                    true
                } else {
                    markState = MarkState.UNMARKED
                    false
                }
            }

            fun isNearMine(): Boolean {
                //state != MINED &&
                return nearHowManyMines > 0 //&& state == OPENED
            }

            fun isNearMarked(): Boolean {
                return nearHowManyMarked > 0
            }

            fun isFired(): Boolean {
                return mineState == MineState.FIRED
            }

            fun isClosed(): Boolean {
                return openState == OpenState.CLOSED
            }

            fun isMinedOrMarked(): Boolean {
                return mineState == MineState.MINED || markState == MarkState.MARKED
            }

            fun isMined(): Boolean {
                return mineState == MineState.MINED //|| markState == MarkState.MARKED
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Cell

                if (cellId != other.cellId) return false

                return true
            }

            override fun hashCode(): Int {
                return cellId
            }

            fun isOpen(): Boolean {
                return openState == OpenState.OPENED
            }

        }

        fun MoveMine(col: Int, row: Int) {
            removeMine( row,col)
            var r = (0..fieldWidth * fieldHeight - 1).random()
            while (r in positions) {
                r = (0..fieldWidth * fieldHeight - 1).random()
            }
            positions.add(r)
            println("new mine position: $r")
            addMine(r / fieldHeight, r % fieldWidth)
        }

        fun removeMine(row: Int, col: Int) {

            cells[row][col].removeMine()
            minedCells.remove(cells[row][col])
            val neighbors = getNeighboursOf(row, col)
            neighbors.forEach {
                it.removeNearMine()
            }
            positions.remove((row * col-1) + col)
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

        private fun checkAndAddToSet(set: MutableSet<Cell>, c: Cell) {
            if (!c.isMined() && c.isClosed())//&& !c.isNearMarked()
                set.add(c)
        }

        private fun getNeighborsToBeOpened(row: Int, col: Int): Set<Cell> {
            val set = mutableSetOf<Cell>()
            if (row > 0) {
                val c = cells[row - 1][col]
                checkAndAddToSet(set, c)
                if (col > 0) {
                    val c = cells[row - 1][col - 1]
                    checkAndAddToSet(set, c)
                }
                if (col < fieldWidth - 1) {
                    val c = cells[row - 1][col + 1]
                    checkAndAddToSet(set, c)
                }
            }
            if (row < fieldHeight - 1) {
                val c = cells[row + 1][col]
                checkAndAddToSet(set, c)
                if (col > 0) {
                    val c = cells[row + 1][col - 1]
                    checkAndAddToSet(set, c)
                }
                if (col < fieldWidth - 1) {
                    val c = cells[row + 1][col + 1]
                    checkAndAddToSet(set, c)
                }
            }
            if (col > 0) {
                val c = cells[row][col - 1]
                checkAndAddToSet(set, c)
            }
            if (col < fieldWidth - 1) {
                val c = cells[row][col + 1]
                checkAndAddToSet(set, c)
            }
            return set
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
                val neighbors = getNeighboursOf(row, col)
//                println("near marked")
                neighbors.forEach {

                    it.addNearMarked()
//                    println(it.summary())
                }
            } else {
                markedCells.remove(cells[row][col])
                val neighbors = getNeighboursOf(row, col)
//                println("near unmarked")
                neighbors.forEach {

                    it.removeNearMarked()
//                    println(it.summary())
                }
            }
        }

        fun playerOpenCell(col: Int, row: Int) {
            if (cells[row][col].isClosed()) {
                cells[row][col].open()
                if (!cells[row][col].isFired()) {
                    val set = getNeighborsToBeOpened(row, col)

                    set.forEach {
//                        println(it.summary())
                        openCell(it.col, it.row)
                    }
                }
            }
        }

        private fun openCell(col: Int, row: Int) {
            cells[row][col].open()
            val set = getNeighborsToBeOpened(row, col)
            set.forEach {
//                println(it.summary())
                openCell(it.col, it.row)
            }
        }

        fun OpenAllMines() {
            minedCells.forEach { it.open() }
        }

        fun isFiredCell(col: Int, row: Int): Boolean {
            return cells[row][col].isFired()
        }

        fun isOpenAndNearMine(col: Int, row: Int): Boolean {
            return cells[row][col].isNearMine() && cells[row][col].isOpen()
        }

        fun checkWin(): Boolean {
            return minedCells == markedCells
        }

        fun mineWillBeFired(col: Int, row: Int): Boolean {
            return cells[row][col].isMined()
        }
    }

    private fun ask() {
        print("Set/unset mine marks or claim a cell as free: ")

    }


    fun display() {
        field.draw()
    }


    fun play(col: Int, row: Int, command: String): Boolean {
        if (command == "mine") {
            if (field.isOpenAndNearMine(col, row)) {
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
        } else { //free command

            if (isFirstFreeCommand) {
                if (field.mineWillBeFired(col, row)) {
                    field.MoveMine(col, row)

                }
                field.playerOpenCell(col, row)
                display()
//                if (field.isFiredCell(col, row)) {
//                    println("failed")
//                }
                isFirstFreeCommand = false
            } else {
                field.playerOpenCell(col, row)
                if (field.isFiredCell(col, row)) {
                    field.OpenAllMines()
                    display()
                    println("You stepped on a mine and failed!")
                } else {
                    display()
                }
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

        val (col, row, command) = readln().split(" ")
        game.play(col.toInt() - 1, row.toInt() - 1, command)
    }

}
