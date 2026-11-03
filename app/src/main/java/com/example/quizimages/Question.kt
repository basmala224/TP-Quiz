package com.example.quizimages

import androidx.annotation.DrawableRes

/**
 * Une question du quiz.
 *
 * @property texte énoncé affiché sous l'image
 * @property imageRes identifiant de l'image dans res/drawable
 * @property choix les trois réponses proposées
 * @property indiceBonneReponse position (0, 1 ou 2) de la bonne réponse dans [choix]
 * @property descriptionImage description lue par les outils d'accessibilité
 */
data class Question(
    val texte: String,
    @DrawableRes val imageRes: Int,
    val choix: Array<String>,
    val indiceBonneReponse: Int,
    val descriptionImage: String
)
