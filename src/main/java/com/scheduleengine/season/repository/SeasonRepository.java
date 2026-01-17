package com.scheduleengine.season.repository;

import com.scheduleengine.season.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
  List<Season> findByLeagueId(Long leagueId);

  Optional<Season> findByName(String name);

  boolean existsByName(String name);
}
