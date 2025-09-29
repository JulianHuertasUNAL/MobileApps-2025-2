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


class MainActivity : AppCompatActivity(),DifficultyDialogFragment.DifficultyDialogListener,
QuitConfirmDialogFragment.QuitConfirmDialogListener, BoardView.OnCellTouchListener {

    private lateinit var binding: ActivityMainBinding
    //private lateinit var mBoardButtons: Array<Button>
    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardView: BoardView
    private var mGameOver: Boolean = false
    private var humanFirstTurn: Boolean = true // true = human, false = computer
    private var humanScore: Int = 0
    private var androidScore: Int = 0
    private var ties: Int = 0
    private var isHumanTurn: Boolean = true
    private val computerMoveHandler = Handler(Looper.getMainLooper())
    private var mSoundPool: SoundPool? = null
    private var mHumanSoundId: Int = 0
    private var mComputerSoundId: Int = 0
    private var mSoundPoolLoaded: Boolean = false

    private lateinit var mPrefs: SharedPreferences


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState) // Siempre llama a la superclase primero

        outState.putCharArray("board", mGame.getBoardCopy()) // Usando el método existente
        outState.putBoolean("mGameOver", mGameOver)
//        outState.putInt("humanScore", humanScore) // No es necesario Integer.valueOf() en Kotlin
//        outState.putInt("androidScore", androidScore)
//        outState.putInt("ties", ties)
        outState.putCharSequence("infoText", binding.information.text) // Guarda el texto del TextView de información
        outState.putBoolean("humanFirstTurn", humanFirstTurn) // El tutorial lo llama mGoFirst
        outState.putBoolean("isHumanTurn", isHumanTurn) // También guarda de quién es el turno actual
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE)
        mGame = TicTacToeGame()
        mBoardView = binding.boardView // Acceso directo a través de binding
        //mBoardView.setGame(mGame)
        mBoardView.setOnCellTouchListener(this)

        /*mBoardButtons = arrayOf(
            binding.one, binding.two, binding.three,
            binding.four, binding.five, binding.six,
            binding.seven, binding.eight, binding.nine
        )*/



        // Configurar AudioAttributes para SoundPool (necesario para API 21+)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME) // Indica que es para juegos
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Sonidos de UI/eventos
            .build()

        // Crear la instancia de SoundPool
        // El primer parámetro es maxStreams: cuántos sonidos pueden reproducirse simultáneamente.
        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(2) // Permitir que ambos sonidos se reproduzcan si es necesario
            .build()

        // Listener para saber cuándo los sonidos se han cargado (opcional pero bueno para la fiabilidad)
        mSoundPool?.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) { // status 0 significa éxito
                // Podrías tener una lógica más granular aquí si cargas muchos sonidos
                // Por ahora, asumimos que si uno carga, todos están listos o lo estarán pronto.
                // O podrías contar cuántos sonidos esperas cargar.
                mSoundPoolLoaded = true
            } else {
                // Log.e("SoundPool", "Error al cargar sonido ID $sampleId, estado $status")
                Toast.makeText(this, "Error al cargar sonidos", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar los sonidos. Guárdalos en variables miembro para usarlos después.
        // Reemplaza R.raw.human_move y R.raw.computer_move con los nombres de tus archivos.
        mHumanSoundId = mSoundPool?.load(this, R.raw.human_move, 1) ?: 0
        mComputerSoundId = mSoundPool?.load(this, R.raw.computer_move, 1) ?: 0
        // El tercer parámetro (priority) es 1 por defecto, no es crítico para pocos sonidos.


        if (savedInstanceState == null) {
            // La Activity se crea por primera vez
            restoreGameSettingsFromPreferences()
            updateScoreDisplay() // Para inicializar los scores a 0 en la UI
            startNewGame()       // Inicia un juego completamente nuevo
        } else {
            // La Activity se está recreando, restaurar estado
            val boardChars = savedInstanceState.getCharArray("board")
            if (boardChars != null) {
                mGame.setBoardState(boardChars)
            }
            mGameOver = savedInstanceState.getBoolean("mGameOver")
//            humanScore = savedInstanceState.getInt("humanScore")
//            androidScore = savedInstanceState.getInt("androidScore")
//            ties = savedInstanceState.getInt("ties")
            binding.information.text = savedInstanceState.getCharSequence("infoText", getString(R.string.turn_human)) // Restaurar infoText

            // El tutorial usa 'mGoFirst' como Char, pero tú lo tienes como 'humanFirstTurn' Boolean
            // y también 'isHumanTurn' Boolean.
            humanFirstTurn = savedInstanceState.getBoolean("humanFirstTurn", true)
            isHumanTurn = savedInstanceState.getBoolean("isHumanTurn", true)

            restoreGameSettingsFromPreferences()

            // Si el juego estaba terminado, nos aseguramos que no sea turno del humano
            // y que el handler de movimientos del PC esté limpio.
            if (mGameOver) {
                isHumanTurn = false
                computerMoveHandler.removeCallbacksAndMessages(null)
            }
            // Si no está game over, y NO es turno del humano (es decir, el PC estaba "pensando" o iba a jugar),
            // podría ser necesario reiniciar el handler del PC.
            // Pero por ahora, restaurar isHumanTurn y el texto de información debería ser suficiente
            // para que el usuario sepa de quién es el turno. Un movimiento pendiente del PC con delay se perderá.
        }

        mBoardView.setGame(mGame) // Ahora establece el juego en BoardView, ya sea nuevo o restaurado
        updateScoreDisplay()      // Mostrar las puntuaciones (ya sean 0s o restauradas)
        mBoardView.invalidate()   // Asegura que el tablero (nuevo o restaurado) se dibuje correctamente
    }

    private fun restoreGameSettingsFromPreferences() {
        humanScore = mPrefs.getInt("humanScore", 0) // El tutorial usa mHumanWins
        androidScore = mPrefs.getInt("androidScore", 0) // El tutorial usa mComputerWins
        ties = mPrefs.getInt("ties", 0) // El tutorial usa mTies

        val difficultyOrdinal = mPrefs.getInt("difficultyLevel", TicTacToeGame.DifficultyLevel.Expert.ordinal)
        mGame.setDifficultyLevel(TicTacToeGame.difficultyLevelFromOrdinal(difficultyOrdinal))
    }

    private fun updateScoreDisplay() {
        binding.humanScoreNumber.text = humanScore.toString()
        binding.androidScoreNumber.text = androidScore.toString()
        binding.tiesScoreNumber.text = ties.toString()
    }

    private fun startNewGame() {
        mGame.clearBoard()
        mBoardView.invalidate() // Redibuja la vista
        mGameOver = false
        computerMoveHandler.removeCallbacksAndMessages(null)
//        for (i in mBoardButtons.indices) {
//            mBoardButtons[i].text = ""
//            mBoardButtons[i].isEnabled = true
//            mBoardButtons[i].setOnClickListener { onBoardButtonClick(i) }
//        }
        isHumanTurn = humanFirstTurn
        if (isHumanTurn) {
            binding.information.text = getString(R.string.turn_human)
        }else{
            binding.information.text = getString(R.string.turn_computer)
            // El computador hace el primer movimiento, pero con retraso
            isHumanTurn = false // Marcar que no es turno del humano
            computerMoveHandler.postDelayed({
                if (!mGameOver) { // Comprobar si el juego no ha sido reiniciado/terminado mientras esperaba
                    val computerMove = mGame.getComputerMove()
                    if (computerMove != -1) {
                        mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                        if (mSoundPoolLoaded) mSoundPool?.play(mComputerSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                        mBoardView.invalidate()
                    }
                    // Después del movimiento del PC, es turno del humano

                    binding.information.text = getString(R.string.turn_human)
                    isHumanTurn = true
                }
            }, 1000) // Retraso de 1 segundo para el primer movimiento del PC
        }
    }

//    private fun onBoardButtonClick(location: Int) {
//        if (mGameOver) return
//        if (!mBoardButtons[location].isEnabled) return
//
//        setMove(TicTacToeGame.HUMAN_PLAYER, location)
//        var winner = mGame.checkForWinner()
//        if (winner == 0) {
//            binding.information.text = getString(R.string.turn_computer)
//            val move = mGame.getComputerMove()
//            if (move >= 0) setMove(TicTacToeGame.COMPUTER_PLAYER, move)
//            winner = mGame.checkForWinner()
//        }
//
//        when (winner) {
//            0 -> binding.information.text = getString(R.string.turn_human)
//            1 -> {
//                binding.information.text = getString(R.string.result_tie)
//                ties++
//                binding.tiesScoreNumber.text = ties.toString()
//                humanFirstTurn = !humanFirstTurn
//                mGameOver = true
//            }
//            2 -> {
//                binding.information.text = getString(R.string.result_human_wins)
//                humanScore++
//                binding.humanScoreNumber.text = humanScore.toString()
//                humanFirstTurn = !humanFirstTurn
//                mGameOver = true
//            }
//            3 -> {
//                binding.information.text = getString(R.string.result_computer_wins)
//                androidScore++
//                binding.androidScoreNumber.text = androidScore.toString()
//                humanFirstTurn = !humanFirstTurn
//                mGameOver = true
//            }
//        }
//    }

//    private fun setMove(player: Char, location: Int) {
//        mGame.setMove(player, location)
//        val button = mBoardButtons[location]
//        button.isEnabled = false
//        button.text = player.toString()
//        val colorRes = if (player == TicTacToeGame.HUMAN_PLAYER) R.color.tic_x else R.color.tic_o
//        button.setTextColor(ContextCompat.getColor(this, colorRes))
//    }

    override fun onCellTouched(cellIndex: Int) {
        if (mGameOver || !isHumanTurn || !mSoundPoolLoaded) {
            if (!mSoundPoolLoaded && !mGameOver) { // Solo mostrar si no es porque el juego terminó
                Toast.makeText(this, "Sonidos aún no cargados", Toast.LENGTH_SHORT).show()
            }
            if (!isHumanTurn && !mGameOver) {
                Toast.makeText(this, "Espera el turno del computador", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Verificar si la celda está ocupada (usando el modelo del juego)
        if (mGame.getBoardOccupant(cellIndex) != TicTacToeGame.OPEN_SPOT) {
            // Opcional: Mostrar Toast si la celda está ocupada
            // Toast.makeText(this, "Celda ocupada.", Toast.LENGTH_SHORT).show()
            return
        }

        // Intento de movimiento del humano
        if (mGame.setMove(TicTacToeGame.HUMAN_PLAYER, cellIndex)) {
            mBoardView.invalidate() // Redibujar para mostrar el movimiento del humano
            isHumanTurn = false // Inmediatamente después del movimiento del humano, ya no es su turno
            // Reproducir sonido del humano
            mSoundPool?.play(mHumanSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            // Parámetros de play:
            // soundID: el ID devuelto por load()
            // leftVolume, rightVolume: 0.0 a 1.0
            // priority: 0 es la más baja. Útil si tienes más streams que maxStreams.
            // loop: 0 para no repetir, -1 para repetir indefinidamente, >0 para N repeticiones.
            // rate: velocidad de reproducción, 1.0 es normal, 0.5 es media velocidad, 2.0 doble.
            var winner = mGame.checkForWinner()
            if (winner == 0) { // Si no hay ganador, turno del computador
                binding.information.text = getString(R.string.turn_computer)
                // Usar un Handler para un pequeño retraso antes del movimiento del computador (opcional)
                computerMoveHandler.postDelayed({
                    if (!mGameOver) { // Comprobar de nuevo, por si acaso
                        val computerMove = mGame.getComputerMove()
                        if (computerMove != -1) {
                            mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                            mBoardView.invalidate() // Redibujar para mostrar el movimiento del computador
                            mSoundPool?.play(mComputerSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
                        }
                        winner = mGame.checkForWinner() // Comprobar ganador de nuevo
                        handleGameResult(winner)
                    }
                }, 500) // Retraso de 0.5 segundos, ajústalo
            } else {
                handleGameResult(winner) // El humano ganó o hubo empate en su turno
            }
        }
    }

    private fun handleGameResult(winner: Int) {
        var messageSet = false
        when (winner) {
            0 -> { // Sigue el juego (o turno del humano si el computador ya jugó)
                if (!mGameOver) { // Solo si mGameOver no fue ya establecido por el otro jugador
                    binding.information.text = getString(R.string.turn_human)
                    isHumanTurn = true
                }
            }
            1 -> {
                binding.information.text = getString(R.string.result_tie)
                ties++
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
                messageSet = true
            }
            2 -> {
                binding.information.text = getString(R.string.result_human_wins)
                humanScore++
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
                messageSet = true
            }
            3 -> {
                binding.information.text = getString(R.string.result_computer_wins)
                androidScore++
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
                messageSet = true
            }
        }
        if (messageSet) { // Solo actualiza puntuación si el juego realmente terminó (o empate)
            updateScoreDisplay()
        }
    }

    override fun onDifficultySelected(difficultyLevel: TicTacToeGame.DifficultyLevel) {
        mGame.setDifficultyLevel(difficultyLevel)
        Toast.makeText(this, "Difficulty selected: ${difficultyLevel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onQuitConfirmed() {
        finish() // Cierra la actividad
    }

    override fun onDestroy() {
        super.onDestroy()
        mSoundPool?.release() // Liberar recursos de SoundPool
        mSoundPool = null
    }

    private fun showDifficultyDialog() {
        val dialog = DifficultyDialogFragment.newInstance(mGame.currentDifficultyLevel)
        dialog.setDifficultyDialogListener(this) // Usar el metodo setter
        dialog.show(supportFragmentManager, "DifficultyDialogTag")
    }

    private fun showQuitConfirmDialog() {
        val dialog = QuitConfirmDialogFragment.newInstance()
        dialog.setQuitConfirmDialogListener(this) // Usar el metodo setter
        dialog.show(supportFragmentManager, "QuitConfirmDialogTag")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { // item.itemId corresponde al android:id del XML
            R.id.new_game -> {
                startNewGame()
                true
            }
            R.id.ai_difficulty -> {
                // Lógica para mostrar diálogo de dificultad
                showDifficultyDialog()
                true
            }
            R.id.reset_scores -> {
                humanScore = 0
                androidScore = 0
                ties = 0
                updateScoreDisplay() // Actualizar la UI
                // Opcional: También guardar inmediatamente estos 0s en SharedPreferences
                // para que se refleje si la app se cierra antes de onStop()
                val ed: SharedPreferences.Editor = mPrefs.edit()
                ed.putInt("humanScore", humanScore)
                ed.putInt("androidScore", androidScore)
                ed.putInt("ties", ties)
                ed.apply() // Usar apply aquí está bien
                Toast.makeText(this, "Scores reset!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()

        // Guardar las puntuaciones actuales en SharedPreferences
        mPrefs.edit {
            putInt("humanScore", humanScore)     // Usando tus nombres de variable
            putInt("androidScore", androidScore) // Usando tus nombres de variable
            putInt("ties", ties)                 // Usando tus nombres de variable
            putInt("difficultyLevel", mGame.currentDifficultyLevel.ordinal)
        } // .apply() es asíncrono y generalmente preferido sobre .commit()
        //ed.commit() // .commit() es síncrono, el tutorial lo usa. Para pocas escrituras, está bien.
    }
}