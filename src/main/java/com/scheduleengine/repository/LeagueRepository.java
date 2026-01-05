package com.scheduleengine.repository;

import com.scheduleengine.domain.League;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
}
