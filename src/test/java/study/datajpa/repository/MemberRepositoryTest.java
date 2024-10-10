package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import org.hibernate.boot.jaxb.SourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  TeamRepository teamRepository;

  @PersistenceContext
  EntityManager em;

  @Test
  void testMember() {
    Member member = new Member("memberA");
    Member savedMember = memberRepository.save(member);

    Member findMember = memberRepository.findById(savedMember.getId()).get();

    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
  }

  @Test
  void basicCRUD() {
    Member member1 = new Member("member1");
    Member member2 = new Member("member2");
    memberRepository.save(member1);
    memberRepository.save(member2);

    Member findMember1 = memberRepository.findById(member1.getId()).get();
    Member findMember2 = memberRepository.findById(member2.getId()).get();
    assertThat(findMember1).isEqualTo(member1);
    assertThat(findMember2).isEqualTo(member2);

    List<Member> all = memberRepository.findAll();
    assertThat(all.size()).isEqualTo(2);

    Long count = memberRepository.count();
    assertThat(count).isEqualTo(2);

    memberRepository.delete(member1);
    memberRepository.delete(member2);

    Long deletedCount = memberRepository.count();
    assertThat(deletedCount).isEqualTo(0);
  }

  @Test
  void findByUsernameAndAgeGreaterThan() {

    Member member1 = new Member("memberA", 10);
    Member member2 = new Member("memberB", 20);
    memberRepository.save(member1);
    memberRepository.save(member2);

    List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("memberB", 15);

    assertThat(result.get(0).getUsername()).isEqualTo("memberB");
    assertThat(result.get(0).getAge()).isEqualTo(20);
    assertThat(result.size()).isEqualTo(1);
  }

  @Test
  void findMemberDto() {

    Team team = new Team("teamA");
    teamRepository.save(team);

    Member member1 = new Member("memberA", 10);
    member1.setTeam(team);
    memberRepository.save(member1);

    List<MemberDto> memberDto = memberRepository.findMemberDto();
    for (MemberDto dto : memberDto) {
      System.out.println("dto = " + dto);
    }
  }

  @Test
  void findByNames() {

    Member member1 = new Member("memberA", 10);
    Member member2 = new Member("memberB", 20);
    memberRepository.save(member1);
    memberRepository.save(member2);

    List<Member> result = memberRepository.findByNames(Arrays.asList("memberA", "memberB"));

    for (Member member : result) {
      System.out.println("member = " + member);
    }
  }

  @Test
  void paging() {

    // given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));

    // when
    Page<Member> page = memberRepository.findByAge(age, pageRequest);
//    Slice<Member> page = memberRepository.findByAge(age, pageRequest);

    // Controller에서 entity 넘기면 안되기 때문에 dto로 변경하는 방법(엔티티 외부 노출하면 X, 엔티티 변경시 API 스펙 바뀜!)
    Page<MemberDto> toMap = page.map(
        member -> new MemberDto(member.getId(), member.getUsername(), null));

    // then
    List<Member> content = page.getContent();
    long totalElements = page.getTotalElements();

    assertThat(content.size()).isEqualTo(3);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getNumber()).isEqualTo(0);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.isFirst()).isTrue();
    assertThat(page.hasNext()).isTrue();
  }

  @Test
  void slicing() {

    // given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));

    // when
    Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

    // then
    List<Member> content = page.getContent();

    assertThat(content.size()).isEqualTo(3);
//    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getNumber()).isEqualTo(0);
//    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.isFirst()).isTrue();
    assertThat(page.hasNext()).isTrue();
  }

  @Test
  void bulkUpdate() {

    // given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 19));
    memberRepository.save(new Member("member3", 20));
    memberRepository.save(new Member("member4", 21));
    memberRepository.save(new Member("member5", 40));

    // when
    int resultCount = memberRepository.bulkAgePlus(20);
//    em.clear();

    List<Member> result = memberRepository.findByUsername("member5");
    Member member5 = result.get(0);
    System.out.println("member5 = " + member5);

    // then
    assertThat(resultCount).isEqualTo(3);
  }

  @Test
  void fetchJoin() {

    Team teamA = teamRepository.save(new Team("teamA"));
    Team teamB = teamRepository.save(new Team("teamB"));
    // given
    memberRepository.save(new Member("member1", 10, teamA));
    memberRepository.save(new Member("member2", 19, teamB));

//    em.flush();
//    em.clear();

    // when
    List<Member> members = memberRepository.findMemberFetchJoin();

    for (Member member : members) {
      System.out.println("member = " + member);
      System.out.println("member.teamClass = " + member.getTeam().getClass());
      System.out.println("member.team = " + member.getTeam().getMembers());
    }
  }

  @Test
  void findMemberLazy() {

    Team teamA = teamRepository.save(new Team("teamA"));
    Team teamB = teamRepository.save(new Team("teamB"));
    // given
    memberRepository.save(new Member("member1", 10, teamA));
    memberRepository.save(new Member("member2", 19, teamB));

//    em.flush();
//    em.clear();

    // when
    List<Member> members = memberRepository.findAll();

    for (Member member : members) {
      System.out.println("member = " + member);
      System.out.println("member.teamClass = " + member.getTeam().getClass());
      System.out.println("member.team = " + member.getTeam().getMembers());
    }
  }

  @Test
  void queryHint() {

    // given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    // when
    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");

    em.flush();
  }

  @Test
  void lock() {

    // given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    // when
    List<Member> result = memberRepository.findLockByUsername("member1");

  }

  @Test
  void callCustom() {
    List<Member> result = memberRepository.findMemberCustom();
  }

  @Test
  void JpaEventBaseEntity() throws Exception {
    //given
    Member member = new Member("member1");
    memberRepository.save(member); // @PrePersist

    Thread.sleep(100);
    member.setUsername("member2");

    em.flush(); // @PreUpdate
    em.clear();

    //when
    Member findMember = memberRepository.findById(member.getId()).get();

    //then
    System.out.println("findMember = " + findMember.getCreatedDate());
    System.out.println("findMember = " + findMember.getLastModifiedDate());
    System.out.println("findMember = " + findMember.getCreatedBy());
    System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
  }

  @Test
  void projections() {
    //given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 10, teamA);
    Member m2 = new Member("m2", 10, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    //when
    List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");

    for (UsernameOnly usernameOnly : result) {
      System.out.println("usernameOnly = " + usernameOnly);
    }

    //then

  }

  @Test
  void nativeQuery(){

    //given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 10, teamA);
    Member m2 = new Member("m2", 10, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    //when
    Member result = memberRepository.findByNativeQuery("m1");
    System.out.println("result = " + result);

    //then
  }

  @Test
  void nativeProQuery(){

    //given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 10, teamA);
    Member m2 = new Member("m2", 10, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    //when
    Page<MemberProjection> result = memberRepository.findByNativeProjection(
        PageRequest.of(0, 10));
    System.out.println("result = " + result);

    //then
  }


}