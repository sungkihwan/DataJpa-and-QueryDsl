package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired MemberJpaRepository memberJpaRepository;
    @Autowired
    EntityManager em;

    @Test
    public void testMember() throws Exception {
        //given
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        Member member1 = memberRepository.findById(saveMember.getId()).get();
        //when

        //then
        assertThat(member1.getId()).isEqualTo(member.getId());
        assertThat(member1.getUsername()).isEqualTo(member.getUsername());
        assertThat(member1).isEqualTo(member);
    }

    @Test
    public void testJpaMember() throws Exception {
        //given
        Member member = new Member("memember");
        Member saveMember = memberRepository.save(member);
        //when
        Member member1 = memberRepository.findById(saveMember.getId()).get();
        //then

        assertThat(member1).isEqualTo(member);
        assertThat(member1.getUsername()).isEqualTo(member.getUsername());
        assertThat(member1.getId()).isEqualTo(member.getId());
    }

    @Test
    public void testEntity() throws Exception {
        //given
        Team team1 = new Team("memeee");
        Team team2 = new Team("bbbbteam");
        em.persist(team1);
        em.persist(team2);
        //when

        Member member1 = new Member("kimchi", 10, team1);
        Member member2 = new Member("kimchi2", 15, team1);
        Member member3 = new Member("kimchi5", 17, team2);
        Member member4 = new Member("kimchi8", 18, team2);
        //then

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() throws Exception {
        //given
        Member member1 = new Member("member1",10);
        Member member2 = new Member("member1",20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
        //then

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member1",15);

        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(all.size());

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findUser() throws Exception {
        //given
        Member member1 = new Member("member1",10);
        Member member2 = new Member("member1",20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findUser(member1.getUsername(),member1.getAge());
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo(member1.getUsername());
        assertThat(result.get(0)).isEqualTo(member1);
        //then
    }

    @Test
    public void findMemberDto() throws Exception {
        //given
        Team team = new Team("TEAM1");
        teamRepository.save(team);

        Member member1 = new Member("member1",10);
        Member member2 = new Member("member2",20);
        member1.setTeam(team);
        member2.setTeam(team);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<MemberDto> result = memberRepository.findMemberDto();

        for (MemberDto dto : result) {
            System.out.println(dto);
        }
        //then
    }

    @Test
    public void findByNames() throws Exception {
        Member member1 = new Member("member1",10);
        Member member2 = new Member("member2",20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> result = memberRepository.findByNames(Arrays.asList("member1","member2"));

        for (Member dto : result) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void paging() throws Exception {
        //given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 10));
        memberJpaRepository.save(new Member("member3", 10));
        memberJpaRepository.save(new Member("member4", 10));
        memberJpaRepository.save(new Member("member5", 10));
        int age = 10;
        int offset = 0;
        int limit = 3;
        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);
        //페이지 계산 공식 적용...
        // totalPage = totalCount / size ...
        // 마지막 페이지 ...
        // 최초 페이지 ..
        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }
}