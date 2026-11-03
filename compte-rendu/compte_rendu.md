# Compte rendu — TP Android : quiz en images

**Étudiante :** Basmala Aouida
**Thème choisi :** les formes géométriques (triangle, carré, cercle, étoile, rectangle)

## 1. Présentation de l'interface et des widgets

L'application tient dans une seule activité, `MainActivity`, sans fragment. Son interface est définie dans `res/layout/activity_main.xml`, et les widgets sont accédés avec View Binding.

| Widget | Identifiant | Rôle |
|---|---|---|
| `TextView` | `txt_titre` | Titre, en haut de l'écran |
| `TextView` | `txt_score` | Score, en haut à droite |
| `ProgressBar` horizontale | `progress_quiz` | Nombre de réponses données |
| `TextView` | `txt_progression` | Progression écrite, par exemple « 2 sur 5 » |
| `TextView` | `txt_numero_question` | « Question 2 sur 5 » |
| `ImageView` | `img_question` | Image de la question |
| `TextView` | `txt_question` | Texte de la question |
| 3 `Button` | `btn_choix1` à `btn_choix3` | Les trois réponses |
| `TextView` | `txt_correction` | Message de correction |
| `Button` | `btn_suivant` | « Question suivante » ou « Voir le résultat » |
| `LinearLayout` | `zone_resultat` | Zone du résultat final |
| `Button` | `btn_rejouer` | Relance une partie |

Le bandeau (titre, score, progression) est placé hors du `ScrollView` : il reste visible pendant que le reste de l'écran défile sur un petit écran. L'image utilise `scaleType="fitCenter"`, qui conserve ses proportions et évite toute déformation.

## 2. Tableau des questions et association des images

Une question est représentée par la data class `Question` :

```kotlin
data class Question(
    val texte: String,
    @DrawableRes val imageRes: Int,
    val choix: Array<String>,
    val indiceBonneReponse: Int,
    val descriptionImage: String
)
```

Les cinq questions sont déclarées de façon statique dans le tableau `questions` de `MainActivity`. Chacune référence son image par son identifiant de ressource, par exemple `R.drawable.triangle`. Les images sont des drawables vectoriels XML.

| N° | Image | Question | Bonne réponse | Indice |
|---|---|---|---|---|
| 1 | `triangle` | Combien de côtés possède cette forme ? | 3 côtés | 1 |
| 2 | `carre` | Quelle forme voyez-vous sur l'image ? | Un carré | 2 |
| 3 | `cercle` | De quelle couleur est ce cercle ? | Bleu | 0 |
| 4 | `etoile` | Combien de branches compte cette étoile ? | 5 branches | 2 |
| 5 | `rectangle` | Quelle est cette forme ? | Un rectangle | 1 |

La bonne réponse n'est pas toujours sur le même bouton.

## 3. Événements, score et progression

- **Clic sur un choix :** `verifierReponse(indice)` compare l'indice du bouton à `indiceBonneReponse`. Si la réponse est juste, le score augmente de 1. Dans tous les cas, le nombre de réponses augmente de 1, les trois boutons sont désactivés et la correction s'affiche.
- **Bouton suivant :** `passerALaSuite()` passe à la question suivante ou, après la dernière, au résultat. Il n'apparaît qu'après une réponse.
- **Bouton Rejouer :** `reinitialiserQuiz()` remet tout l'état à zéro et réaffiche la première question.
- **Rotation :** `onSaveInstanceState` sauvegarde la partie et `onCreate` la restaure (amélioration facultative).

La correction ne repose pas seulement sur la couleur : elle comporte un symbole (✓ ou ✗) et une phrase qui indique la bonne réponse en cas d'erreur.

## 4. Réponses aux questions de l'énoncé

**Comment représentez-vous une question et sa bonne réponse ?**
Par un objet `Question` du tableau `questions`. La bonne réponse est l'indice (0, 1 ou 2) de la réponse correcte dans le tableau `choix`.

**Comment le clic sur un bouton permet-il de savoir quel choix a été sélectionné ?**
Les boutons sont rangés dans le tableau `boutonsChoix`, dans le même ordre que les choix. Dans `forEachIndexed`, chaque écouteur retient l'indice de son bouton et le transmet à `verifierReponse(indice)`.

**Comment empêchez-vous l'attribution de plusieurs points pour une question ?**
Par deux protections : les trois boutons sont désactivés après la réponse, et `verifierReponse` s'arrête immédiatement si `reponseDonnee` vaut déjà `true`. Le second contrôle couvre aussi deux clics très rapprochés.

**Quelle différence faites-vous entre le score et la progression ?**
Le score (`score`) compte les bonnes réponses. La progression (`nombreReponses`) compte toutes les réponses données. Une réponse fausse fait donc avancer la barre sans changer le score.

**Comment détectez-vous la dernière question et affichez-vous le résultat ?**
`estDerniereQuestion()` teste `indiceQuestion == questions.lastIndex`. Le bouton affiche alors « Voir le résultat ». Au clic, `partieTerminee` passe à `true` et `afficherResultat()` masque la zone de question et affiche le score final. L'indice n'est jamais augmenté après la dernière question, donc le tableau ne peut pas être dépassé.

## 5. Tests réalisés

Appareil de test : *à compléter (par exemple Pixel 7, API 36)*.

| Test | Résultat |
|---|---|---|
| Lancer une nouvelle partie | Première question, trois choix, score 0/5, progression 0/5 
| Donner une bonne réponse | Score +1 et progression +1 
| Donner une mauvaise réponse | Score inchangé et progression +1 
| Appuyer plusieurs fois sur un choix | Aucun point supplémentaire, trois choix désactivés 
| Passer à la question suivante | Image, texte et choix actualisés, score conservé 
| Répondre juste aux cinq questions | Score final 5/5 et progression 5/5 
| Répondre faux aux cinq questions | Score final 0/5 et progression 5/5 
| Mélanger réponses justes et fausses | Score égal au nombre de réponses justes 
| Utiliser Rejouer | Première question, score et progression à zéro 
| Tester sur un petit écran | Tous les choix accessibles, textes lisibles 
| Tourner l'écran en cours de partie (facultatif) | Question, score et correction conservés 

## 6. Difficultés rencontrées

- **Barres système :** sur les versions récentes d'Android, l'application s'affiche sous la barre d'état et la barre de navigation. Des marges calculées avec `setOnApplyWindowInsetsListener` gardent le titre et le score visibles.
- **Petits écrans :** la correction et le bouton suivant pouvaient se trouver sous le bas de l'écran. Un `ScrollView` et un défilement automatique après la réponse règlent ce point.
- **Lisibilité des boutons désactivés :** les couleurs par défaut étaient trop pâles. Des couleurs dédiées à l'état désactivé gardent les réponses lisibles.
