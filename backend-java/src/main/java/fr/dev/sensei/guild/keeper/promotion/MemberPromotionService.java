package fr.dev.sensei.guild.keeper.promotion;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;

import java.util.Map;
import java.util.Optional;

/**
 * Promotion d'un membre au rang immediatement superieur des lors que son
 * experience atteint le seuil du rang vise.
 *
 * <p>Seuils (experience requise pour atteindre le rang) :
 * APPRENTICE 100, VETERAN 300, ELITE 700, GUILD_MASTER 1500.
 *
 * <p>Un membre deja GUILD_MASTER ne peut plus etre promu. La promotion se fait
 * d'un seul rang a la fois, meme si l'experience couvre plusieurs paliers.
 */
public class MemberPromotionService {

    private static final Map<MemberRank, Integer> EXPERIENCE_THRESHOLD = Map.of(
            MemberRank.APPRENTICE, 100,
            MemberRank.VETERAN, 300,
            MemberRank.ELITE, 700,
            MemberRank.GUILD_MASTER, 1500);

    private final MemberRepository memberRepository;

    public MemberPromotionService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Promeut le membre si son experience atteint le seuil du rang suivant.
     *
     * @return le nouveau rang si une promotion a eu lieu, {@link Optional#empty()} sinon
     * @throws IllegalArgumentException si le membre est introuvable
     */
    public Optional<MemberRank> promoteIfEligible(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable : " + memberId));

        if (member.rank() == MemberRank.GUILD_MASTER) {
            return Optional.empty();
        }

        MemberRank nextRank = MemberRank.values()[member.rank().ordinal() + 1];
        if (member.experiencePoints() >= EXPERIENCE_THRESHOLD.get(nextRank)) {
            member.promoteTo(nextRank);
            memberRepository.save(member);
            return Optional.of(nextRank);
        }
        return Optional.empty();
    }
}
