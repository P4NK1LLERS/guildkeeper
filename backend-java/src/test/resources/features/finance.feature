# language: fr
Fonctionnalité: Distribution de butin depuis le compte de la guilde

  En tant que maître de guilde
  Je veux distribuer une part du trésor à un membre qui a rempli une mission
  Afin de récompenser sa contribution sans jamais mettre le compte à découvert

  Contexte:
    Soit un compte de guilde avec un solde de 100 pièces d'or

  Scénario: Distribution de butin nominale à un membre
    Quand je distribue 30 pièces d'or de butin à "Dragan"
    Alors le compte de la guilde a un solde de 70 pièces d'or

  Scénario: Rejet d'une distribution supérieure au solde disponible
    Quand j'essaie de distribuer 150 pièces d'or de butin à "Dragan"
    Alors la distribution est rejetée pour cause de solde insuffisant
    Et le compte de la guilde a un solde de 100 pièces d'or
