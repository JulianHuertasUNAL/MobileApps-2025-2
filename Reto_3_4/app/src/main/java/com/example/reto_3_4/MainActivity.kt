package com.example.reto_3_4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reto_3_4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var mGame: TicTacToeGame
    private var mGameOver: Boolean = false

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
        binding.information.text = getString(R.string.first_human)
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
                mGameOver = true
            }
            2 -> {
                binding.information.text = getString(R.string.result_human_wins)
                mGameOver = true
            }
            3 -> {
                binding.information.text = getString(R.string.result_computer_wins)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(getString(R.string.menu_new_game))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startNewGame()
        return true
    }
}