package fr.dev.sensei.guild.keeper.cucumber;

import fr.dev.sensei.guild.keeper.recruitment.DuplicateMemberException;
import fr.dev.sensei.guild.keeper.recruitment.InMemoryMemberRepository;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import fr.dev.sensei.guild.keeper.recruitment.RecruitmentService;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Step definitions de {@code features/recruitment.feature}.
 *
 * <p>Une nouvelle instance est creee par Cucumber pour chaque scenario : les
 * champs ci-dessous forment donc l'etat isole d'un scenario.
 */
public class RecruitmentSteps {

    private MemberRepository memberRepository;
    private RecruitmentService recruitmentService;
    private Throwable caughtException;

    @Soit("une guilde vide")
    public void une_guilde_vide() {
        memberRepository = new InMemoryMemberRepository();
        recruitmentService = new RecruitmentService(memberRepository);
        caughtException = null;
    }

    @Soit("{string} déjà membre de la guilde")
    public void deja_membre_de_la_guilde(String name) {
        memberRepository.save(Member.novice(UUID.randomUUID().toString(), name, 1));
    }

    @Quand("je recrute le candidat {string}")
    public void je_recrute_le_candidat(String name) {
        recruitmentService.recruit(name);
    }

    @Quand("j'essaie de recruter le candidat {string}")
    public void j_essaie_de_recruter_le_candidat(String name) {
        caughtException = catchThrowable(() -> recruitmentService.recruit(name));
    }

    @Alors("{string} est membre de la guilde")
    public void est_membre_de_la_guilde(String name) {
        assertThat(memberRepository.findByName(name)).isPresent();
    }

    @Et("{string} a le rang {string}")
    public void a_le_rang(String name, String rank) {
        assertThat(memberRepository.findByName(name)).get()
                .extracting(Member::rank)
                .isEqualTo(MemberRank.valueOf(rank));
    }

    @Et("{string} a {int} point(s) d'expérience")
    public void a_points_d_experience(String name, int experiencePoints) {
        assertThat(memberRepository.findByName(name)).get()
                .extracting(Member::experiencePoints)
                .isEqualTo(experiencePoints);
    }

    @Alors("le recrutement est rejeté pour cause de membre en double")
    public void le_recrutement_est_rejete_pour_cause_de_membre_en_double() {
        assertThat(caughtException).isInstanceOf(DuplicateMemberException.class);
    }

    @Alors("le recrutement est rejeté car le nom est vide")
    public void le_recrutement_est_rejete_car_le_nom_est_vide() {
        assertThat(caughtException).isInstanceOf(IllegalArgumentException.class);
    }
}
