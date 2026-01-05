package com.scheduleengine.repository;

import com.scheduleengine.domain.Season;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findByLeagueId(Long leagueId);
}
