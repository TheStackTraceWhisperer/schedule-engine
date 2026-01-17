package com.scheduleengine.player.repository;

import com.scheduleengine.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    Optional<Player> findByFirstNameAndLastName(String firstName, String lastName);
    void deleteByTeamId(Long teamId);
}

