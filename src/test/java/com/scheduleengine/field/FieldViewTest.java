package com.scheduleengine.field;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.service.FieldService;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class FieldViewTest {

    @Mock
    private FieldService fieldService;

    private FieldView fieldView;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        when(fieldService.findAll()).thenReturn(Collections.emptyList());

        fieldView = new FieldView(fieldService);

        VBox view = fieldView.getView();
        Scene scene = new Scene(view, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void shouldDisplayTitle() {
        verifyThat("Fields", hasText("Fields"));
    }

    @Test
    void shouldDisplayAddFieldButton() {
        verifyThat("Add Field", hasText("Add Field"));
    }

    @Test
    void shouldDisplayRefreshButton() {
        verifyThat("Refresh", hasText("Refresh"));
    }

    @Test
    void shouldDisplayDeleteSelectedButton() {
        verifyThat("Delete Selected", hasText("Delete Selected"));
    }

    @Test
    void shouldLoadDataOnInitialization() {
        verify(fieldService, atLeastOnce()).findAll();
    }
}

