package com.example.ronda.domain.card




data class Card(
        val type: CardType,
        val num: Int,
        var currentCell: Pair<Int, Int>? = null // (row, col) if placed, else null
){
        // 1 10 dhab
        // 11 20 twajn
        // 21 30 syufa
        // 31 40 zrawet

        private val cardTypeRanges = mapOf(
                CardType.DHAB to (1..10),
                CardType.TWAJEN to (11..20),
                CardType.SYUFA to (21..30),
                CardType.ZRAWET to (31..40)
        )
        val cardId get() = calculateCardId()

        init {
            if (num == 0 || num in 8..9 || num > 12)  throw IllegalArgumentException("Invalid card number")
        }
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


data object BackCard
