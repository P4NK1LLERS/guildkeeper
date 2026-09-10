package fr.dev.sensei.guild.keeper.recruitment;

import java.util.UUID;

/**
 * Recrutement de nouveaux membres.
 *
 * <p>Regles :
 * <ul>
 *   <li>le nom du candidat doit etre renseigne (non vide) ;</li>
 *   <li>aucun membre du meme nom ne doit deja exister dans le repository ;</li>
 *   <li>un membre recrute demarre au rang NOVICE avec 0 point d'experience.</li>
 * </ul>
 */
public class RecruitmentService {

    /** Chance attribuee par defaut a un nouveau membre (valeur deterministe, dans la plage 1..10). */
    static final int DEFAULT_RECRUIT_LUCK = 1;

    private final MemberRepository memberRepository;

    public RecruitmentService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Recrute un candidat et le persiste.
     *
     * @throws IllegalArgumentException si le nom est vide
     * @throws DuplicateMemberException si un membre du meme nom existe deja
     */
    public Member recruit(String candidateName) {
        if (candidateName == null || candidateName.isBlank()) {
            throw new IllegalArgumentException("Le nom du candidat est obligatoire.");
        }
        String name = candidateName.strip();
        if (memberRepository.findByName(name).isPresent()) {
            throw new DuplicateMemberException(name);
        }
        Member recruit = Member.novice(UUID.randomUUID().toString(), name, DEFAULT_RECRUIT_LUCK);
        memberRepository.save(recruit);
        return recruit;
    }
}
