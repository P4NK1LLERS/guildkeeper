package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.recruitment.Member;

/**
 * Port de notification d'un membre. L'implementation reelle (email, message
 * in-game...) n'a pas d'interet pour le cours : une implementation fake est
 * fournie dans les sources de test ({@code FakeNotificationPort}).
 */
public interface NotificationPort {

    void notifyMember(Member member, String message);
}
