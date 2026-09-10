# language: fr
# Atelier du chapitre 6 (BDD) — scénario fourni, step definitions à écrire.
#
# Ce fichier n'est pas encore exécuté : RunCucumberTest ne sélectionne que
# recruitment.feature. Pour l'activer une fois RewardsSteps implémenté :
# élargir @SelectClasspathResource("features/recruitment.feature") en
# @SelectClasspathResource("features").

Fonctionnalité: Distribution des récompenses d'une quête terminée
  En tant que maître de guilde
  Je veux qu'un membre reçoive expérience et butin quand il termine une quête
  Afin que la progression de la guilde reflète le travail accompli

  Scénario: Un vétéran gagne expérience et butin en terminant une quête
    Soit un aventurier "Dante" de rang "VETERAN"
    Et une quête "Purger le donjon" de difficulté "HARD" rapportant 100 d'expérience et 100 d'or
    Quand "Dante" termine la quête "Purger le donjon"
    Alors "Dante" gagne 120 points d'expérience
    Et "Dante" reçoit 125 pièces d'or de butin
    Et "Dante" est notifié
