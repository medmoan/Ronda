package com.example.ronda


import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test


// Assuming CardType enum is accessible here
// enum class CardType { DHAB, TWAJEN, SYUFA, ZRAWET }

class CardTest {

    // --- Test DHAB Type (range.first == 1) ---
    @Test
    fun `positionCardOrder for DHAB type with num less than or equal to 7`() {
        // Test a few values within the num <= 7 range
        val card1 = Card.Front(CardType.Dhab, 1)
        assertEquals(1, card1.cardId) // num = 1 -> order 1

        val card7 = Card.Front(CardType.Dhab, 7)
        assertEquals(7, card7.cardId) // num = 7 -> order 7
    }

    @Test
    fun `positionCardOrder for DHAB type with num greater than 7`() {
        val card10 = Card.Front(CardType.Dhab, 10)
        assertEquals(8, card10.cardId) // num = 10 -> order 10-2=8

        val card12 = Card.Front(CardType.Zrawet, 12)
        assertEquals(40, card12.cardId) // num = 12 -> order 12+31-3=43-3=40

        val card11 = Card.Front(CardType.Syufa, 11)
        assertEquals(29, card11.cardId) // num = 11 -> order 11+21-3=32-3 -> 29
    }

    // --- Test TWAJEN Type (range.first != 1, specifically 11) ---
    @Test
    fun `positionCardOrder for TWAJEN type`() {
        // range.first (11) + num - 3
        val card11 = Card.Front(CardType.Twajen, 11) // 11 + 11 - 3 = 19
        assertEquals(19, card11.cardId)

    }


    // --- Test Validation in init block (prevents entering non existing card it is between 1 and 10 excluding 8 and 9 for each type) ---
    @Test
    fun `Card construction throws for invalid num 0`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Card.Front(CardType.Dhab, 0)
        }
        assertEquals("Invalid card number", exception.message) // Or your more specific message
    }

    @Test
    fun `Card construction throws for invalid num 8 (if part of global rule)`() {
        // This test depends on your exact init block validation for num 8,9
        // If the rule is num in 8..9 throws, then:
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Card.Front(CardType.Syufa, 8) // Assuming type does not matter for this rule
        }
        assertEquals("Invalid card number", exception.message)
    }

    @Test
    fun `Card construction throws for num greater than 12 (if part of global rule)`() {
        // This test depends on your exact init block validation for num > 12
        val exception = assertThrows(IllegalArgumentException::class.java) {
            // If this rule means ZRAWET 31 should fail, then Card init validation needs to be updated
            // Based on your current init: if (num == 0 || num in 8..9 || num > 12)
            Card.Front(CardType.Twajen, 13)
        }
        assertEquals("Invalid card number", exception.message)
    }


}
