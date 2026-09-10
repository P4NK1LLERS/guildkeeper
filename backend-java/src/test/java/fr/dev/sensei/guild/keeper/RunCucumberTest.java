package fr.dev.sensei.guild.keeper;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Point d'entree JUnit 5 pour Cucumber.
 *
 * <p>Les fichiers {@code .feature} vivent sous
 * {@code src/test/resources/features/} et la glue sous
 * {@code src/test/java/.../cucumber/} (package
 * {@code fr.dev.sensei.guild.keeper.cucumber}), disposition classique d'un
 * projet Maven. La glue et l'affichage sont configures dans
 * {@code src/test/resources/junit-platform.properties}.
 *
 * <p>Remarque : {@code maven-surefire-plugin} affiche "Tests run: 0" pour ce
 * moteur (limitation connue de l'integration surefire / cucumber-junit-platform).
 * Le detail reel des scenarios est donne par les plugins Cucumber
 * {@code pretty} et {@code summary} dans la sortie de {@code mvn test}.
 *
 * <p>Atelier chapitre 6 : {@code features/rewards.feature} est fourni mais pas
 * execute tant que la selection cible un seul fichier. Une fois {@code RewardsSteps}
 * implemente, remplacer la ligne ci-dessous par
 * {@code @SelectClasspathResource("features")} pour executer tout le dossier.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/recruitment.feature")
@SelectClasspathResource("features/finance.feature")
public class RunCucumberTest {
}
