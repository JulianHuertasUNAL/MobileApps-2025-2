package com.example.reto_3_4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameAdapter(
    private val games: MutableList<Game>,
    private val onGameClicked: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameIdText: TextView = view.findViewById(R.id.game_id_text)
        val player1NameText: TextView = view.findViewById(R.id.player1_name_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameIdText.text = "Partida de ${game.player1Name}"
        holder.player1NameText.text = "Toca para unirte"

        holder.itemView.setOnClickListener {
            onGameClicked(game)
        }
    }

    override fun getItemCount() = games.size

    fun updateGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}