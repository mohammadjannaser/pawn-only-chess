package chess




/**
 * win condition
 * 1. one of the pawns reaches the opponent's last row
 * 2.  if all opponent pawns are captured
 * draw condition
 * 1. one of the players is unable to make a valid move
 */

const val MAX_ROW_COL = 18
const val CHESS_BOARD_PLUS_SIGN = "+"
const val CHESS_BOARD_DOTS_SIGN = "---"
const val CHESS_BOARD_PIPE = "|"

const val PLAYER_WHITE_SIGN = "W"
const val PLAYER_BLACK_SIGN = "B"
const val EMPTY_SPACE = "   "

const val CURRENT_COL = 0
const val CURRENT_ROW = 1
const val TARGET_COL = 2
const val TARGET_ROW = 3

enum class PlayerTurn { FIRST_PLAYER_TURN, SECOND_PLAYER_TURN }

class Chess {

    private var currentPlayerTurn = PlayerTurn.FIRST_PLAYER_TURN

    private var board = MutableList(MAX_ROW_COL) { MutableList(MAX_ROW_COL) { "" } }

    private val columnAlphabet = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    private val rowNumbers = listOf("8", "7", "6", "5", "4", "3", "2", "1")

    private lateinit var firstPlayerName: String
    private lateinit var secondPlayerName: String

    private val boardCoordinate = MutableList(4){0}
    private lateinit var userMove : String

    var lastMove = Pair("",false)

    fun startGame() {

        println("Pawns-Only Chess")
        drawChessBoard()
        buildInitBoardState()

        getPlayersInfo()
        displayTheBoard()

        getPlayerMove()

    }

    private fun getPlayersInfo() {
        println("First Player's name:")
        firstPlayerName = readln()
        println("Second Player's name:")
        secondPlayerName = readln()
    }

    private fun getPlayerMove() {

        val regex = Regex("[a-h][1-8][a-h][1-8]")

        while (true) {
            println(
                if (currentPlayerTurn == PlayerTurn.FIRST_PLAYER_TURN) "$firstPlayerName's turn:"
                else "$secondPlayerName's turn:"
            )

            // Get the user move
            userMove = readln()
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

        println("Bye!")

    }

    private fun setCoordinate() {
        boardCoordinate[CURRENT_COL]    = columnAlphabet.indexOf(userMove[CURRENT_COL].toString()) + 1
        boardCoordinate[CURRENT_ROW]    = rowNumbers.indexOf(userMove[CURRENT_ROW].toString()) + 1
        boardCoordinate[TARGET_COL]     = columnAlphabet.indexOf(userMove[TARGET_COL].toString()) + 1
        boardCoordinate[TARGET_ROW]     = rowNumbers.indexOf(userMove[TARGET_ROW].toString()) + 1
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

        val rowToCheck = if (isWhitePlayerTurn()) 1 else 8

        for (col in 1..8) {
            val pawn = board[mapRow(rowToCheck)][mapCol(col)]

            if (pawn.contains(if (isWhitePlayerTurn()) PLAYER_WHITE_SIGN else PLAYER_BLACK_SIGN)) {
                if (isWhitePlayerTurn()) println("White Wins!") else println("Black Wins!")
                return true
            }
        }

        return false
    }

    private fun arePawnsCaptured(): Boolean {

        var totalWhite = 0
        var totalBlack = 0

        for (row in 1..8){
            for (col in 1..8){
                val pawn = board[row * 2 - 1][col * 2]
                if (pawn.contains(PLAYER_WHITE_SIGN)) totalWhite++
                else if (pawn.contains(PLAYER_BLACK_SIGN)) totalBlack++
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


    private fun isMoveValid(): Boolean {

        if (!isPawnsExist()) return false

        if (!isTargetPlaceValid()) {
            println("Invalid Input")
            return false
        }
        return true
    }


    /**
     * here check for all the pawns exits on the table
     * if one of them is able to move so return tru else return false.
     * check for valid conditions
     ******************************************************************************************************************/
    private fun isAbleToMove(): Boolean {

        for (row in 1..8){
            for (col in 1..8){
                val pawn = board[mapRow(row)][mapCol(col)]
                if (pawn == if (isWhitePlayerTurn()) PLAYER_WHITE_SIGN else PLAYER_BLACK_SIGN ) {
                    if (isAbleToMoveForward(row,col)) return true
                    if (isAbleToCapture(row,col)) return true
                    if (isEnPassant()) return true
                }
            }
        }
        return false
    }

    private fun mapRow(row: Int) = row * 2 - 1
    private fun mapCol(col: Int) = col * 2

    private fun isAbleToCapture(row: Int,col: Int) : Boolean {

        // 1. if the pawn able to capture the pawns
        // 2. with two condition its possible to capture. row+-1 and col+-1
        val pawnSign = if (isWhitePlayerTurn()) PLAYER_BLACK_SIGN else PLAYER_WHITE_SIGN
        val addMinus = if (isWhitePlayerTurn()) 1 else -1

        if (isWhitePlayerTurn() && row == 1) return false
        if (isBlackPlayerTurn() && row == 8) return false

        when  {
            col == 1 && row == 1 -> {
                val pawn = board[mapRow(row - addMinus) ][mapCol(col + 1)]
                if (pawn.contains(pawnSign)) return true
            }
            col == 8 && row == 8 -> {
                val pawn = board[mapRow(row - addMinus) ][mapCol(col - 1)]
                if (pawn.contains(pawnSign)) return true
            }
            else -> {
                val pawn1 = board[mapRow(row - addMinus) ][mapCol(col + 1)]
                val pawn2 = board[mapRow(row - addMinus) ][mapCol(col - 1)]

                if (pawn1.contains(pawnSign)) return true
                if (pawn2.contains(pawnSign)) return true
            }
        }

        return false

    }

    private fun isAbleToMoveForward(row: Int, col: Int): Boolean {
        // if first move able to move 2 step forward
        // if not first move able to move only one move
        // if the front row is empty can move forward

        if (isWhitePlayerTurn()) {

            if (row == 1) return false
            val pawn = board[mapRow(row - 1) ][mapCol(col)]
            if (pawn != EMPTY_SPACE) return false
        }
        else {
            if (row == 8) return false
            val pawn = board[mapRow(row + 1) ][mapCol(col)]
            if (pawn != EMPTY_SPACE) return false
        }

        return true
    }

    private fun isPawnsExist(): Boolean {

        // location of the board
        val pawns = board[boardCoordinate[CURRENT_ROW] * 2 - 1][boardCoordinate[CURRENT_COL] * 2]

        if (currentPlayerTurn == PlayerTurn.FIRST_PLAYER_TURN && !pawns.contains(PLAYER_WHITE_SIGN)) {
            println("No white pawn at ${userMove[CURRENT_COL]}${userMove[CURRENT_ROW]}")
            return false
        } else if (currentPlayerTurn == PlayerTurn.SECOND_PLAYER_TURN && !pawns.contains(PLAYER_BLACK_SIGN)) {
            println("No black pawn at ${userMove[CURRENT_COL]}${userMove[CURRENT_ROW]}")
            return false
        }
        return true
    }


    /**
     *  1. if first move allow two rank only
     *  2. other moves allowed only one rank move
     *  3. move only forward not backward or next
     *  4. the target place is empty or not
     ******************************************************************************************************************/
    private fun isTargetPlaceValid(): Boolean {

        val currentCol = boardCoordinate[CURRENT_COL]
        val currentRow = boardCoordinate[CURRENT_ROW]
        val targetCol = boardCoordinate[TARGET_COL]
        val targetRow = boardCoordinate[TARGET_ROW]

        // target row can not be backward
        // e6e6 black player turn can not go from e6 to e8
        // white player can not go backward from a2 a1

        if (isWhitePlayerTurn()) {
            if (currentRow < targetRow) return false
        }
        else {
            if (currentRow > targetRow) return false
        }

        return  when{
            isAbleToMovePawn() -> true
            isEnPassant() -> true
            isAbleToCapturePawn() -> true
            else -> false
        }

    }

    private fun isAbleToCapturePawn(): Boolean {

        val currentCol = boardCoordinate[CURRENT_COL]
        val currentRow = boardCoordinate[CURRENT_ROW]
        val targetCol = boardCoordinate[TARGET_COL]
        val targetRow = boardCoordinate[TARGET_ROW]

        val pawnSign = if (isWhitePlayerTurn()) PLAYER_BLACK_SIGN else PLAYER_WHITE_SIGN
        val addMinus = if (isWhitePlayerTurn()) 1 else -1

        val isTargetPlaceValidPosition = targetRow == currentRow - addMinus && (targetCol == currentCol+1 || targetCol == currentCol-1)
        val pawn = board[mapRow(targetRow) ][mapCol(targetCol)]

        return isTargetPlaceValidPosition && pawn.contains(pawnSign)
    }

    private fun isAbleToMovePawn(): Boolean {

        val currentCol = boardCoordinate[CURRENT_COL]
        val currentRow = boardCoordinate[CURRENT_ROW]
        val targetCol = boardCoordinate[TARGET_COL]
        val targetRow = boardCoordinate[TARGET_ROW]

        val forward1 = if (isWhitePlayerTurn()) + 1 else - 1
        val forward2 = if (isWhitePlayerTurn()) + 2 else - 2

        return if (isFirstMove()) {
            targetCol == currentCol && (targetRow == currentRow - forward1 || targetRow == currentRow - forward2)
        } else targetCol == currentCol && targetRow == currentRow - forward1

    }


    // En passant condition for white player
    // 1. A white pawn is on the 5th rank
    // 2. A black pawn is on the adjacent file (a vertical line); the black pawn
    // (this should be its first move in the game) moves 2 squares forward, passing the white pawn.
    // 3. The white pawn can capture the black pawn by moving forward diagonally.
    // 4.  The capture should be done right away, otherwise, the right to the en passant capture is lost:

    // En passant condition for black player
    // 1. A black pawn is on the 4th rank
    // 2. A white pawn is on the adjacent file (a vertical line); the white pawn
    // (this should be its first move in the game) moves 2 squares forward passing the black pawn.
    // 3.  The black pawn can capture the white pawn by moving forward diagonally
    // 4. The capture should be done right away, otherwise, the right to the en passant capture is lost:

    private fun isEnPassant(): Boolean {
        val currentCol = boardCoordinate[CURRENT_COL]
        val currentRow = boardCoordinate[CURRENT_ROW]
        val targetCol = boardCoordinate[TARGET_COL]
        val targetRow = boardCoordinate[TARGET_ROW]

        val condition1 = currentRow == if (isWhitePlayerTurn()) 4 else 5
        val condition2 = lastMove.first == hasAdjacent() && lastMove.second

        return condition1 && condition2
    }

    private fun hasAdjacent(): String? {

        val currentCol = boardCoordinate[CURRENT_COL]
        val currentRow = boardCoordinate[CURRENT_ROW]
        val targetCol = boardCoordinate[TARGET_COL]
        val targetRow = boardCoordinate[TARGET_ROW]

        val isColValid = targetCol == currentCol+1 || targetCol == currentCol-1

        if (!isColValid) return null

        // has adjacent target row -+1
        val adjRow = if (isWhitePlayerTurn()) targetRow + 1 else targetRow - 1
        val pawn1 = board[adjRow * 2 - 1][targetCol * 2]

        if (isWhitePlayerTurn() && pawn1.contains(PLAYER_BLACK_SIGN)) {
            return "${columnAlphabet[targetCol - 1]}${rowNumbers[adjRow - 1]}"
        }
        else if (isBlackPlayerTurn() && pawn1.contains(PLAYER_WHITE_SIGN)) {
            return "${columnAlphabet[targetCol-1]}${rowNumbers[adjRow-1]}"
        }

        return null
    }


    private fun isTargetPlaceEmpty(): Boolean {
        // location of the board
        val pawns = board[boardCoordinate[TARGET_ROW] * 2 - 1][boardCoordinate[TARGET_COL] * 2]
        return pawns.contains(EMPTY_SPACE)
    }

    private fun hasOpponentPawns(): Boolean {
        // location of the board
        val pawns = board[boardCoordinate[TARGET_ROW] * 2 - 1][boardCoordinate[TARGET_COL] * 2]
        return pawns.contains(if (currentPlayerTurn == PlayerTurn.FIRST_PLAYER_TURN) PLAYER_BLACK_SIGN else PLAYER_WHITE_SIGN)
    }

    private fun isWhitePlayerTurn() = currentPlayerTurn == PlayerTurn.FIRST_PLAYER_TURN
    private fun isBlackPlayerTurn() = currentPlayerTurn == PlayerTurn.SECOND_PLAYER_TURN

    private fun makeMove() {

        if (isEnPassant()) {
            // change the current space to white
            board[boardCoordinate[CURRENT_ROW] * 2 - 1][boardCoordinate[CURRENT_COL] * 2] = EMPTY_SPACE

            // bellow or above target column should also become white space if is white bellow if black above white space
            if (isWhitePlayerTurn()) board[(boardCoordinate[TARGET_ROW] + 1) * 2 - 1][boardCoordinate[TARGET_COL] * 2] = EMPTY_SPACE
            else board[(boardCoordinate[TARGET_ROW] - 1) * 2 - 1][boardCoordinate[TARGET_COL] * 2] = EMPTY_SPACE

            board[boardCoordinate[TARGET_ROW] * 2 - 1][boardCoordinate[TARGET_COL] * 2] =
                if (isWhitePlayerTurn()) spaceAround(PLAYER_WHITE_SIGN)
                else spaceAround(PLAYER_BLACK_SIGN)
        }
        else {
            // change the current space to white
            board[boardCoordinate[CURRENT_ROW] * 2 - 1][boardCoordinate[CURRENT_COL] * 2] = EMPTY_SPACE
            board[boardCoordinate[TARGET_ROW] * 2 - 1][boardCoordinate[TARGET_COL] * 2] =
                if (isWhitePlayerTurn()) spaceAround(PLAYER_WHITE_SIGN)
                else spaceAround(PLAYER_BLACK_SIGN)
        }


        // after move done display the board again
        displayTheBoard()


    }

    private fun changeCurrentPlayer() {

        val targetLocation = "${userMove[TARGET_COL]}${userMove[TARGET_ROW]}"
        lastMove = if (isFirstMove()) targetLocation to true
        else targetLocation to  false

        // Now change the player turn
        currentPlayerTurn = if (currentPlayerTurn == PlayerTurn.FIRST_PLAYER_TURN)
            PlayerTurn.SECOND_PLAYER_TURN
        else PlayerTurn.FIRST_PLAYER_TURN

    }


    private fun isFirstMove() = (isWhitePlayerTurn() && boardCoordinate[CURRENT_ROW]  == 7) ||
            (isBlackPlayerTurn() && boardCoordinate[CURRENT_ROW]  == 2)


    /**
     * first build the initial stage for both the players
     */
    private fun buildInitBoardState() {
        for (col in 2 until MAX_ROW_COL) {
            if (col % 2 == 0) {
                board[3][col] = spaceAround(PLAYER_BLACK_SIGN)
                board[13][col] = spaceAround(PLAYER_WHITE_SIGN)
            }
        }
    }

    private fun displayTheBoard() {
        for (row in board) {
            println(row.joinToString(""))
        }
    }

    private fun drawChessBoard() {

        for (row in 0 until MAX_ROW_COL) {

            for (col in 0 until MAX_ROW_COL) {

                if (row == 0) {
                    if (col == 0) board[row][col] = EMPTY_SPACE
                    else if (col % 2 == 1) board[row][col] = CHESS_BOARD_PLUS_SIGN
                    else board[row][col] = CHESS_BOARD_DOTS_SIGN
                } else if (row == MAX_ROW_COL - 1) {
                    if (col == 0) board[row][col] = EMPTY_SPACE
                    else if (col % 2 == 0) board[row][col] = spaceAround(columnAlphabet[col / 2 - 1])
                    else board[row][col] = " "
                } else if (row % 2 == 1) {
                    if (col == 0) board[row][col] = spaceAround(rowNumbers[row / 2])
                    else if (col % 2 == 1) board[row][col] = CHESS_BOARD_PIPE
                    else board[row][col] = EMPTY_SPACE
                } else {
                    if (col == 0) board[row][col] = EMPTY_SPACE
                    else if (col % 2 == 1) board[row][col] = CHESS_BOARD_PLUS_SIGN
                    else board[row][col] = CHESS_BOARD_DOTS_SIGN
                }
            }
        }

    }

    private fun spaceAround(str: String) = " $str "

}