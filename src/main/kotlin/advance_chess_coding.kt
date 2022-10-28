
import java.lang.IndexOutOfBoundsException

fun main() {

    val game = Game()
    game.startGame()

}

const val MAX_ROW_COL = 18
const val CHESS_BOARD_PLUS_SIGN = "+"
const val CHESS_BOARD_DOTS_SIGN = "---"
const val CHESS_BOARD_PIPE = "|"

const val EMPTY_SPACE = "   "

const val CURRENT_COL = 0
const val CURRENT_ROW = 1
const val TARGET_COL = 2
const val TARGET_ROW = 3

enum class PlayerTurn { WHITE_TURN, BLACK_TURN }


interface PawnInterface {

    val sign : String
    val row: Int
    val col: Int

    fun isAbleToMove():Boolean
    fun isAllowedToCapture(): Boolean
    fun isEnPassant(): Boolean
}
var currentCol = 0
var currentRow = 0
var targetCol = 0
var targetRow = 0


lateinit var userMove : String
var lastMove = Pair("",false)

var currentPlayerTurn = PlayerTurn.WHITE_TURN

var chessBoard = MutableList(MAX_ROW_COL) { MutableList<Any>(MAX_ROW_COL) {EMPTY_SPACE}}


open class ChessBoard {

    private lateinit var whitePawn: WhitePawn
    private lateinit var blackPawn: BlackPawn

    private lateinit var whitePlayerName: String
    private lateinit var blackPlayerName: String

    val columnAlphabet = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val rowNumbers = listOf("8", "7", "6", "5", "4", "3", "2", "1")

    fun displayTheBoard() {

        for (row in 0 until  MAX_ROW_COL){
            for (col in 0 until  MAX_ROW_COL){

                when (val pawn = chessBoard[row][col]) {
                    is WhitePawn -> {
                        print(pawn.sign)
                    }

                    is BlackPawn -> {
                        print(pawn.sign)
                    }

                    else -> print(pawn)
                }
            }

            println()
        }

    }

    fun drawChessBoard() {

        for (row in 0 until MAX_ROW_COL) {

            for (col in 0 until MAX_ROW_COL) {

                if (row == 0) {
                    if (col == 0) chessBoard[row][col] = EMPTY_SPACE
                    else if (col % 2 == 1) chessBoard[row][col] = CHESS_BOARD_PLUS_SIGN
                    else chessBoard[row][col] = CHESS_BOARD_DOTS_SIGN
                } else if (row == MAX_ROW_COL - 1) {
                    if (col == 0) chessBoard[row][col] = EMPTY_SPACE
                    else if (col % 2 == 0) chessBoard[row][col] = spaceAround(columnAlphabet[col / 2 - 1])
                    else chessBoard[row][col] = " "
                } else if (row % 2 == 1) {
                    if (col == 0) chessBoard[row][col] = spaceAround(rowNumbers[row / 2])
                    else if (col % 2 == 1) chessBoard[row][col] = CHESS_BOARD_PIPE
                    else chessBoard[row][col] = EMPTY_SPACE
                } else {
                    if (col == 0) chessBoard[row][col] = EMPTY_SPACE
                    else if (col % 2 == 1) chessBoard[row][col] = CHESS_BOARD_PLUS_SIGN
                    else chessBoard[row][col] = CHESS_BOARD_DOTS_SIGN
                }
            }
        }

    }
    /**
     * first build the initial stage for both the players
     */
    fun buildInitBoardState() {
        for (col in 2 until MAX_ROW_COL) {
            if (col % 2 == 0) {
                chessBoard[3][col] = BlackPawn(2,mapCol(col))
                chessBoard[13][col] = WhitePawn(7,col)
            }
        }
    }

    private fun spaceAround(str: String) = " $str "

    fun mapRow(row: Int) = row * 2 - 1
    fun mapCol(col: Int) = col * 2

    fun isWhiteTurn() = currentPlayerTurn == PlayerTurn.WHITE_TURN
    fun isBlackTurn() = currentPlayerTurn == PlayerTurn.BLACK_TURN

    fun getPlayersInfo() {
        println("First Player's name:")
        whitePlayerName = readln()
        println("Second Player's name:")
        blackPlayerName = readln()
    }

    fun currentPlayerName() = if (isWhiteTurn()) whitePlayerName else blackPlayerName


}


open class Game : ChessBoard() {

    private fun setCoordinate() {
        currentCol    = columnAlphabet.indexOf(userMove[CURRENT_COL].toString()) + 1
        currentRow   = rowNumbers.indexOf(userMove[CURRENT_ROW].toString()) + 1
        targetCol    = columnAlphabet.indexOf(userMove[TARGET_COL].toString()) + 1
        targetRow     = rowNumbers.indexOf(userMove[TARGET_ROW].toString()) + 1
    }

    fun isFirstMove() = (isWhiteTurn() && currentRow  == 7) || (isBlackTurn() && currentRow  == 2)

    fun startGame() {
        println("Pawns-Only Chess")
        drawChessBoard()
        buildInitBoardState()

        getPlayersInfo()
        displayTheBoard()
        getPlayerMove()
        println("Bye!")
    }

    /**
     * here check for all the pawns exits on the table
     * if one of them is able to move so return tru else return false.
     * check for valid conditions
     ******************************************************************************************************************/
    open fun isAbleToMove(): Boolean {

        for (row in 1..8){
            for (col in 1..8){
                val pawn = chessBoard[mapRow(row)][mapCol(col)]

                if (isWhiteTurn() && pawn is WhitePawn) {
                    if (isAbleToMoveForward(row,col)) return true
                    if (isAbleToCapture(row,col)) return true
                    if (isEnPassant()) return true
                }
                else if (isBlackTurn() && pawn is BlackPawn) {
                    if (isAbleToMoveForward(row,col)) return true
                    if (isAbleToCapture(row,col)) return true
                    if (isEnPassant()) return true
                }
            }
        }
        return false
    }

    private fun isAbleToCapture(row: Int,col: Int) : Boolean {

        // 1. if the pawn able to capture the pawns
        // 2. with two condition its possible to capture. row+-1 and col+-1
        val pawnToCheck = chessBoard[mapRow(row)][mapCol(col)]

        val addMinus = if (isWhiteTurn()) 1 else -1

        if (isWhiteTurn() && row == 1) return false
        if (isBlackTurn() && row == 8) return false

        when (col) {
            1 -> {
                val targetPawn = chessBoard[mapRow(row - addMinus) ][mapCol(col + 1)]
                if (pawnToCheck is WhitePawn && targetPawn is BlackPawn) return true
                if (pawnToCheck is BlackPawn && targetPawn is WhitePawn) return true
            }
            8 -> {
                val targetPawn = chessBoard[mapRow(row - addMinus) ][mapCol(col - 1)]
                if (pawnToCheck is WhitePawn && targetPawn is BlackPawn) return true
                if (pawnToCheck is BlackPawn && targetPawn is WhitePawn) return true
            }
            else -> {

                val targetPawn1 = chessBoard[mapRow(row - addMinus) ][mapCol(col + 1)]
                val targetPawn2 = chessBoard[mapRow(row - addMinus) ][mapCol(col - 1)]
                if (pawnToCheck is WhitePawn && targetPawn1 is BlackPawn) return true
                if (pawnToCheck is BlackPawn && targetPawn1 is WhitePawn) return true
                if (pawnToCheck is WhitePawn && targetPawn2 is BlackPawn) return true
                if (pawnToCheck is BlackPawn && targetPawn2 is WhitePawn) return true

            }
        }

        return false

    }

    private fun isAbleToMoveForward(row: Int, col: Int): Boolean {
        // if first move able to move 2 step forward
        // if not first move able to move only one move
        // if the front row is empty can move forward

        if (isWhiteTurn()) {

            if (row == 1) return false
            val pawn = chessBoard[mapRow(row - 1) ][mapCol(col)]
            if (pawn != EMPTY_SPACE) return false
        }
        else {
            if (row == 8) return false
            val pawn = chessBoard[mapRow(row + 1) ][mapCol(col)]
            if (pawn != EMPTY_SPACE) return false
        }

        return true
    }


    open fun isEnPassant(): Boolean {

        val condition1 = currentRow == if (isWhiteTurn()) 4 else 5
        val condition2 = lastMove.first == hasAdjacent() && lastMove.second
        return condition1 && condition2
    }

    open fun hasAdjacent(): String? {

        val isColValid = targetCol == currentCol +1 || targetCol == currentCol-1

        if (!isColValid) return null

        // has adjacent target row -+1
        val adjRow = if (isWhiteTurn()) targetRow + 1 else targetRow - 1
        val pawn1 = chessBoard[mapRow(adjRow)][mapCol(targetRow)]

        if (isWhiteTurn() && pawn1 is BlackPawn) {
            return "${columnAlphabet[targetCol - 1]}${rowNumbers[adjRow - 1]}"
        }
        else if (isBlackTurn() && pawn1 is WhitePawn) {
            return "${columnAlphabet[targetCol - 1]}${rowNumbers[adjRow-1]}"
        }

        return null
    }

    open fun isPawnsExist(): Boolean {

        // location of the board
        return try {
            val currentPawn = chessBoard[mapRow(currentRow)][mapCol(currentCol)]
            if (currentPawn == EMPTY_SPACE) {
                println("No white pawn at ${userMove[CURRENT_COL]}${userMove[CURRENT_ROW]}")

                false
            } else true

        } catch (exception: IndexOutOfBoundsException) {
            println("No white pawn at ${userMove[CURRENT_COL]}${userMove[CURRENT_ROW]}")
            false
        }
    }

    private fun getPlayerMove() {

        val regex = Regex("[a-h][1-8][a-h][1-8]")

        while (true) {

            println("${currentPlayerName()}'s turn:")

            // Get the user move
            userMove = readln()
            // set the coordinate tor variable
            setCoordinate()

            when {
                userMove == "exit" -> break
                !userMove.matches(regex) -> println("Invalid Input")
                isMoveValid() -> {
                    makeMove()
                    if (winConditions()) break
                    changeCurrentPlayer()
                    // check the stalemate condition
                    if (!isAbleToMove()){
                        println("Stalemate!")
                        break
                    }


                }
            }
        }
    }

    private fun makeMove() {

        if (isEnPassant()) {
            // change the current space to white
            chessBoard[mapRow(currentRow)][mapCol(currentCol)] = EMPTY_SPACE

            // bellow or above target column should also become white space if is white bellow if black above white space
            if (isWhiteTurn()) chessBoard[ mapRow((targetRow + 1))][mapCol(targetCol)] = EMPTY_SPACE
            else chessBoard[(targetRow - 1) * 2 - 1][targetCol * 2] = EMPTY_SPACE

            chessBoard[mapRow(targetRow)][ mapCol(targetCol)] =
                if (isWhiteTurn()) WhitePawn(mapRow(currentRow),mapCol(currentCol))
                else BlackPawn(mapRow(currentRow),mapCol(currentCol))
        }
        else {
            // change the current space to white
            chessBoard[mapRow(currentRow)][mapCol(currentCol)] = EMPTY_SPACE
            chessBoard[mapRow(targetRow)][ mapCol(targetCol)] =
                if (isWhiteTurn()) WhitePawn(mapRow(currentRow),mapCol(currentCol))
                else BlackPawn(mapRow(currentRow),mapCol(currentCol))
        }

        // after move done display the board again
        displayTheBoard()

    }

    private fun changeCurrentPlayer() {

        val targetLocation = "${userMove[TARGET_COL]}${userMove[TARGET_ROW]}"
        lastMove = if (isFirstMove()) targetLocation to true
        else targetLocation to  false

        // Now change the player turn
        currentPlayerTurn = if (isWhiteTurn()) PlayerTurn.BLACK_TURN
        else PlayerTurn.WHITE_TURN

    }

    private fun isMoveValid(): Boolean {

        if (!isPawnsExist()) return false

        if (!isTargetPlaceValid()) {
            println("Invalid Input")
            return false
        }
        return true
    }


    private fun isTargetPlaceValid(): Boolean {

        val currentPawn = chessBoard[mapRow(currentRow)][mapCol(currentCol)]

        if (isWhiteTurn()) {
            if (currentRow < targetRow) return false
        }
        else {
            if (currentRow > targetRow) return false
        }


        if ((currentPawn is WhitePawn) && currentPawn.isAbleToMove()) {
            return true
        }
        else if ((currentPawn is BlackPawn) && currentPawn.isAbleToMove()) {
            return true
        }

        return false

    }


    /**
     * The program should check whether the winning or stalemate conditions are met after each turn.
     * The game is over when either player succeeds in moving their pawn to the last opposite rank — rank
     * 8 for White, rank 1 for Black. The game is also over when all opposite pawns are captured.
     * Stalemate (draw) occurs when a player can't make any valid move on their turn.
     ******************************************************************************************************************/
    private fun winConditions(): Boolean {
        if (reachedToTheLast()) return true
        if (arePawnsCaptured()) return true
        return false
    }

    private fun reachedToTheLast(): Boolean {

        val rowToCheck = if (isWhiteTurn()) 1 else 8

        for (col in 1..8) {
            val pawn = chessBoard[mapRow(rowToCheck)][mapCol(col)]

            if (isWhiteTurn()) {
                if (pawn is WhitePawn) {
                    println("White Wins!")
                    return true
                }
            }
            else {
                if (pawn is BlackPawn) {
                    println("Black Wins!")
                    return true
                }
            }

        }

        return false
    }

    private fun arePawnsCaptured(): Boolean {

        var totalWhite = 0
        var totalBlack = 0

        for (row in 1..8){
            for (col in 1..8){
                val pawn = chessBoard[mapRow(row)][mapCol(col)]
                if (pawn is WhitePawn) totalWhite++
                else if (pawn is BlackPawn) totalBlack++
            }
        }
        if (totalWhite == 0) {
            println("Black Wins!")
            return true
        }
        else if (totalBlack == 0) {
            println("White Wins!")
            return true
        }
        return false

    }

}


class WhitePawn(override val row: Int,override val col: Int) : Game(),PawnInterface {

    override val sign = " W "

    override fun isAbleToMove(): Boolean {
        val condition1 = isAllowedToCapture()
        val condition2 = isAbleToMoveForward()
        val condition3 = isEnPassant()
        return condition1 || condition2 || condition3
    }


    override fun isAllowedToCapture(): Boolean {
        val isTargetPlaceValidPosition = targetRow == currentRow - 1 && (targetCol == currentCol+1 || targetCol == currentCol-1)
        val pawn = chessBoard[mapRow(targetRow) ][mapCol(targetCol)]
        return isTargetPlaceValidPosition && pawn is BlackPawn
    }

    private fun isAbleToMoveForward(): Boolean {
        if (currentRow == 1) return false

        return if (isFirstMove()) {
            val pawn1 = chessBoard[mapRow(currentRow - 1) ][mapCol(currentCol)]
            val pawn2 = chessBoard[mapRow(currentRow - 2) ][mapCol(currentCol)]
            val condition1 = if (targetRow == currentRow -1) pawn1 == EMPTY_SPACE else pawn1 == EMPTY_SPACE && pawn2 == EMPTY_SPACE
            val condition2 = targetCol == currentCol && (targetRow == currentRow - 1 || targetRow == currentRow - 2)
            condition1 && condition2
        } else {

            val pawn1 = chessBoard[mapRow(currentRow - 1) ][mapCol(currentCol)]
            val condition1 = pawn1 == EMPTY_SPACE
            val condition2 = targetCol == currentCol && (targetRow == currentRow - 1 || targetRow == currentRow - 2)
            condition1 && condition2
        }
    }
}

class BlackPawn(override val row: Int,override val col: Int) : Game(),PawnInterface {

    override val sign = " B "
    override fun isAbleToMove(): Boolean {
        val condition1 = isAllowedToCapture()
        val condition2 = isAbleToMoveForward()
        val condition3 = isEnPassant()
        return condition1 || condition2 || condition3
    }
    override fun isAllowedToCapture(): Boolean {
        val isTargetPlaceValidPosition = targetRow == currentRow + 1 && (targetCol == currentCol+1 || targetCol == currentCol-1)
        val pawn = chessBoard[mapRow(targetRow) ][mapCol(targetCol)]
        return isTargetPlaceValidPosition && pawn is WhitePawn
    }
    private fun isAbleToMoveForward(): Boolean {
        if (currentRow == 8) return false
        return if (isFirstMove()) {
            val pawn1 = chessBoard[mapRow(currentRow + 1) ][mapCol(currentCol)]
            val pawn2 = chessBoard[mapRow(currentRow + 2) ][mapCol(currentCol)]
            val condition1 = if (targetRow == currentRow + 1) pawn1 == EMPTY_SPACE else pawn1 == EMPTY_SPACE && pawn2 == EMPTY_SPACE
            val condition2 = targetCol == currentCol && (targetRow == currentRow + 1 || targetRow == currentRow + 2)

            condition1 && condition2

        } else {

            val pawn1 = chessBoard[mapRow(currentRow + 1) ][mapCol(currentCol)]
            val condition1 = pawn1 == EMPTY_SPACE
            val condition2 = targetCol == currentCol && (targetRow == currentRow + 1)
            condition1 && condition2
        }
    }


}