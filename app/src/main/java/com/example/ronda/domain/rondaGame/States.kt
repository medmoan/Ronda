package com.example.ronda.domain.rondaGame

import com.example.ronda.domain.card.User

enum class Difficulty {
    Easy, Medium, Hard
}
enum class Actions {
    None, Just_played, Makla, Darba, Taawida1, Taawida2, Messa, Last_hand
}
enum class Flag {
    None, Ronda, Tringa
}
data class RondaState(val userturn: User, val gameState: GameState)
enum class GameState {
    Ready, Play, End
}
sealed class EndResult {
    data class Win(val winnerScore: Int, val loserScore: Int) : EndResult()
    data class Lose(val loserScore: Int, val winnerScore: Int) : EndResult()
    data class Draw(val score: Int) : EndResult()
    data object None : EndResult()
}