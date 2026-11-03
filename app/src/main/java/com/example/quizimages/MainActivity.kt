package com.example.quizimages

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizimages.databinding.ActivityMainBinding

/**
 * Unique activité de l'application : elle affiche les questions, vérifie les
 * réponses, tient le score à jour et présente le résultat final.
 */
class MainActivity : AppCompatActivity() {

    /** Les cinq questions du quiz. La bonne réponse change de bouton d'une question à l'autre. */
    private val questions = arrayOf(
        Question(
            texte = "Combien de côtés possède cette forme ?",
            imageRes = R.drawable.triangle,
            choix = arrayOf("4 côtés", "3 côtés", "5 côtés"),
            indiceBonneReponse = 1,
            descriptionImage = "Une forme orange à trois côtés et trois pointes"
        ),
        Question(
            texte = "Quelle forme voyez-vous sur l'image ?",
            imageRes = R.drawable.carre,
            choix = arrayOf("Un cercle", "Un triangle", "Un carré"),
            indiceBonneReponse = 2,
            descriptionImage = "Une forme verte à quatre côtés de même longueur"
        ),
        Question(
            texte = "De quelle couleur est ce cercle ?",
            imageRes = R.drawable.cercle,
            choix = arrayOf("Bleu", "Rouge", "Vert"),
            indiceBonneReponse = 0,
            descriptionImage = "Un cercle plein de couleur bleue"
        ),
        Question(
            texte = "Combien de branches compte cette étoile ?",
            imageRes = R.drawable.etoile,
            choix = arrayOf("6 branches", "4 branches", "5 branches"),
            indiceBonneReponse = 2,
            descriptionImage = "Une étoile jaune à cinq branches"
        ),
        Question(
            texte = "Quelle est cette forme ?",
            imageRes = R.drawable.rectangle,
            choix = arrayOf("Un carré", "Un rectangle", "Un cercle"),
            indiceBonneReponse = 1,
            descriptionImage = "Une forme violette à quatre côtés, plus large que haute"
        )
    )

    private lateinit var binding: ActivityMainBinding

    /** Les trois boutons de choix : la position dans ce tableau correspond à l'indice du choix. */
    private lateinit var boutonsChoix: Array<Button>

    // État de la partie
    private var indiceQuestion = 0
    private var score = 0
    private var nombreReponses = 0
    private var reponseDonnee = false
    private var indiceChoixSelectionne = AUCUN_CHOIX
    private var partieTerminee = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appliquerMargesSysteme()

        boutonsChoix = arrayOf(binding.btnChoix1, binding.btnChoix2, binding.btnChoix3)

        // Chaque écouteur retient l'indice de son bouton : c'est ainsi que l'on sait quel choix a été cliqué.
        boutonsChoix.forEachIndexed { indice, bouton ->
            bouton.setOnClickListener { verifierReponse(indice) }
        }
        binding.btnSuivant.setOnClickListener { passerALaSuite() }
        binding.btnRejouer.setOnClickListener { reinitialiserQuiz() }

        savedInstanceState?.let { restaurerEtat(it) }
        afficherEtatCourant()
    }

    /** Sauvegarde la partie pour la retrouver après une rotation de l'écran. */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLE_INDICE_QUESTION, indiceQuestion)
        outState.putInt(CLE_SCORE, score)
        outState.putInt(CLE_NOMBRE_REPONSES, nombreReponses)
        outState.putBoolean(CLE_REPONSE_DONNEE, reponseDonnee)
        outState.putInt(CLE_CHOIX_SELECTIONNE, indiceChoixSelectionne)
        outState.putBoolean(CLE_PARTIE_TERMINEE, partieTerminee)
    }

    private fun restaurerEtat(etat: Bundle) {
        indiceQuestion = etat.getInt(CLE_INDICE_QUESTION)
        score = etat.getInt(CLE_SCORE)
        nombreReponses = etat.getInt(CLE_NOMBRE_REPONSES)
        reponseDonnee = etat.getBoolean(CLE_REPONSE_DONNEE)
        indiceChoixSelectionne = etat.getInt(CLE_CHOIX_SELECTIONNE, AUCUN_CHOIX)
        partieTerminee = etat.getBoolean(CLE_PARTIE_TERMINEE)
    }

    /** Évite que le contenu passe sous la barre d'état ou la barre de navigation. */
    private fun appliquerMargesSysteme() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutRacine) { vue, insets ->
            val marges = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            vue.setPadding(marges.left, marges.top, marges.right, marges.bottom)
            insets
        }
    }

    private fun afficherEtatCourant() {
        actualiserBandeau()
        if (partieTerminee) {
            afficherResultat()
        } else {
            afficherQuestion()
        }
    }

    /** Affiche la question courante, avec sa correction si elle a déjà reçu une réponse. */
    private fun afficherQuestion() {
        val question = questions[indiceQuestion]

        binding.zoneQuestion.visibility = View.VISIBLE
        binding.zoneResultat.visibility = View.GONE

        binding.txtNumeroQuestion.text =
            getString(R.string.numero_question_format, indiceQuestion + 1, questions.size)
        binding.imgQuestion.setImageResource(question.imageRes)
        binding.imgQuestion.contentDescription = question.descriptionImage
        binding.txtQuestion.text = question.texte

        boutonsChoix.forEachIndexed { indice, bouton ->
            bouton.text = question.choix[indice]
            bouton.isEnabled = !reponseDonnee
        }

        if (reponseDonnee) {
            afficherCorrection(question)
        } else {
            binding.txtCorrection.visibility = View.GONE
            binding.btnSuivant.visibility = View.GONE
        }
    }

    /** Valide le choix cliqué. Une question ne peut être validée qu'une seule fois. */
    private fun verifierReponse(indiceChoix: Int) {
        if (reponseDonnee || partieTerminee) return

        val question = questions[indiceQuestion]
        reponseDonnee = true
        indiceChoixSelectionne = indiceChoix
        nombreReponses++
        if (indiceChoix == question.indiceBonneReponse) {
            score++
        }

        boutonsChoix.forEach { it.isEnabled = false }
        actualiserBandeau()
        afficherCorrection(question)

        // Sur petit écran, on fait défiler pour montrer la correction et le bouton suivant.
        binding.scrollContenu.post { binding.scrollContenu.fullScroll(View.FOCUS_DOWN) }
    }

    /** Le message et le symbole (✓ ou ✗) indiquent le résultat : la couleur n'est qu'un complément. */
    private fun afficherCorrection(question: Question) {
        val estJuste = indiceChoixSelectionne == question.indiceBonneReponse

        binding.txtCorrection.text = if (estJuste) {
            getString(R.string.correction_juste)
        } else {
            getString(R.string.correction_fausse, question.choix[question.indiceBonneReponse])
        }
        binding.txtCorrection.setBackgroundColor(
            ContextCompat.getColor(this, if (estJuste) R.color.juste_fond else R.color.fausse_fond)
        )
        binding.txtCorrection.setTextColor(
            ContextCompat.getColor(this, if (estJuste) R.color.juste_texte else R.color.fausse_texte)
        )
        binding.txtCorrection.visibility = View.VISIBLE

        binding.btnSuivant.setText(
            if (estDerniereQuestion()) R.string.voir_resultat else R.string.question_suivante
        )
        binding.btnSuivant.visibility = View.VISIBLE
    }

    /** Met à jour le score et la progression du bandeau supérieur. */
    private fun actualiserBandeau() {
        binding.txtScore.text = getString(R.string.score_format, score, questions.size)
        binding.progressQuiz.max = questions.size
        binding.progressQuiz.progress = nombreReponses
        binding.txtProgression.text =
            getString(R.string.progression_format, nombreReponses, questions.size)
    }

    private fun estDerniereQuestion() = indiceQuestion == questions.lastIndex

    /** Passe à la question suivante ou, après la dernière, au résultat final. */
    private fun passerALaSuite() {
        if (!reponseDonnee) return

        if (estDerniereQuestion()) {
            partieTerminee = true
            afficherResultat()
        } else {
            indiceQuestion++
            reponseDonnee = false
            indiceChoixSelectionne = AUCUN_CHOIX
            afficherQuestion()
        }
        binding.scrollContenu.scrollTo(0, 0)
    }

    /** Masque la question et les choix, puis affiche le score final. */
    private fun afficherResultat() {
        binding.zoneQuestion.visibility = View.GONE
        binding.zoneResultat.visibility = View.VISIBLE

        binding.txtResultatScore.text =
            getString(R.string.resultat_score_format, score, questions.size)
        binding.txtResultatMessage.setText(
            when {
                score == questions.size -> R.string.resultat_message_parfait
                score >= 3 -> R.string.resultat_message_bien
                else -> R.string.resultat_message_rejouer
            }
        )
    }

    /** Bouton Rejouer : retour à la première question, score et progression à zéro. */
    private fun reinitialiserQuiz() {
        indiceQuestion = 0
        score = 0
        nombreReponses = 0
        reponseDonnee = false
        indiceChoixSelectionne = AUCUN_CHOIX
        partieTerminee = false

        afficherEtatCourant()
        binding.scrollContenu.scrollTo(0, 0)
    }

    private companion object {
        const val AUCUN_CHOIX = -1

        const val CLE_INDICE_QUESTION = "indice_question"
        const val CLE_SCORE = "score"
        const val CLE_NOMBRE_REPONSES = "nombre_reponses"
        const val CLE_REPONSE_DONNEE = "reponse_donnee"
        const val CLE_CHOIX_SELECTIONNE = "choix_selectionne"
        const val CLE_PARTIE_TERMINEE = "partie_terminee"
    }
}
