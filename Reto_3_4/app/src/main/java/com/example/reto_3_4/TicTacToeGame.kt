package com.example.reto_3_4

import kotlin.random.Random

class TicTacToeGame {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }

    enum class DifficultyLevel { Easy, Harder, Expert }

    var currentDifficultyLevel: DifficultyLevel = DifficultyLevel.Expert
        private set // El setter es privado, solo se puede cambiar desde DENTRO de TicTacToeGame

    // Método público para cambiar el nivel de dificultad
    fun setDifficultyLevel(newLevel: DifficultyLevel) {
        currentDifficultyLevel = newLevel
    }

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val rand = Random(System.currentTimeMillis())

    fun clearBoard() {
        for (i in board.indices) board[i] = OPEN_SPOT
    }

    fun setMove(player: Char, location: Int): Boolean {
        if (location in 0 until BOARD_SIZE && board[location] == OPEN_SPOT) {
            board[location] = player
            return true
        }
        return false
    }

    fun getBoardCopy(): CharArray = board.copyOf()

    fun getBoardOccupant(location: Int): Char {
        return if (location in 0 until BOARD_SIZE) {
            board[location]
        } else {
            // Manejo de error o valor por defecto si la ubicación es inválida.
            // Podrías lanzar una IllegalArgumentException aquí también,
            // pero para este caso, devolver OPEN_SPOT puede ser más simple
            // si el código de llamada puede manejarlo.
            OPEN_SPOT // O podrías lanzar una excepción.
        }
    }
    private fun getRandomMove(): Int {
        val availableSpots = mutableListOf<Int>()
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                availableSpots.add(i)
            }
        }
        return if (availableSpots.isNotEmpty()) {
            availableSpots.random(rand) // Usa el mismo 'rand' para consistencia
        } else {
            -1 // No hay movimientos disponibles
        }
    }

    /**
     * Devuelve el índice de un movimiento ganador para el COMPUTER_PLAYER.
     * Devuelve -1 si no hay movimiento ganador.
     * Importante: Deja el tablero en su estado original.
     */
    private fun getWinningMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER // Prueba el movimiento
                if (checkForWinnerInternal(board) == 3) { // 3 significa que COMPUTER_PLAYER (O) ganó
                    board[i] = OPEN_SPOT // Deshace el movimiento
                    return i
                }
                board[i] = OPEN_SPOT // Deshace el movimiento
            }
        }
        return -1
    }

    /**
     * Devuelve el índice de un movimiento para bloquear una victoria del HUMAN_PLAYER.
     * Devuelve -1 si no hay movimiento de bloqueo.
     * Importante: Deja el tablero en su estado original.
     */
    private fun getBlockingMove(): Int {
        for (i in board.indices) {
            if (board[i] == OPEN_SPOT) {
                board[i] = HUMAN_PLAYER // Prueba si el humano jugara ahí
                if (checkForWinnerInternal(board) == 2) { // 2 significa que HUMAN_PLAYER (X) ganó
                    board[i] = OPEN_SPOT // Deshace el movimiento
                    return i
                }
                board[i] = OPEN_SPOT // Deshace el movimiento
            }
        }
        return -1
    }

    fun getComputerMove(): Int {
        var move = -1 // Inicializa 'move'

        when (currentDifficultyLevel) {
            DifficultyLevel.Easy -> {
                move = getRandomMove()
            }
            DifficultyLevel.Harder -> {
                move = getWinningMove()
                if (move == -1) {
                    move = getRandomMove()
                }
            }
            DifficultyLevel.Expert -> {
                move = getWinningMove()
                if (move == -1) {
                    move = getBlockingMove()
                }
                // Si todavía no hay movimiento, intenta tomar el centro, luego esquinas, luego lados
                if (move == -1) {
                    if (board[4] == OPEN_SPOT) {
                        move = 4
                    } else {
                        // Intenta una esquina aleatoria
                        val corners = listOf(0, 2, 6, 8).shuffled(rand)
                        for (c in corners) {
                            if (board[c] == OPEN_SPOT) {
                                move = c
                                break
                            }
                        }
                        // Si no hay esquinas, intenta un lado aleatorio
                        if (move == -1) {
                            val sides = listOf(1, 3, 5, 7).shuffled(rand)
                            for (s in sides) {
                                if (board[s] == OPEN_SPOT) {
                                    move = s
                                    break
                                }
                            }
                        }
                    }
                }
                if (move == -1) {
                    move = getRandomMove()
                }
            }
        }
        return move
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