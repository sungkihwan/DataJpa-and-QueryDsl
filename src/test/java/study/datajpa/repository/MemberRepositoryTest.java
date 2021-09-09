package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @PersistenceContext
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
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        for (MemberDto dto : map) {
            System.out.println("dto = " + dto);
        }

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();
        System.out.println("totalElements = " + totalElements);
        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void pagingSlice() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        List<Member> content = slice.getContent();
        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(slice.getSize()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(0);
        assertThat(slice.isFirst()).isTrue();
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 25));
        memberRepository.save(new Member("member4", 30));
        memberRepository.save(new Member("member5", 50));

        int resultCount = memberRepository.bulkAgePlus(20);

        List<Member> member5 = memberRepository.findByUsername("member5");
        System.out.println("member5 = " + member5.get(0));

//        em.clear(); // 영속성 컨텍스트 초기화

        List<Member> member6 = memberRepository.findByUsername("member5");
        System.out.println("member5 = " + member6.get(0));

        System.out.println("resultCount = " + resultCount);

        assertThat(resultCount).isEqualTo(4);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member);
            // findAll을 사용후에 클래스를 찍어보면 가짜 프록시 객체를 갖고 있음
            // -> @EntityGraph 사용
            System.out.println("member = " + member.getTeam().getClass());
            // N + 1문제가 발생
            System.out.println("member = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        Member member1 = new Member("memgggg", 50);
        memberRepository.save(member1);

        em.flush();
        em.clear();

        // 쿼리힌트 리드온리로 변경감지 체크를 안해서 업데이트를 안함.
        List<Member> memgggg = memberRepository.findReadOnlyByUsername("memgggg");
        memgggg.get(0).setUsername("membobo");
        em.flush();
    }

    @Test
    public void lock() {
        Member member1 = new Member("memgggg", 50);
        memberRepository.save(member1);

        em.flush();
        em.clear();

        // 쿼리힌트 리드온리로 변경감지 체크를 안해서 업데이트를 안함.
        List<Member> memgggg = memberRepository.findLockByUsername("memgggg");
    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("밍밍밍", 20);
        memberRepository.save(member); // @PrePersist

        Thread.sleep(100);
        member.setUsername("몽몽이");

        em.flush(); //@PreUpdate
        em.clear();
        //when
        List<Member> findMembers = memberRepository.findByUsername(member.getUsername());

        for (Member findMember : findMembers) {
            System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
            System.out.println("findMember.getUpdatedDate() = " + findMember.getLastModifiedDate());
            System.out.println("findMember.getCreatedBy() = " + findMember.getCreatedBy());
            System.out.println("findMember.getModifiedBy() = " + findMember.getLastModifiedBy());
        }
    }

    @Test
    public void projection() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        //when
        //Probe 생성

        List<UsernameOnlyDto> m1 = memberRepository.findProjectionsByUsername("m1");

        for (UsernameOnlyDto usernameOnly : m1) {
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }
    }
}