package com.example.reto_3_4

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reto_3_4.databinding.ActivityLobbyBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var gameAdapter: GameAdapter
    private var gamesListener: ListenerRegistration? = null

    private lateinit var playerId: String
    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Obtener el ID único del jugador
        playerId = PlayerManager.getPlayerId(this)

        // Restaurar y mostrar el nombre del jugador si ya existe
        playerName = PlayerManager.getPlayerName(this) ?: "Jugador Anónimo"
        binding.nameEditText.setText(playerName)

        setupRecyclerView()

        binding.createGameButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Guardar el nombre para futuras sesiones
            PlayerManager.savePlayerName(this, name)
            Toast.makeText(this, "Creando partida para $name...", Toast.LENGTH_SHORT).show()
            playerName = name
            createNewGame()
        }
    }

    override fun onStart() {
        super.onStart()
        listenForAvailableGames()
    }

    override fun onStop() {
        super.onStop()
        // Detener el listener para no gastar recursos cuando la app no está visible
        gamesListener?.remove()
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(mutableListOf()) { game ->
            // Acción al hacer clic en una partida para unirse
            joinGame(game)
        }
        binding.gamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.gamesRecyclerView.adapter = gameAdapter
    }

    private fun listenForAvailableGames() {
        binding.loadingIndicator.visibility = View.VISIBLE
        // Escuchar solo las partidas que están en estado "WAITING"
        val query = db.collection("games").whereEqualTo("status", GameStatus.WAITING.name)

        gamesListener = query.addSnapshotListener { snapshots, e ->
            binding.loadingIndicator.visibility = View.GONE
            if (e != null) {
                // Manejar error
                Toast.makeText(this, "Error al cargar partidas: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val gamesList = snapshots?.map { document ->
                // Convertir cada documento a un objeto Game
                val game = document.toObject(Game::class.java)
                game.gameId = document.id // Guardar el ID del documento
                game
            }?.filter { it.player1Id != playerId } // No mostrar las partidas que yo mismo creé

            if (gamesList != null) {
                gameAdapter.updateGames(gamesList)
            }
        }
    }

    private fun createNewGame() {
        val newGame = Game(
            player1Id = playerId,
            player1Name = playerName,
            status = GameStatus.WAITING
        )

        db.collection("games").add(newGame)
            .addOnSuccessListener { documentReference ->
                // Éxito al crear la partida
                val gameId = documentReference.id
                startGameActivity(gameId)
            }
            .addOnFailureListener { e ->
                // Error al crear la partida
                Toast.makeText(this, "Error al crear la partida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinGame(game: Game) {
        val gameId = game.gameId
        if (gameId == null) {
            Toast.makeText(this, "ID de partida inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // Unirse a la partida actualizando los campos del jugador 2
        db.collection("games").document(gameId)
            .update(
                mapOf(
                    "player2Id" to playerId,
                    "player2Name" to playerName,
                    "status" to GameStatus.IN_PROGRESS.name
                )
            )
            .addOnSuccessListener {
                // Éxito al unirse, iniciar la partida
                startGameActivity(gameId)
            }
            .addOnFailureListener { e ->
                // Error al unirse
                Toast.makeText(this, "Error al unirse a la partida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startGameActivity(gameId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("GAME_ID", gameId)
        }
        startActivity(intent)
    }

}