package fr.dev.sensei.guild.keeper.recruitment;

import java.util.Objects;

/**
 * Membre de la guilde.
 *
 * <p>L'identifiant, le nom et la chance sont fixes a la creation. Le rang et les
 * points d'experience evoluent au fil des quetes (modules rewards et promotion),
 * d'ou les deux mutateurs dedies.
 */
public class Member {

    private final String id;
    private final String name;
    private MemberRank rank;
    private int experiencePoints;
    private final int luck;

    public Member(String id, String name, MemberRank rank, int experiencePoints, int luck) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.rank = Objects.requireNonNull(rank, "rank");
        if (experiencePoints < 0) {
            throw new IllegalArgumentException("Les points d'experience ne peuvent pas etre negatifs : " + experiencePoints);
        }
        if (luck < 1 || luck > 10) {
            throw new IllegalArgumentException("La chance doit etre comprise entre 1 et 10 : " + luck);
        }
        this.experiencePoints = experiencePoints;
        this.luck = luck;
    }

    /** Cree un nouveau membre au rang NOVICE avec 0 point d'experience. */
    public static Member novice(String id, String name, int luck) {
        return new Member(id, name, MemberRank.NOVICE, 0, luck);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public MemberRank rank() {
        return rank;
    }

    public int experiencePoints() {
        return experiencePoints;
    }

    public int luck() {
        return luck;
    }

    /** Credite un gain d'experience (toujours strictement positif). */
    public void addExperience(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le gain d'experience doit etre strictement positif : " + amount);
        }
        this.experiencePoints += amount;
    }

    /** Positionne le membre sur un nouveau rang (utilise par le module promotion). */
    public void promoteTo(MemberRank newRank) {
        this.rank = Objects.requireNonNull(newRank, "newRank");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Member{id=%s, name=%s, rank=%s, xp=%d, luck=%d}".formatted(id, name, rank, experiencePoints, luck);
    }
}
