package com.scheduleengine.repository;

import com.scheduleengine.domain.Team;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAll();
    List<Team> findByLeagueId(Long leagueId);
}
