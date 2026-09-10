# language: fr
Fonctionnalité: Recrutement de membres de la guilde
  En tant que maître de guilde
  Je veux recruter de nouveaux membres selon des règles claires
  Afin que le registre de la guilde reste cohérent

  Contexte:
    Soit une guilde vide

  Règle: Un candidat n'est recruté que si son nom est renseigné et pas déjà pris

    Scénario: Recrutement réussi d'un nouveau candidat
      Quand je recrute le candidat "Dragan"
      Alors "Dragan" est membre de la guilde
      Et "Dragan" a le rang "NOVICE"
      Et "Dragan" a 0 point d'expérience

    Scénario: Rejet d'un candidat dont le nom est déjà pris
      Soit "Attila" déjà membre de la guilde
      Quand j'essaie de recruter le candidat "Attila"
      Alors le recrutement est rejeté pour cause de membre en double

    Scénario: Rejet d'un candidat au nom vide
      Quand j'essaie de recruter le candidat ""
      Alors le recrutement est rejeté car le nom est vide
