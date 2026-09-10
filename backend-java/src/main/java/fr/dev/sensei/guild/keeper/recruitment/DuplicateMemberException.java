package fr.dev.sensei.guild.keeper.recruitment;

/**
 * Levee lorsqu'on tente de recruter un candidat dont le nom est deja porte par
 * un membre existant de la guilde.
 */
public class DuplicateMemberException extends RuntimeException {

    public DuplicateMemberException(String name) {
        super("Un membre nomme " + name + " existe deja dans la guilde.");
    }
}
