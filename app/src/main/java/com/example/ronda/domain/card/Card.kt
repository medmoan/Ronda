package com.example.ronda.domain.card


sealed class Card() {
     data class Front(val type: CardType, val num: Int): Card() {

             val cardId get() = calculateCardId()

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
             private fun calculateCardId(): Int {
                     val currentRange = cardTypeRanges[type]
                             ?: throw NoSuchElementException("No range defined for card type: $type")

                     return if (currentRange.first == 1) {
                             if (num <= 7) num
                             else num - 2
                     }
                     else currentRange.first + num - 3
             }

     }
     data object Back: Card()
}

fun getCell(card: Card) {
        when (card) {
            Card.Back -> println("Back card $card")
            is Card.Front -> println("Front card ${card.cardId} ${card.type} ${card.num}")
        }
}
fun main() {
        val front = Card.Front(CardType.Twajen, 12)
        val back = Card.Back
        getCell(
                front
        )
}