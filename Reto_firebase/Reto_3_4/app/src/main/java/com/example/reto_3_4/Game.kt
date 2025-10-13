package com.example.reto_3_4

import com.google.firebase.firestore.Exclude

// La anotación @JvmField en gameId es importante para que Firestore pueda
// establecer este valor al deserializar, ya que no está en el constructor.
data class Game(
    val player1Id: String? = null,
    val player1Name: String? = null,
    var player2Id: String? = null,
    var player2Name: String? = null,
    val board: List<String> = List(9) { "" }, // Usamos "" para casilla vacía, "X" o "O"
    var turn: String? = player1Id, // El turno inicial es del jugador 1
    var status: GameStatus = GameStatus.WAITING,
    var winner: String? = null
) {
    @get:Exclude // Excluye este campo de ser guardado en Firestore
    @set:Exclude
    @JvmField
    var gameId: String? = null
}

enum class GameStatus {
    WAITING, // Esperando al jugador 2
    IN_PROGRESS, // En progreso
    FINISHED // Partida terminada
}