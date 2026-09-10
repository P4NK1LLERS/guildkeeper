package fr.dev.sensei.guild.keeper.finance;

/** Levee lorsqu'un montant nul ou negatif est fourni a une operation financiere. */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(int amount) {
        super("Montant invalide (doit etre strictement positif) : " + amount);
    }
}
