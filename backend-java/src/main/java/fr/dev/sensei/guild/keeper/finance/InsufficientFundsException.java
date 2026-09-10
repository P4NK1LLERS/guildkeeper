package fr.dev.sensei.guild.keeper.finance;

/** Levee lorsqu'un debit depasse la solvabilite du compte de guilde. */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(int balance, int requestedAmount) {
        super("Solde insuffisant : solde " + balance + ", montant demande " + requestedAmount + ".");
    }
}
