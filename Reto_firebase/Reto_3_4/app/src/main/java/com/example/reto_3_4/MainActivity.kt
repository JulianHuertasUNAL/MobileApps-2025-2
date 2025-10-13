package com.example.reto_3_4

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.os.Handler // Para el retraso del computador (opcional)
import android.os.Looper  // Para el retraso del computador (opcional)
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reto_3_4.databinding.ActivityMainBinding
import android.view.MotionEvent
import android.media.AudioAttributes // Necesario para SoundPool en API 21+
import android.media.SoundPool // Importar SoundPool
import android.os.Build // Para comprobaciones de versión de API
import androidx.core.content.edit

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class MainActivity : AppCompatActivity(),DifficultyDialogFragment.DifficultyDialogListener,
QuitConfirmDialogFragment.QuitConfirmDialogListener, BoardView.OnCellTouchListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var boardView: BoardView

    // --- Lógica de Firebase ---
    private lateinit var db: FirebaseFirestore
    private var gameListener: ListenerRegistration? = null
    private var gameId: String? = null
    private var currentGame: Game? = null // El estado actual del juego, viene de Firestore
    private lateinit var playerId: String // ID de este jugador

    // --- Lógica de Sonido (se mantiene) ---
    private var mSoundPool: SoundPool? = null
    private var mHumanSoundId: Int = 0
    private var mComputerSoundId: Int = 0 // Podemos renombrar esto a "opponentSoundId"
    private var mSoundPoolLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Inicialización ---
        boardView = binding.boardView
        boardView.setOnCellTouchListener(this)

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el ID del jugador y el ID del juego desde el Intent
        playerId = PlayerManager.getPlayerId(this)
        gameId = intent.getStringExtra("GAME_ID")

        if (gameId == null) {
            Toast.makeText(this, "Error: ID de partida no encontrado.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay ID de partida
            return
        }

        // Ya no se necesita la lógica de onSaveInstanceState/restore
        // ni SharedPreferences aquí, Firestore es la fuente de verdad.
        // Se elimina también la lógica del A.I. y sus puntuaciones.

        setupSoundPool()
    }

    override fun onStart() {
        super.onStart()
        // Empezar a escuchar los cambios del juego en Firestore
        gameId?.let { listenForGameUpdates(it) }
    }

    override fun onStop() {
        super.onStop()
        // Dejar de escuchar para ahorrar recursos
        gameListener?.remove()
    }

    private fun listenForGameUpdates(gameId: String) {
        val gameRef = db.collection("games").document(gameId)
        gameListener = gameRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Error al escuchar la partida: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Mapear el documento de Firestore a nuestro objeto Game
                currentGame = snapshot.toObject(Game::class.java)
                // Actualizar toda la UI con los nuevos datos
                updateUI()
            } else {
                Toast.makeText(this, "La partida ya no existe.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateUI() {
        currentGame?.let { game ->
            // 1. Actualizar el tablero
            boardView.setBoardState(game.board) // Necesitaremos un metodo en BoardView para pasar el tablero
            boardView.invalidate()

            // 2. Actualizar texto de información y estado del juego
            var infoText = ""
            when (game.status) {
                GameStatus.WAITING -> {
                    infoText = "Esperando que se una el Jugador 2..."
                }
                GameStatus.IN_PROGRESS -> {
                    infoText = if (game.turn == playerId) {
                        "¡Es tu turno!"
                    } else {
                        "Turno de ${getOpponentName(game)}"
                    }
                }
                GameStatus.FINISHED -> {
                    infoText = when {
                        game.winner == "TIE" -> "¡Es un empate!"
                        game.winner == playerId -> "¡Has ganado!"
                        else -> "Has perdido. Gana ${getOpponentName(game)}."
                    }
                    // Deshabilitar la opción de "Nuevo Juego" si el juego multijugador ha terminado.
                    // El usuario debe volver al Lobby para iniciar otro.
                    invalidateOptionsMenu()
                }
            }
            binding.information.text = infoText

            // 3. (Opcional) Actualizar puntuaciones si decides implementarlas
            // Por ahora, el layout de puntuaciones ya no se usará.
            binding.humanScoreNumber.text = ""
            binding.androidScoreNumber.text = ""
            binding.tiesScoreNumber.text = ""
            binding.humanScoreText.text = ""
            binding.androidScoreText.text = ""
            binding.tiesScoreText.text = ""
        }
    }

    private fun getOpponentName(game: Game): String {
        return if (game.player1Id == playerId) game.player2Name ?: "Oponente" else game.player1Name ?: "Oponente"
    }

    override fun onCellTouched(cellIndex: Int) {
        val game = currentGame ?: return

        // --- Validaciones antes de realizar un movimiento ---
        if (game.status != GameStatus.IN_PROGRESS) {
            Toast.makeText(this, "La partida no está en curso.", Toast.LENGTH_SHORT).show()
            return
        }
        if (game.turn != playerId) {
            Toast.makeText(this, "No es tu turno.", Toast.LENGTH_SHORT).show()
            return
        }
        if (game.board[cellIndex].isNotEmpty()) {
            Toast.makeText(this, "Esa casilla ya está ocupada.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Realizar el movimiento ---
        mSoundPool?.play(mHumanSoundId, 1.0f, 1.0f, 1, 0, 1.0f)

        val newBoard = game.board.toMutableList()
        val playerMark = if (game.player1Id == playerId) "X" else "O"
        newBoard[cellIndex] = playerMark

        // Determinar el próximo turno
        val nextTurn = if (game.player1Id == playerId) game.player2Id else game.player1Id

        // Verificar si hay ganador
        val winnerResult = checkForWinner(newBoard)
        var newStatus = game.status
        var winnerId: String? = null

        when (winnerResult) {
            1 -> { // Gana X
                winnerId = game.player1Id
                newStatus = GameStatus.FINISHED
            }
            2 -> { // Gana O
                winnerId = game.player2Id
                newStatus = GameStatus.FINISHED
            }
            3 -> { // Empate
                winnerId = "TIE"
                newStatus = GameStatus.FINISHED
            }
        }

        // --- Escribir los cambios en Firestore ---
        gameId?.let {
            db.collection("games").document(it)
                .update(mapOf(
                    "board" to newBoard,
                    "turn" to nextTurn,
                    "status" to newStatus.name,
                    "winner" to winnerId
                ))
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al realizar el movimiento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        // ¡No actualices la UI aquí! El listener `listenForGameUpdates` se encargará de ello.
    }

    private fun checkForWinner(board: List<String>): Int {
        // Filas
        for (i in 0..2) {
            if (board[i * 3] != "" && board[i * 3] == board[i * 3 + 1] && board[i * 3] == board[i * 3 + 2]) {
                return if (board[i * 3] == "X") 1 else 2
            }
        }
        // Columnas
        for (i in 0..2) {
            if (board[i] != "" && board[i] == board[i + 3] && board[i] == board[i + 6]) {
                return if (board[i] == "X") 1 else 2
            }
        }
        // Diagonales
        if (board[0] != "" && board[0] == board[4] && board[0] == board[8]) return if (board[0] == "X") 1 else 2
        if (board[2] != "" && board[2] == board[4] && board[2] == board[6]) return if (board[2] == "X") 1 else 2

        // Comprobar empate
        if (board.none { it.isEmpty() }) return 3

        return 0 // No hay ganador ni empate todavía
    }

    override fun onDifficultySelected(difficultyLevel: TicTacToeGame.DifficultyLevel) { /* No-op */ }
    override fun onQuitConfirmed() { finish() }

    private fun setupSoundPool() {
        val audioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(2)
            .build()
        mSoundPool?.setOnLoadCompleteListener { _, _, status ->
            mSoundPoolLoaded = (status == 0)
        }
        mHumanSoundId = mSoundPool?.load(this, R.raw.human_move, 1) ?: 0
        mComputerSoundId = mSoundPool?.load(this, R.raw.computer_move, 1) ?: 0
    }

    override fun onDestroy() {
        super.onDestroy()
        mSoundPool?.release()
        mSoundPool = null
        gameListener?.remove() // Asegurarse de que el listener se quite al destruir la actividad
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        menu.findItem(R.id.ai_difficulty)?.isVisible = false
        menu.findItem(R.id.reset_scores)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_game -> {
                // En multijugador, "Nuevo Juego" debería llevar de vuelta al Lobby.
                finish() // Cierra la actividad actual y vuelve a LobbyActivity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}