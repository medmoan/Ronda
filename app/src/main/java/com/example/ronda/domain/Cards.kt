package com.example.ronda.domain

import com.example.ronda.domain.card.Card

data class Cards(
    val cards: List<Card.Front>,
    var areCardsJustGenerated: Boolean = false
)