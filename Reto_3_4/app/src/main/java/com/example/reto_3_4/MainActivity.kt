package com.example.reto_3_4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reto_3_4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),DifficultyDialogFragment.DifficultyDialogListener,
QuitConfirmDialogFragment.QuitConfirmDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var mGame: TicTacToeGame
    private var mGameOver: Boolean = false
    private var humanFirstTurn: Boolean = true // true = human, false = computer
    private var humanScore: Int = 0
    private var androidScore: Int = 0
    private var ties: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mGame = TicTacToeGame()

        mBoardButtons = arrayOf(
            binding.one, binding.two, binding.three,
            binding.four, binding.five, binding.six,
            binding.seven, binding.eight, binding.nine
        )

        binding.humanScoreNumber.text = humanScore.toString()
        binding.androidScoreNumber.text = androidScore.toString()
        binding.tiesScoreNumber.text = ties.toString()

        startNewGame()
    }

    private fun startNewGame() {
        mGame.clearBoard()
        mGameOver = false
        for (i in mBoardButtons.indices) {
            mBoardButtons[i].text = ""
            mBoardButtons[i].isEnabled = true
            mBoardButtons[i].setOnClickListener { onBoardButtonClick(i) }
        }
        if (humanFirstTurn) {
            binding.information.text = getString(R.string.turn_human)
        }else{
            binding.information.text = getString(R.string.turn_computer)
            val move = mGame.getComputerMove()
            if (move >= 0) setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            binding.information.text = getString(R.string.turn_human)
        }
    }

    private fun onBoardButtonClick(location: Int) {
        if (mGameOver) return
        if (!mBoardButtons[location].isEnabled) return

        setMove(TicTacToeGame.HUMAN_PLAYER, location)
        var winner = mGame.checkForWinner()
        if (winner == 0) {
            binding.information.text = getString(R.string.turn_computer)
            val move = mGame.getComputerMove()
            if (move >= 0) setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            winner = mGame.checkForWinner()
        }

        when (winner) {
            0 -> binding.information.text = getString(R.string.turn_human)
            1 -> {
                binding.information.text = getString(R.string.result_tie)
                ties++
                binding.tiesScoreNumber.text = ties.toString()
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
            }
            2 -> {
                binding.information.text = getString(R.string.result_human_wins)
                humanScore++
                binding.humanScoreNumber.text = humanScore.toString()
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
            }
            3 -> {
                binding.information.text = getString(R.string.result_computer_wins)
                androidScore++
                binding.androidScoreNumber.text = androidScore.toString()
                humanFirstTurn = !humanFirstTurn
                mGameOver = true
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        val button = mBoardButtons[location]
        button.isEnabled = false
        button.text = player.toString()
        val colorRes = if (player == TicTacToeGame.HUMAN_PLAYER) R.color.tic_x else R.color.tic_o
        button.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    override fun onDifficultySelected(difficultyLevel: TicTacToeGame.DifficultyLevel) {
        mGame.setDifficultyLevel(difficultyLevel)
        Toast.makeText(this, "Difficulty selected: ${difficultyLevel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onQuitConfirmed() {
        finish() // Cierra la actividad
    }

    private fun showDifficultyDialog() {
        val dialog = DifficultyDialogFragment.newInstance(mGame.currentDifficultyLevel)
        dialog.setDifficultyDialogListener(this) // Usar el método setter
        dialog.show(supportFragmentManager, "DifficultyDialogTag")
    }

    private fun showQuitConfirmDialog() {
        val dialog = QuitConfirmDialogFragment.newInstance()
        dialog.setQuitConfirmDialogListener(this) // Usar el método setter
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
            R.id.quit -> {
                showQuitConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}