package org.yu.number_game;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.yu.number_game.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,String> {

    Member findByUsername(String username);

    List<Member> findAllByOrderByHighScoreDesc(Pageable pageable);

}
