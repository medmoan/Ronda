package com.example.ronda.domain.card

import com.example.ronda.domain.rondaGame.Flag

enum class User {
        MYUSER,
        OTHERUSER,
        NONE
}
sealed class Card {
     data class Front(
             val type: CardType,
             val num: Int,
             var owner: User? = null
     ): Card() {

             val cardId get() = calculateAssetId()

             init {
                    validateCard(num)
             }
             /**
              * Validates if the card number is within the allowed ranges (1-7 or 10-12).
              * Throws IllegalArgumentException if the number is invalid.
              * @param num The card number to validate.
              * @throws IllegalArgumentException if the [num] is not in the ranges 1-7 or 10-12.
              */
             private fun validateCard(num: Int) {
                     val isValidNumber = (num in 1..7) || (num in 10..12)
                     require(isValidNumber) { "Invalid card number: $num. Must be in ranges 1-7 or 10-12." }
             }
             private val cardTypeRanges = mapOf(
                     CardType.Dhab to (1..10),
                     CardType.Twajen to (11..20),
                     CardType.Syufa to (21..30),
                     CardType.Zrawet to (31..40)
             )
             override fun toString(): String {
                     return "Card type='$type', number='$num')"
             }
             override fun equals(other: Any?): Boolean {
                     if (this === other) return true
                     if (javaClass != other?.javaClass) return false
                     other as Card.Front
                     return cardId == other.cardId // Or type == other.type && num == other.num
             }

             override fun hashCode(): Int {
                     return cardId.hashCode() // Or combine hashes of type and num
             }

             /**
              * @return Order number of card in asset/ folder which can be an Id(unique)
              */
             private fun calculateAssetId(): Int {
                     val baseOffset = when (type) { // Use lowercase for robust matching
                             CardType.Dhab -> 0    // Golds: 1-10
                             CardType.Twajen -> 10   // Cups: 11-20
                             CardType.Syufa -> 20  // Swords: 21-30
                             CardType.Zrawet -> 30  // Clubs: 31-40
                     }

                     // The 'num' for face cards (10, 11, 12) maps to 8th, 9th, 10th card of the suit
                     val valueInSuit = when (num) {
                             in 1..7 -> num    // Cards 1 through 7 are direct
                             10 -> 8           // Sota (10) is the 8th card in the suit sequence
                             11 -> 9           // Caballo (11) is the 9th card
                             12 -> 10          // Rey (12) is the 10th card
                             else -> throw IllegalArgumentException("Invalid card number for type $type: $num")
                     }

                     return baseOffset + valueInSuit
             }

     }
        data class Flag(val flags: com.example.ronda.domain.rondaGame.Flag = com.example.ronda.domain.rondaGame.Flag.None): Card()
        data object Back: Card()
}

fun getCell(card: Card) {
        when (card) {
            Card.Back -> println("Back card $card")
            is Card.Front -> println("Front card ${card.cardId} ${card.type} ${card.num}")
            is Card.Flag -> { println("Flag card ${card.flags}") }
        }
}
fun main() {
        val front = Card.Front(CardType.Twajen, 12)
        val back = Card.Back
        val flag = Card.Flag(Flag.Ronda)
        getCell(
                flag
        )
}