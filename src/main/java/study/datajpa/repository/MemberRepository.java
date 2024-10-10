package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{

  List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//  List<Member> findTop3By();

  @Query(name = "Member.findByUsername") // @Query 없어도 동작함
  List<Member> findByUsername(@Param("username") String username);

  // @Param 으로 parameter 세팅
  @Query("select m from Member m where m.username = :username and m.age = :age")
  List<Member> findUser(@Param("username") String username, @Param("age") int age);

  @Query("select m.username from Member m" )
  List<String> findUsernameList();

  // new operation!!
  // DTO 로 바로 조회, 생성자 필요
  @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
  List<MemberDto> findMemberDto();

  // in 절 사용
  @Query("select m from Member m where m.username in :names")
  List<Member> findByNames(@Param("names") Collection<String> names);

  // 페이징 위해 Pageable 인터페이스 사용
  // 반환 객체 Page 는 count 쿼리 함께 나감 -> Slice 와 차이
  Page<Member> findByAge(int age, Pageable pageable);

  // Slice는 내부적으로 limit + 1 개 더 가져옴, count 쿼리가 없음
  Slice<Member> findSliceByAge(int age, Pageable pageable);

  // count 쿼리 분리 -> 이럴 경우에 where 조건도 없고, left join 해도 count 결과는 같기 때문에 count 할 떄 불필요한 join 을 줄이기 위해 쿼리 분리 가능
  @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
  Page<Member> findCountByAge(int age, Pageable pageable);

  // 벌크 연산 하려면 @Modifying 붙여야함!
  @Modifying(clearAutomatically = true) // bulk 연산 후 영속성 컨텍스트 초기화
  @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
  int bulkAgePlus(@Param("age") int age);

  // fetch join
  @Query("select m from Member m left join fetch m.team")
  List<Member> findMemberFetchJoin();

  // @EntityGraph == fetch join
  @Override
  @EntityGraph(attributePaths = {"team"})
  List<Member> findAll();

  @EntityGraph(attributePaths = {"team"})
  @Query("select m from Member m")
  List<Member> findMemberEntityGraph();

  @EntityGraph(attributePaths = {"team"})
  List<Member> findEntityGraphByUsername(@Param("username") String username);

  // readOnly 하면 스냅샷을 추가로 만들지 않음 -> 변경감지 작동 x
  @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
  Member findReadOnlyByUsername(String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<Member> findLockByUsername(String username);

  // Projection 사용 엔티티 -> 바로 DTO
  List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);

  // Native 쿼리 사용
  @Query(value = "select * from member where username = ?", nativeQuery = true)
  Member findByNativeQuery(String username);

  @Query(value = "select m.member_id as id, m.username, t.name as teamName "
      + "from Member m left join team t",
      countQuery = "select count(*) from member",
      nativeQuery = true)
  Page<MemberProjection> findByNativeProjection(Pageable pageable);
}

