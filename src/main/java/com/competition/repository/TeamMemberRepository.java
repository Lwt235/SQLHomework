package com.competition.repository;

import com.competition.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMember.TeamMemberId> {
    List<TeamMember> findByTeamId(Integer teamId);
    List<TeamMember> findByUserId(Integer userId);
}
