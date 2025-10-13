package com.example.reto_3_4

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object PlayerManager {
    private const val PREFS_NAME = "player_prefs"
    private const val KEY_PLAYER_ID = "player_id"
    private const val KEY_PLAYER_NAME = "player_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getPlayerId(context: Context): String {
        val prefs = getPrefs(context)
        var playerId = prefs.getString(KEY_PLAYER_ID, null)
        if (playerId == null) {
            playerId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_PLAYER_ID, playerId).apply()
        }
        return playerId
    }

    fun getPlayerName(context: Context): String? {
        return getPrefs(context).getString(KEY_PLAYER_NAME, null)
    }

    fun savePlayerName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_PLAYER_NAME, name).apply()
    }
}