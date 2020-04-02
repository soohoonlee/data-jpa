package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  TeamRepository teamRepository;

  @PersistenceContext
  EntityManager em;

  @Autowired
  MemberQueryRepository memberQueryRepository;

  @Test
  void testMember() {
    System.out.println("memberRepository = " + memberRepository.getClass());
    Member member = new Member("memberA");
    Member savedMember = memberRepository.save(member);

    Member findMember = memberRepository.findById(savedMember.getId()).get();

    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    assertThat(findMember).isEqualTo(member);
  }

  @Test
  void basicCrud() {
    Member member1 = new Member("member1");
    Member member2 = new Member("member2");
    memberRepository.save(member1);
    memberRepository.save(member2);

    // 단건 조회 검증
    Member findMember1 = memberRepository.findById(member1.getId()).get();
    Member findMember2 = memberRepository.findById(member2.getId()).get();
    assertThat(findMember1).isEqualTo(member1);
    assertThat(findMember2).isEqualTo(member2);

    // 리스트 조회 검증
    List<Member> all = memberRepository.findAll();
    assertThat(all).hasSize(2);

    // 카운트 검증
    long count = memberRepository.count();
    assertThat(count).isEqualTo(2);

    // 삭제 검증
    memberRepository.delete(member1);
    memberRepository.delete(member2);

    long deleteCount = memberRepository.count();
    assertThat(deleteCount).isEqualTo(0);
  }

  @Test
  void findByUsernameAndAgeGreaterThan() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

    assertThat(result.get(0).getUsername()).isEqualTo("AAA");
    assertThat(result.get(0).getAge()).isEqualTo(20);
    assertThat(result).hasSize(1);
  }

  @Test
  void findHelloBy() {
    List<Member> helloBy = memberRepository.findTop3HelloBy();
  }

  @Test
  void testNamedQuery() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByUsername("AAA");
    Member findMember = result.get(0);
    assertThat(findMember).isEqualTo(m1);
  }

  @Test
  void testQuery() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findUser("AAA", 10);
    assertThat(result.get(0)).isEqualTo(m1);
  }

  @Test
  void testUsernameList() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<String> usernameList = memberRepository.findUsernameList();
    for (String s : usernameList) {
      System.out.println("s = " + s);
    }
    assertThat(usernameList).isNotEmpty();
    assertThat(usernameList).hasSize(2);
  }

  @Test
  void testMemberDto() {
    Team team = new Team("teamA");
    teamRepository.save(team);

    Member m1 = new Member("AAA", 10);
    m1.setTeam(team);
    memberRepository.save(m1);

    List<MemberDto> memberDto = memberRepository.findMemberDto();
    for (MemberDto dto : memberDto) {
      System.out.println("dto = " + dto);
    }

    assertThat(memberDto).isNotEmpty();
    assertThat(memberDto).hasSize(1);
    assertThat(memberDto.get(0).getTeamName()).isEqualTo("teamA");
  }

  @Test
  void findByNames() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
    for (Member member : result) {
      System.out.println("member = " + member);
    }

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getUsername()).isEqualTo("AAA");
  }

  @Test
  void returnType() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    // List<Member> aaa = memberRepository.findListByUsername("AAA");
    // Member findMember = memberRepository.findMemberByUsername("AAA");
    Optional<Member> aaa = memberRepository.findOptionalByUsername("AAA");
    System.out.println("aaa = " + aaa);
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

    Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

    // then
    List<Member> content = page.getContent();
    long totalElements = page.getTotalElements();

    assertThat(content.size()).isEqualTo(3);
    assertThat(totalElements).isEqualTo(5);
    assertThat(page.getNumber()).isEqualTo(0);
    assertThat(page.getTotalPages()).isEqualTo(2);
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
    // em.clear();

    List<Member> result = memberRepository.findByUsername("member5");
    Member member5 = result.get(0);
    System.out.println("member5 = " + member5);

    // then
    assertThat(resultCount).isEqualTo(3);
  }

  @Test
  void findMemberLazy() {
    // given
    // member1 -> teamA
    // member2 -> teamB
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    teamRepository.save(teamA);
    teamRepository.save(teamB);
    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 10, teamB);
    memberRepository.save(member1);
    memberRepository.save(member2);

    em.flush();
    em.clear();

    // when N + 1
    // select Member 1
    // List<Member> members = memberRepository.findAll();
    // List<Member> members = memberRepository.findMemberFetchJoin();
    List<Member> members = memberRepository.findEntityGraphByUsername("member1");
    for (Member member : members) {
      System.out.println("member = " + member.getUsername());
      System.out.println("member.teamClass = " + member.getTeam().getClass());
      System.out.println("member.team = " + member.getTeam().getName());
    }
  }

  @Test
  void queryHint() {
    // given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    // when
    // Member findMember = memberRepository.findById(member1.getId()).get();
    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");
    // findMember.setUsername("member2");

    em.flush();
  }

  @Test
  void lock() {
    // given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    // when
    List<Member> findMember = memberRepository.findLockByUsername("member1");
  }

  @Test
  void callCustom() {
    List<Member> result = memberRepository.findMemberCustom();
  }

  @Test
  void specBasic() {
    // given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 0, teamA);
    Member m2 = new Member("m2", 0, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    // when
    Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
    List<Member> result = memberRepository.findAll(spec);

    assertThat(result).hasSize(1);
  }

  @Test
  void queryByExample() {
    // given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 0, teamA);
    Member m2 = new Member("m2", 0, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    // when
    // probe
    Member member = new Member("m1");
    Team team = new Team("teamA");
    member.setTeam(team);

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");
    Example<Member> example = Example.of(member, matcher);

    List<Member> result = memberRepository.findAll(example);

    assertThat(result.get(0).getUsername()).isEqualTo("m1");
  }

  @Test
  void projections() {
    // given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 0, teamA);
    Member m2 = new Member("m2", 0, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    // when
    // List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);
    List<NestedClosedProjections> result = memberRepository.findProjectionsByUsername("m1", NestedClosedProjections.class);

    for (NestedClosedProjections usernameOnly : result) {
      System.out.println("UserName = " + usernameOnly.getUsername());
      System.out.println("TeamName = " + usernameOnly.getTeam().getName());
    }
  }
}