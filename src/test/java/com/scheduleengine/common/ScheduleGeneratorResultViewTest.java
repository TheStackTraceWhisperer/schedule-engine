package com.scheduleengine.common;

import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.season.domain.Season;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class ScheduleGeneratorResultViewTest {

    @Mock
    private ScheduleGeneratorService scheduleGeneratorService;

    @Mock
    private GameService gameService;

    private ScheduleGeneratorResultView scheduleGeneratorResultView;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        scheduleGeneratorResultView = new ScheduleGeneratorResultView(scheduleGeneratorService, gameService);

        Season testSeason = new Season();
        testSeason.setId(1L);
        testSeason.setName("Test Season 2024");

        VBox view = scheduleGeneratorResultView.getView(testSeason);
        Scene scene = new Scene(view, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void shouldDisplayTitleWithSeasonName() {
        verifyThat("Schedule Generator - Test Season 2024", hasText("Schedule Generator - Test Season 2024"));
    }

    @Test
    void shouldDisplayGenerateScheduleButton() {
        verifyThat("Generate Schedule", hasText("Generate Schedule"));
    }

    @Test
    void shouldDisplaySaveToDatabaseButton() {
        verifyThat("Save to Database", hasText("Save to Database"));
    }

    @Test
    void shouldDisplayCancelButton() {
        verifyThat("Cancel", hasText("Cancel"));
    }

    @Test
    void shouldDisplayInfoLabel() {
        verifyThat("Click 'Generate Schedule' to create a round-robin schedule for this season.",
                   hasText("Click 'Generate Schedule' to create a round-robin schedule for this season."));
    }
}

