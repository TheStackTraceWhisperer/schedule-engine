package com.scheduleengine.season;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.season.service.SeasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that duplicate season names are prevented
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SeasonDuplicateNameTest {

  @Autowired
  private SeasonService seasonService;

  @Autowired
  private LeagueService leagueService;

  private League testLeague;

  @BeforeEach
  void setUp() {
    testLeague = new League();
    testLeague.setName("Test League");
    testLeague.setDescription("Test league for season duplicate name testing");
    testLeague = leagueService.save(testLeague);
  }

  @Test
  void testCannotCreateTwoSeasonsWithSameName() {
    // Create first season with unique test name
    Season season1 = new Season();
    season1.setName("Test Season Alpha");
    season1.setStartDate(LocalDate.of(2026, 3, 1));
    season1.setEndDate(LocalDate.of(2026, 5, 31));
    season1.setLeague(testLeague);

    Season saved = seasonService.save(season1);
    assertNotNull(saved.getId());
    assertEquals("Test Season Alpha", saved.getName());

    // Try to create second season with same name
    Season season2 = new Season();
    season2.setName("Test Season Alpha");  // Same name!
    season2.setStartDate(LocalDate.of(2026, 6, 1));
    season2.setEndDate(LocalDate.of(2026, 8, 31));
    season2.setLeague(testLeague);

    // Should throw IllegalArgumentException
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> seasonService.save(season2)
    );

    assertTrue(exception.getMessage().contains("Test Season Alpha"));
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void testCanCreateSeasonsWithDifferentNames() {
    // Create first season with unique test name
    Season season1 = new Season();
    season1.setName("Test Season Beta");
    season1.setStartDate(LocalDate.of(2026, 3, 1));
    season1.setEndDate(LocalDate.of(2026, 5, 31));
    season1.setLeague(testLeague);
    seasonService.save(season1);

    // Create second season with different name - should succeed
    Season season2 = new Season();
    season2.setName("Test Season Gamma");  // Different name
    season2.setStartDate(LocalDate.of(2026, 6, 1));
    season2.setEndDate(LocalDate.of(2026, 8, 31));
    season2.setLeague(testLeague);

    assertDoesNotThrow(() -> {
      Season saved = seasonService.save(season2);
      assertNotNull(saved.getId());
      assertEquals("Test Season Gamma", saved.getName());
    });
  }

  @Test
  void testCanUpdateSeasonKeepingSameName() {
    // Create season with unique test name
    Season season = new Season();
    season.setName("Test Season Delta");
    season.setStartDate(LocalDate.of(2026, 3, 1));
    season.setEndDate(LocalDate.of(2026, 5, 31));
    season.setLeague(testLeague);
    Season saved = seasonService.save(season);

    // Update the same season keeping the same name - should succeed
    saved.setStartDate(LocalDate.of(2026, 3, 15));  // Change date
    assertDoesNotThrow(() -> {
      Season updated = seasonService.update(saved.getId(), saved);
      assertEquals("Test Season Delta", updated.getName());
      assertEquals(LocalDate.of(2026, 3, 15), updated.getStartDate());
    });
  }

  @Test
  void testCannotUpdateSeasonToExistingName() {
    // Create first season with unique test name
    Season season1 = new Season();
    season1.setName("Test Season Epsilon");
    season1.setStartDate(LocalDate.of(2026, 3, 1));
    season1.setEndDate(LocalDate.of(2026, 5, 31));
    season1.setLeague(testLeague);
    seasonService.save(season1);

    // Create second season with unique test name
    Season season2 = new Season();
    season2.setName("Test Season Zeta");
    season2.setStartDate(LocalDate.of(2026, 6, 1));
    season2.setEndDate(LocalDate.of(2026, 8, 31));
    season2.setLeague(testLeague);
    Season saved2 = seasonService.save(season2);

    // Try to update season2 to have the same name as season1
    saved2.setName("Test Season Epsilon");  // Try to use existing name

    // Should throw either IllegalArgumentException (service check) or
    // DataIntegrityViolationException (database constraint)
    assertThrows(
      Exception.class,
      () -> seasonService.update(saved2.getId(), saved2),
      "Should throw an exception when trying to use duplicate name"
    );
  }

  @Test
  void testExistsByName() {
    // Initially should not exist
    assertFalse(seasonService.existsByName("Test Season Omega"));

    // Create season with unique test name
    Season season = new Season();
    season.setName("Test Season Omega");
    season.setStartDate(LocalDate.of(2026, 1, 1));
    season.setEndDate(LocalDate.of(2026, 12, 31));
    season.setLeague(testLeague);
    seasonService.save(season);

    // Now should exist
    assertTrue(seasonService.existsByName("Test Season Omega"));

    // Different name should not exist
    assertFalse(seasonService.existsByName("Test Season Nonexistent"));
  }
}

