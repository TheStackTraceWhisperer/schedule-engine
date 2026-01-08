package com.scheduleengine;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI Automation test for the Schedule Engine application.
 * This test demonstrates the complete workflow of the application
 * and captures screenshots at each step for tutorial documentation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScheduleEngineUITest extends BaseUITest {
    
    private ConfigurableApplicationContext context;
    private MainView mainView;
    
    @Override
    public void start(Stage stage) throws Exception {
        // Initialize Spring Boot context
        context = SpringApplication.run(ScheduleEngineApplication.class);
        mainView = context.getBean(MainView.class);
        
        // Start the application
        mainView.start(stage);
        currentTestName = "ScheduleEngineUITest";
    }
    
    @AfterEach
    @Override
    public void cleanup(TestInfo testInfo) throws Exception {
        if (context != null) {
            context.close();
        }
        super.cleanup(testInfo);
    }
    
    @Test
    @Order(1)
    @DisplayName("Step 1: Application launches successfully and shows home screen")
    public void test01_LaunchApplication() {
        // Verify the application window is visible
        assertNotNull(lookup("Welcome to Schedule Engine").query(), 
            "Home page should display welcome message");
        
        captureScreenshot("01_home_screen");
        
        // Verify all tabs are present
        clickOn("Home");
        captureScreenshot("02_home_tab_selected");
        
        clickOn("Leagues");
        captureScreenshot("03_leagues_tab");
        
        clickOn("Teams");
        captureScreenshot("04_teams_tab");
        
        clickOn("Fields");
        captureScreenshot("05_fields_tab");
        
        clickOn("Seasons");
        captureScreenshot("06_seasons_tab");
        
        clickOn("Games");
        captureScreenshot("07_games_tab");
    }
    
    @Test
    @Order(2)
    @DisplayName("Step 2: Create a new league")
    public void test02_CreateLeague() {
        // Navigate to Leagues tab
        clickOn("Leagues");
        waitForFxEvents();
        captureScreenshot("10_leagues_empty");
        
        // Click Add League button
        clickOn("Add League");
        waitForFxEvents();
        captureScreenshot("11_add_league_dialog");
        
        // Fill in league details
        clickOn((TextField) lookup(".text-field").nth(0).query());
        write("Premier Soccer League");
        captureScreenshot("12_league_name_entered");
        
        clickOn((TextField) lookup(".text-area").query());
        write("Professional soccer league with top teams");
        captureScreenshot("13_league_description_entered");
        
        // Save the league
        clickOn("Save");
        waitForFxEvents();
        captureScreenshot("14_league_saved");
        
        // Verify league appears in the table
        TableView<?> table = lookup(".table-view").query();
        assertNotNull(table, "League table should exist");
        assertEquals(1, table.getItems().size(), "Table should have 1 league");
    }
    
    @Test
    @Order(3)
    @DisplayName("Step 3: Create teams for the league")
    public void test03_CreateTeams() {
        // First create a league
        test02_CreateLeague();
        
        // Navigate to Teams tab
        clickOn("Teams");
        waitForFxEvents();
        captureScreenshot("20_teams_empty");
        
        // Create first team
        clickOn("Add Team");
        waitForFxEvents();
        captureScreenshot("21_add_team_dialog");
        
        clickOn((TextField) lookup(".text-field").nth(0).query());
        write("Thunder FC");
        
        clickOn((TextField) lookup(".text-field").nth(1).query());
        write("John Smith");
        
        clickOn((TextField) lookup(".text-field").nth(2).query());
        write("john.smith@example.com");
        
        captureScreenshot("22_first_team_details_entered");
        
        clickOn("Save");
        waitForFxEvents();
        captureScreenshot("23_first_team_saved");
        
        // Create second team
        clickOn("Add Team");
        waitForFxEvents();
        
        clickOn((TextField) lookup(".text-field").nth(0).query());
        write("Lightning United");
        
        clickOn((TextField) lookup(".text-field").nth(1).query());
        write("Jane Doe");
        
        clickOn((TextField) lookup(".text-field").nth(2).query());
        write("jane.doe@example.com");
        
        captureScreenshot("24_second_team_details_entered");
        
        clickOn("Save");
        waitForFxEvents();
        captureScreenshot("25_two_teams_saved");
        
        // Verify teams appear in the table
        TableView<?> table = lookup(".table-view").query();
        assertTrue(table.getItems().size() >= 2, "Table should have at least 2 teams");
    }
    
    @Test
    @Order(4)
    @DisplayName("Step 4: Create playing fields")
    public void test04_CreateFields() {
        // Navigate to Fields tab
        clickOn("Fields");
        waitForFxEvents();
        captureScreenshot("30_fields_empty");
        
        // Create first field
        clickOn("Add Field");
        waitForFxEvents();
        captureScreenshot("31_add_field_dialog");
        
        clickOn((TextField) lookup(".text-field").nth(0).query());
        write("Central Stadium");
        
        clickOn((TextField) lookup(".text-field").nth(1).query());
        write("Downtown");
        
        clickOn((TextField) lookup(".text-field").nth(2).query());
        write("123 Main Street");
        
        captureScreenshot("32_field_details_entered");
        
        clickOn("Save");
        waitForFxEvents();
        captureScreenshot("33_field_saved");
        
        // Verify field appears in the table
        TableView<?> table = lookup(".table-view").query();
        assertEquals(1, table.getItems().size(), "Table should have 1 field");
    }
    
    @Test
    @Order(5)
    @DisplayName("Step 5: Navigate through all tabs and verify data persistence")
    public void test05_VerifyDataPersistence() {
        // Create test data
        test02_CreateLeague();
        
        // Navigate through tabs and verify data is present
        clickOn("Home");
        waitForFxEvents();
        captureScreenshot("40_home_with_data");
        
        clickOn("Leagues");
        waitForFxEvents();
        captureScreenshot("41_leagues_with_data");
        
        TableView<?> leagueTable = lookup(".table-view").query();
        assertTrue(leagueTable.getItems().size() > 0, "Leagues should be present");
        
        clickOn("Teams");
        waitForFxEvents();
        captureScreenshot("42_teams_persistence_check");
        
        clickOn("Fields");
        waitForFxEvents();
        captureScreenshot("43_fields_persistence_check");
        
        clickOn("Home");
        waitForFxEvents();
        captureScreenshot("44_final_home_view");
    }
    
    @Test
    @Order(6)
    @DisplayName("Step 6: Test menu functionality")
    public void test06_MenuFunctionality() {
        clickOn("File");
        waitForFxEvents();
        captureScreenshot("50_file_menu_opened");
        
        // Close menu by pressing ESC
        press(KeyCode.ESCAPE);
        waitForFxEvents();
        
        clickOn("Help");
        waitForFxEvents();
        captureScreenshot("51_help_menu_opened");
        
        clickOn("About");
        waitForFxEvents();
        captureScreenshot("52_about_dialog");
        
        // Close dialog
        clickOn("OK");
        waitForFxEvents();
        captureScreenshot("53_dialog_closed");
    }
    
    @Test
    @Order(7)
    @DisplayName("Step 7: Test refresh functionality")
    public void test07_RefreshFunctionality() {
        // Navigate to Leagues
        clickOn("Leagues");
        waitForFxEvents();
        captureScreenshot("60_before_refresh");
        
        // Click refresh button
        Button refreshButton = lookup("Refresh").query();
        assertNotNull(refreshButton, "Refresh button should exist");
        
        clickOn("Refresh");
        waitForFxEvents();
        captureScreenshot("61_after_refresh");
    }
}
