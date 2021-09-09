package study.datajpa.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.MemberSearchCondition;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QueryDslwithJpaRepository {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Autowired
    MemberJpaRepositorywithQueryDsl memberJpaRepository;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void test() throws Exception {
        //given
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        Member member = memberJpaRepository.findById(member1.getId()).get();
        assertThat(member).isEqualTo(member1);

        List<Member> all = memberJpaRepository.findAll();
        for (Member member2 : all) {
            System.out.println("member2 = " + member2);
        }
        assertThat(all).containsExactly(member1);

        List<Member> member12 = memberJpaRepository.findByUsername_QueryDsl("member1");
        assertThat(member12).containsExactly(member1);
        for (Member member2 : member12) {
            System.out.println("member2 = " + member2);
        }
        //when

        //then
    }

    @Test
    public void search() throws Exception {
        MemberSearchCondition cond = new MemberSearchCondition();
        cond.setAgeGoe(35);
        cond.setAgeLoe(45);
        cond.setUsername("");
        cond.setTeamname("");

        List<MemberDto> memberDtos = memberJpaRepository.search(cond);

        for (MemberDto memberDto : memberDtos) {
            System.out.println("memberDto = " + memberDto);
        }

        assertThat(memberDtos).extracting("teamname").containsExactly("teamB");
    }

    @Test
    public void searchPageSimple() throws Exception {
        MemberSearchCondition cond = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberDto> results = memberRepository.searchPageSimple(cond, pageRequest);

        for (MemberDto memberDto : results) {
            System.out.println("memberDto = " + memberDto);
        }

        assertThat(results.getSize()).isEqualTo(3);
        assertThat(results.getContent()).extracting("teamname").contains("teamB");
    }
}