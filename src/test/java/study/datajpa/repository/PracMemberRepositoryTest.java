package study.datajpa.repository;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
public class PracMemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  TeamRepository teamRepository;

  @Test
  void findByUsernameAndAgeGreaterThan() {
    //given
    Member member1 = new Member("memberA", 20);
    Member member2 = new Member("memberA", 20);
    Member member3 = new Member("memberA", 20);
    Member member4 = new Member("memberD", 20);
    Member member5 = new Member("memberE", 20);
    memberRepository.save(member1);
    memberRepository.save(member2);
    memberRepository.save(member3);
    memberRepository.save(member4);
    memberRepository.save(member5);

    //when
    List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("memberA", 10);

    //then
    assertThat(members.size()).isEqualTo(3);
  }

  @Test
  void findByUsername() {
    //given
    Member member1 = new Member("memberA", 20);
    Member member2 = new Member("memberA", 20);
    Member member3 = new Member("memberA", 20);
    Member member4 = new Member("memberD", 20);
    Member member5 = new Member("memberE", 20);
    memberRepository.save(member1);
    memberRepository.save(member2);
    memberRepository.save(member3);
    memberRepository.save(member4);
    memberRepository.save(member5);

    //when
    List<Member> findMembers = memberRepository.findByUsername("memberA");

    //then
    assertThat(findMembers.size()).isEqualTo(3);
  }

  @Test
  void findMemberDto() {
    //given
    Team team = new Team("teamA");
    teamRepository.save(team);

    Member member1 = new Member("memberA", 10, team);
    Member member2 = new Member("memberB", 20, team);
    memberRepository.save(member1);
    memberRepository.save(member2);


    //when
    List<MemberDto> memberDto = memberRepository.findMemberDto();

    //then
    assertThat(memberDto.size()).isEqualTo(2);

    for (MemberDto dto : memberDto) {
      System.out.println(dto.getUsername());
    }
  }

  @Test
  void findSliceByAge() {
    //given
    Member member1 = new Member("memberA", 20);
    Member member2 = new Member("memberB", 20);
    Member member3 = new Member("memberC", 20);
    Member member4 = new Member("memberD", 30);
    Member member5 = new Member("memberE", 30);
    memberRepository.save(member1);
    memberRepository.save(member2);
    memberRepository.save(member3);
    memberRepository.save(member4);
    memberRepository.save(member5);

    PageRequest pageRequest = PageRequest.of(0, 3);

    //when
    Slice<Member> sliceByAge = memberRepository.findSliceByAge(20, pageRequest);

    //then
    for (Member member : sliceByAge) {
      System.out.println(member.getUsername());
    }
  }
  
  @Test
  void findByAge() {
    //given
    Member member1 = new Member("memberA", 20);
    Member member2 = new Member("memberB", 20);
    Member member3 = new Member("memberC", 20);
    Member member4 = new Member("memberD", 30);
    Member member5 = new Member("memberE", 30);
    memberRepository.save(member1);
    memberRepository.save(member2);
    memberRepository.save(member3);
    memberRepository.save(member4);
    memberRepository.save(member5);

    PageRequest pageRequest = PageRequest.of(0, 3);
    
    //when
    Page<Member> page = memberRepository.findByAge(20, pageRequest);

    Page<MemberDto> toMap = page.map(
        member -> new MemberDto(member.getId(), member.getUsername(), null)
    );

    //then

    List<Member> content = page.getContent();

    assertThat(content.size()).isEqualTo(3L);
    assertThat(page.getTotalElements()).isEqualTo(3L);

  }

  @Test
  void bulkUpdate() {
    //given
    Member member1 = new Member("memberA", 1);
    Member member2 = new Member("memberB", 20);
    Member member3 = new Member("memberC", 20);
    Member member4 = new Member("memberD", 30);
    Member member5 = new Member("memberE", 30);
    memberRepository.save(member1);
    memberRepository.save(member2);
    memberRepository.save(member3);
    memberRepository.save(member4);
    memberRepository.save(member5);

    //when
    int resultCount = memberRepository.bulkAgePlus(10);

    List<Member> result = memberRepository.findAll();

    for (Member member : result) {
      System.out.println(member.getAge());
    }

    //then
    assertThat(resultCount).isEqualTo(4);
  }

  @Test
  void fetchJoin() {
    //given
    Team team1 = new Team("teamA");
    teamRepository.save(team1);

    Team team2 = new Team("teamB");
    teamRepository.save(team2);

    Member member1 = new Member("memberA", 10, team1);
    Member member2 = new Member("memberB", 20, team2);
    memberRepository.save(member1);
    memberRepository.save(member2);


    //when
    List<Member> result = memberRepository.findMemberFetchJoin();

    //then
    for (Member member : result) {
      System.out.println(member.getClass());
      System.out.println(member.getTeam().getClass());
    }
  }

  @Test
  void findMemberLazy() {
    //given
    Team team1 = new Team("teamA");
    teamRepository.save(team1);

    Team team2 = new Team("teamB");
    teamRepository.save(team2);

    Member member1 = new Member("memberA", 10, team1);
    Member member2 = new Member("memberB", 20, team2);
    memberRepository.save(member1);
    memberRepository.save(member2);

    //when
    List<Member> result = memberRepository.findAll();

    //then
    for (Member member : result) {
      System.out.println("member.getClass() = " + member.getClass());
      System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
    }
  }




}
