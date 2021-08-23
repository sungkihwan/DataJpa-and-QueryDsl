package study.datajpa.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.datajpa.entity.QMember.member;
import static study.datajpa.entity.QTeam.team;

@SpringBootTest
@Transactional
public class MemberQueryDslRepositoryTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Test
    public void testEntity() {
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
        //초기화
        em.flush();
        em.clear();
        //확인
        List<Member> members = em.createQuery("select m from Member m",
                        Member.class)
                .getResultList();
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }
    }

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
    public void startJPQL() {
        String qlString =
                "select m from Member m " +
                "where m.username = :username";

        Member findMember = em.createQuery(qlString,Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() {
//        QMember m = new QMember("m");
//        QMember m = QMember.member;

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchQueryDsl() {
        List<Member> findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))
                        .or(member.username.eq("member2")))
                        .fetch();

        assertThat(findMember.size()).isEqualTo(2);

//        member.username.eq("member1") // username = 'member1'
//        member.username.ne("member1") //username != 'member1'
//        member.username.eq("member1").not() // username != 'member1'
//        member.username.isNotNull() //이름이 is not null
//        member.age.in(10, 20) // age in (10,20)
//        member.age.notIn(10, 20) // age not in (10, 20)
//        member.age.between(10,30) //between 10, 30
//        member.age.goe(30) // age >= 30
//        member.age.gt(30) // age > 30
//        member.age.loe(30) // age <= 30
//        member.age.lt(30) // age < 30
//        member.username.like("member%") //like 검색
//        member.username.contains("member") // like ‘%member%’ 검색
//        member.username.startsWith("member") //like ‘member%’ 검색
    }

    @Test
    public void FetchQueryDsl() {
        long findMembers = queryFactory
                .selectFrom(member)
                .fetchCount();

        System.out.println("findMembers = " + findMembers);

        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        long total = memberQueryResults.getTotal();

        System.out.println("total = " + total);

        List<Member> result = memberQueryResults.getResults();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void OrderByQueryDsl() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc().nullsLast(), member.age.desc())
                .fetch();

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void pagingQueryDsl() {
        QueryResults<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc().nullsLast(), member.age.desc())
                .offset(2)
                .limit(2)
                .fetchResults();

        System.out.println("members.getTotal() = " + members.getTotal());

        for (Member member: members.getResults()) {
            System.out.println("members = " + member);
        }
    }

    @Test
    public void aggregation() {
        List<Tuple> fetch = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min(),
                        member.age.max()
                )
                .from(member)
                .fetch();


        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

        List<Tuple> fetch1 = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : fetch1) {
            System.out.println("tuple = " + tuple);
        }
//    .groupBy(item.price)
//    .having(item.price.gt(1000))
    }

    @Test
    public void join() {
        List<Member> teamA = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> fetch = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }

        assertThat(fetch)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }
}
