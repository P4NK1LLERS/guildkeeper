package fr.dev.sensei.guild.keeper.finance;

import java.util.Objects;

/**
 * Compte financier d'une guilde. Le solde est exprime en pieces d'or (entier) et
 * ne doit jamais devenir negatif.
 */
public class GuildAccount {

    private final String guildId;
    private int balance;

    public GuildAccount(String guildId, int balance) {
        this.guildId = Objects.requireNonNull(guildId, "guildId");
        if (balance < 0) {
            throw new IllegalArgumentException("Le solde initial ne peut pas etre negatif : " + balance);
        }
        this.balance = balance;
    }

    public String guildId() {
        return guildId;
    }

    public int balance() {
        return balance;
    }

    /** Credite le compte. Reserve au {@link GuildFinanceService}. */
    void increaseBy(int amount) {
        this.balance += amount;
    }

    /** Debite le compte. Reserve au {@link GuildFinanceService}. */
    void decreaseBy(int amount) {
        if (amount > balance) {
            throw new IllegalStateException("Debit superieur au solde disponible.");
        }
        this.balance -= amount;
    }
}
