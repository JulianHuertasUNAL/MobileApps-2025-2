package com.example.reto_3_4

import kotlin.random.Random

class TicTacToeGame {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val rand = Random(System.currentTimeMillis())

    fun clearBoard() {
        for (i in board.indices) board[i] = OPEN_SPOT
    }

    fun setMove(player: Char, location: Int) {
        if (location in 0 until BOARD_SIZE && board[location] == OPEN_SPOT) {
            board[location] = player
        }
    }

    fun getBoardCopy(): CharArray = board.copyOf()

    /**
     * Simple AI:
     * 1) Win if possible
     * 2) Block human if they'd win next
     * 3) Take center
     * 4) Take a corner
     * 5) Take a side
     */
    fun getComputerMove(): Int {
        // 1. Win if possible
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                if (checkForWinnerInternal(board) == 3) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }
        // 2. Block human win
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = HUMAN_PLAYER
                if (checkForWinnerInternal(board) == 2) {
                    board[i] = OPEN_SPOT
                    return i
                }
                board[i] = OPEN_SPOT
            }
        }
        // 3. Center
        if (board[4] == OPEN_SPOT) return 4
        // 4. Corners
        val corners = listOf(0, 2, 6, 8).shuffled(rand)
        for (c in corners) if (board[c] == OPEN_SPOT) return c
        // 5. Sides
        val sides = listOf(1, 3, 5, 7).shuffled(rand)
        for (s in sides) if (board[s] == OPEN_SPOT) return s
        // fallback
        for (i in board.indices) if (board[i] == OPEN_SPOT) return i
        return -1
    }

    /** Public wrapper: 0=no resultado, 1=empate, 2=X ganó, 3=O ganó */
    fun checkForWinner(): Int = checkForWinnerInternal(board)

    private fun checkForWinnerInternal(b: CharArray): Int {
        val wins = arrayOf(
            intArrayOf(0,1,2),
            intArrayOf(3,4,5),
            intArrayOf(6,7,8),
            intArrayOf(0,3,6),
            intArrayOf(1,4,7),
            intArrayOf(2,5,8),
            intArrayOf(0,4,8),
            intArrayOf(2,4,6)
        )
        for (combo in wins) {
            if (b[combo[0]] != OPEN_SPOT &&
                b[combo[0]] == b[combo[1]] &&
                b[combo[1]] == b[combo[2]]) {
                return if (b[combo[0]] == HUMAN_PLAYER) 2 else 3
            }
        }
        // Si hay algún OPEN_SPOT -> juego no terminado
        for (i in b.indices) if (b[i] == OPEN_SPOT) return 0
        return 1 // empate
    }
}