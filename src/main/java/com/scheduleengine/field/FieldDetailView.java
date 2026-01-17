package com.scheduleengine.field;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.game.domain.Game;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.common.TablePreferencesUtil;
import com.scheduleengine.navigation.NavigationContext;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Comprehensive field detail view for managing a single field.
 * Includes hours of operation, usage blocks, and field schedule visualization.
 */
public class FieldDetailView {

    private final FieldService fieldService;
    private final FieldAvailabilityService availabilityService;
    private final FieldUsageBlockService usageBlockService;
    private final GameService gameService;

    private Consumer<NavigationContext> navigationHandler;

    // Make Sunday the first day of the week for headers and day iteration
    private static final DayOfWeek[] DAYS = new DayOfWeek[] {
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    };

    public FieldDetailView(FieldService fieldService, FieldAvailabilityService availabilityService,
                          FieldUsageBlockService usageBlockService, GameService gameService) {
        this.fieldService = fieldService;
        this.availabilityService = availabilityService;
        this.usageBlockService = usageBlockService;
        this.gameService = gameService;
    }

    public void setNavigationHandler(Consumer<NavigationContext> handler) {
        this.navigationHandler = handler;
    }

    /**
     * Get the comprehensive field detail view
     */
    public VBox getView(Field field, NavigationContext context) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        // Allow the scroll content to grow vertically
        scrollPane.setFitToHeight(true);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setStyle("-fx-background-color: #f5f6fa;");
        // Ensure the main container grows with the viewport
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        // Header with field name, basic info, and delete button
        VBox header = buildHeader(field);
        mainContainer.getChildren().add(header);

        // Build utilization section first so we can reference it
        VBox utilizationSection = buildUtilizationSection(field);
        Object utilizationRefresh = utilizationSection.getUserData();

        // Create left container with Hours and Blocks stacked vertically
        VBox leftContainer = new VBox(15);
        leftContainer.setFillWidth(true);
        // Further increase width to accommodate wider content and persistent column sizes
        leftContainer.setPrefWidth(680);
        leftContainer.setMaxWidth(720);

        // Hours of Operation Section with refresh callback
        VBox hoursSection = buildHoursOfOperationSection(field, utilizationRefresh);
        leftContainer.getChildren().add(hoursSection);
        VBox.setVgrow(hoursSection, Priority.ALWAYS);

        // Reserved Time Section with refresh callback
        VBox blocksSection = buildUsageBlocksSection(field, utilizationRefresh);
        leftContainer.getChildren().add(blocksSection);
        VBox.setVgrow(blocksSection, Priority.ALWAYS);

        // Create right container with utilization (should fill available height)
        VBox rightContainer = new VBox(15);
        rightContainer.getChildren().add(utilizationSection);
        VBox.setVgrow(utilizationSection, Priority.ALWAYS);

        // Create horizontal layout: left side (hours + blocks) and right side (utilization)
        HBox contentArea = new HBox(20);
        contentArea.setPrefHeight(Region.USE_COMPUTED_SIZE);

        contentArea.getChildren().add(leftContainer);
        contentArea.getChildren().add(rightContainer);
        // Right side should expand to fill the remaining space
        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        mainContainer.getChildren().add(contentArea);
        // Let the main content area grow so right pane can fill viewport height
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        scrollPane.setContent(mainContainer);
        VBox root = new VBox(scrollPane);
        // Ensure the root VBox grows to fill the window height
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private VBox buildHeader(Field field) {
        VBox header = new VBox(8);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(field.getName());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete Field");
        deleteBtn.setStyle("-fx-padding: 8 16; -fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> confirmDeleteField(field));

        titleBar.getChildren().addAll(title, spacer, deleteBtn);

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getChildren().addAll(
            createInfoItem("Location:", field.getLocation()),
            createInfoItem("Address:", field.getAddress())
        );

        header.getChildren().addAll(titleBar, infoBox);
        return header;
    }

    private void confirmDeleteField(Field field) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Field");
        confirmation.setHeaderText("Confirm Deletion");
        confirmation.setContentText("Are you sure you want to delete the field '" + field.getName() + "'? This action cannot be undone.");

        var result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fieldService.deleteById(field.getId());
            // Navigate back to fields list
            if (navigationHandler != null) {
                NavigationContext ctx = new NavigationContext().navigateTo("fields", "Fields");
                navigationHandler.accept(ctx);
            }
        }
    }

    private HBox createInfoItem(String label, String value) {
        HBox box = new HBox(8);
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label valueLbl = new Label(value != null ? value : "Not specified");
        valueLbl.setStyle("-fx-text-fill: #2c3e50;");
        box.getChildren().addAll(labelLbl, valueLbl);
        return box;
    }

    private VBox buildHoursOfOperationSection(Field field, Object utilizationRefresh) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        Label title = new Label("Hours of Operation");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Table for hours of operation
        TableView<FieldAvailability> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hours configured"));
        table.setEditable(true);

        TableColumn<FieldAvailability, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDayOfWeek().toString()));
        dayCol.setCellFactory(ComboBoxTableCell.forTableColumn(
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
        ));
        dayCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setDayOfWeek(DayOfWeek.valueOf(e.getNewValue()));
                availabilityService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Failed to update: " + ex.getMessage());
            }
        });
        dayCol.setMinWidth(120);

        TableColumn<FieldAvailability, String> openCol = new TableColumn<>("Opens");
        openCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getOpenTime().toString()));
        openCol.setCellFactory(TextFieldTableCell.forTableColumn());
        openCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setOpenTime(LocalTime.parse(e.getNewValue()));
                availabilityService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Invalid time format");
            }
        });
        openCol.setMinWidth(120);

        TableColumn<FieldAvailability, String> closeCol = new TableColumn<>("Closes");
        closeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCloseTime().toString()));
        closeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        closeCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setCloseTime(LocalTime.parse(e.getNewValue()));
                availabilityService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Invalid time format");
            }
        });
        closeCol.setMinWidth(120);

        TableColumn<FieldAvailability, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(80);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            {
                delBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: #dc3545;");
                delBtn.setOnAction(e -> deleteAvailability(getTableView().getItems().get(getIndex()), field, table, utilizationRefresh));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delBtn);
            }
        });

        table.getColumns().setAll(dayCol, openCol, closeCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        TablePreferencesUtil.setupTableColumnPersistence(table, "field.detail.availability");

        List<FieldAvailability> availabilities = availabilityService.findByField(field);
        table.setItems(FXCollections.observableArrayList(availabilities));

        Button addBtn = new Button("+ Add Hours");
        addBtn.setWrapText(true);
        addBtn.setStyle("-fx-padding: 8 16; -fx-background-color: #667eea; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            FieldAvailability newAvailability = new FieldAvailability();
            newAvailability.setField(field);
            newAvailability.setDayOfWeek(DayOfWeek.MONDAY);
            newAvailability.setOpenTime(LocalTime.of(9, 0));
            newAvailability.setCloseTime(LocalTime.of(17, 0));
            FieldAvailability saved = availabilityService.save(newAvailability);
            table.getItems().add(saved);
            FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
        });

        container.getChildren().setAll(title, table, addBtn);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    private VBox buildUsageBlocksSection(Field field, Object utilizationRefresh) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        Label title = new Label("Reserved Time");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Table for usage blocks
        TableView<FieldUsageBlock> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No blocks configured"));
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<FieldUsageBlock, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDayOfWeek().toString()));
        dayCol.setCellFactory(ComboBoxTableCell.forTableColumn(
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
        ));
        dayCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setDayOfWeek(DayOfWeek.valueOf(e.getNewValue()));
                usageBlockService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Failed to update: " + ex.getMessage());
            }
        });
        dayCol.setMinWidth(110);

        TableColumn<FieldUsageBlock, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsageType().toString()));
        typeCol.setCellFactory(ComboBoxTableCell.forTableColumn(
            "LEAGUE", "TOURNAMENT", "PRACTICE", "CLOSED"
        ));
        typeCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setUsageType(FieldUsageBlock.UsageType.valueOf(e.getNewValue()));
                usageBlockService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Failed to update: " + ex.getMessage());
            }
        });
        typeCol.setMinWidth(110);

        TableColumn<FieldUsageBlock, String> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStartTime().toString()));
        startCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setStartTime(LocalTime.parse(e.getNewValue()));
                usageBlockService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Invalid time format");
            }
        });
        startCol.setMinWidth(110);

        TableColumn<FieldUsageBlock, String> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEndTime().toString()));
        endCol.setCellFactory(TextFieldTableCell.forTableColumn());
        endCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setEndTime(LocalTime.parse(e.getNewValue()));
                usageBlockService.save(e.getRowValue());
                FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
            } catch (Exception ex) {
                showError("Error", "Invalid time format");
            }
        });
        endCol.setMinWidth(110);

        TableColumn<FieldUsageBlock, Void> actionCol = new TableColumn<>("Edit");
        actionCol.setMinWidth(60);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            {
                editBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: #667eea;");
                editBtn.setOnAction(e -> editUsageBlock(getTableView().getItems().get(getIndex()), field, table, utilizationRefresh));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        TableColumn<FieldUsageBlock, Void> warningCol = new TableColumn<>("");
        warningCol.setMinWidth(30);
        warningCol.setMaxWidth(30);
        warningCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    FieldUsageBlock block = getTableView().getItems().get(getIndex());
                    if (isBlockOutsideHours(field, block)) {
                        Label warningLabel = new Label("⚠");
                        warningLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800;");
                        warningLabel.setTooltip(new Tooltip("Reserved time exceeds field hours of operation"));
                        setGraphic(warningLabel);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().setAll(dayCol, typeCol, startCol, endCol, warningCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        TablePreferencesUtil.setupTableColumnPersistence(table, "field.detail.usage");

        List<FieldUsageBlock> blocks = usageBlockService.findByField(field);
        table.setItems(FXCollections.observableArrayList(blocks));

        Button addBtn = new Button("+ Add Block");
        addBtn.setWrapText(true);
        addBtn.setStyle("-fx-padding: 8 16; -fx-background-color: #667eea; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            FieldUsageBlock newBlock = new FieldUsageBlock();
            newBlock.setField(field);
            newBlock.setDayOfWeek(DayOfWeek.SATURDAY);
            newBlock.setUsageType(FieldUsageBlock.UsageType.LEAGUE);
            newBlock.setStartTime(LocalTime.of(9, 0));
            newBlock.setEndTime(LocalTime.of(12, 0));
            FieldUsageBlock saved = usageBlockService.save(newBlock);
            table.getItems().add(saved);
            FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setWrapText(true);
        deleteBtn.setStyle("-fx-padding: 8 16; -fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            ObservableList<FieldUsageBlock> selectedItems = table.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                showError("No Selection", "Please select at least one reserved time block to delete.");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Delete " + selectedItems.size() + " reserved time block(s)?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Create a copy of the list to avoid modification during iteration
                    List<FieldUsageBlock> blocksToDelete = new ArrayList<>(selectedItems);
                    for (FieldUsageBlock block : blocksToDelete) {
                        usageBlockService.delete(block.getId());
                        table.getItems().remove(block);
                    }
                    FieldDetailView.this.refreshUtilization(field, utilizationRefresh);
                }
            });
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.getChildren().addAll(addBtn, deleteBtn);

        container.getChildren().setAll(title, table, buttonBox);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    private VBox buildUtilizationSection(Field field) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        VBox.setVgrow(container, Priority.ALWAYS);

        Label title = new Label("Field Schedule");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Build timeline bars for each day
        VBox timelineContainer = buildTimelineVisualization(field);
        VBox.setVgrow(timelineContainer, Priority.ALWAYS);

        // Legend
        VBox legend = buildLegend();

        HBox contentArea = new HBox(20);
        contentArea.setAlignment(Pos.TOP_LEFT);
        contentArea.getChildren().addAll(timelineContainer, legend);
        HBox.setHgrow(timelineContainer, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(480);
        scrollPane.setMinHeight(400);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(title, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Store reference for refresh capability
        container.setUserData(new Object() {
            public void refresh() {
                contentArea.getChildren().remove(0); // Remove timeline container
                VBox newTimeline = buildTimelineVisualization(field);
                contentArea.getChildren().add(0, newTimeline);
                HBox.setHgrow(newTimeline, Priority.ALWAYS);
            }
        });

        return container;
    }

    /**
     * Build a timeline visualization showing hours of operation and reserved time for each day
     */
    private VBox buildTimelineVisualization(Field field) {
        VBox timelineBox = new VBox(12);
        timelineBox.setPadding(new Insets(10));
        timelineBox.setStyle("-fx-background-color: #f9f9f9;");

        // Calculate hour range
        int[] hourRange = calculateHourRange(field);
        int startHour = hourRange[0];
        int endHour = hourRange[1];
        int totalHours = endHour - startHour;

        // Time axis label
        HBox timeAxisBox = createTimeAxis(startHour, endHour);
        timelineBox.getChildren().add(timeAxisBox);

        // Timeline bar for each day
        for (DayOfWeek day : DAYS) {
            HBox dayTimeline = createDayTimeline(field, day, startHour, endHour, totalHours);
            timelineBox.getChildren().add(dayTimeline);
        }

        return timelineBox;
    }

    /**
     * Create the time axis showing hours
     */
    private HBox createTimeAxis(int startHour, int endHour) {
        HBox timeAxis = new HBox(0);
        timeAxis.setPrefHeight(25);
        timeAxis.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");

        // Day label space
        Region dayLabelSpace = new Region();
        dayLabelSpace.setPrefWidth(100);
        timeAxis.getChildren().add(dayLabelSpace);

        // Hour markers
        int totalHours = endHour - startHour;
        for (int h = startHour; h < endHour; h++) {
            Label hourLabel = new Label(to12HourLabel(h));
            hourLabel.setStyle("-fx-font-size: 10px; -fx-text-alignment: center;");
            hourLabel.setPrefWidth(40);
            hourLabel.setAlignment(Pos.CENTER);
            timeAxis.getChildren().add(hourLabel);
        }

        return timeAxis;
    }

    /**
     * Create a timeline bar for a specific day
     */
    private HBox createDayTimeline(Field field, DayOfWeek day, int startHour, int endHour, int totalHours) {
        HBox dayTimeline = new HBox(0);
        dayTimeline.setPrefHeight(45);
        dayTimeline.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Day label
        Label dayLabel = new Label(abbrev(day));
        dayLabel.setStyle("-fx-font-weight: bold; -fx-text-alignment: center; -fx-padding: 5;");
        dayLabel.setPrefWidth(100);
        dayLabel.setAlignment(Pos.CENTER);
        dayTimeline.getChildren().add(dayLabel);

        // Get availability and blocks for this day
        List<FieldAvailability> availability = availabilityService.findByFieldAndDayOfWeek(field, day);
        List<FieldUsageBlock> blocks = usageBlockService.findByFieldAndDayOfWeek(field, day);

        // Create hour slots
        for (int h = startHour; h < endHour; h++) {
            Region hourBlock = createHourBlock(h, availability, blocks);
            hourBlock.setPrefWidth(40);
            dayTimeline.getChildren().add(hourBlock);
        }

        return dayTimeline;
    }

    /**
     * Create an individual hour block with appropriate color based on availability and blocks
     */
    private Region createHourBlock(int hour, List<FieldAvailability> availability, List<FieldUsageBlock> blocks) {
        Region block = new Region();
        String color = "#dfe6e9"; // Closed by default
        String tooltip = String.format("%02d:00", hour);

        // Check availability
        boolean isAvailable = availability.stream().anyMatch(a -> {
            int openMin = a.getOpenTime().getHour() * 60 + a.getOpenTime().getMinute();
            int closeMin = a.getCloseTime().getHour() * 60 + a.getCloseTime().getMinute();
            int cellMin = hour * 60;
            return cellMin >= openMin && cellMin < closeMin;
        });

        if (isAvailable) {
            color = "#43e97b"; // Available
            tooltip += " - Available";
        }

        // Check blocks (override availability)
        for (FieldUsageBlock b : blocks) {
            int startMin = b.getStartTime().getHour() * 60 + b.getStartTime().getMinute();
            int endMin = b.getEndTime().getHour() * 60 + b.getEndTime().getMinute();
            int cellMin = hour * 60;
            if (cellMin >= startMin && cellMin < endMin) {
                switch (b.getUsageType()) {
                    case LEAGUE -> { color = "#667eea"; tooltip = String.format("%02d:00 - League", hour); }
                    case TOURNAMENT -> { color = "#fa709a"; tooltip = String.format("%02d:00 - Tournament", hour); }
                    case PRACTICE -> { color = "#feca57"; tooltip = String.format("%02d:00 - Practice", hour); }
                    case CLOSED -> { color = "#95a5a6"; tooltip = String.format("%02d:00 - Closed Block", hour); }
                }
                break;
            }
        }

        block.setStyle("-fx-background-color: " + color + "; -fx-border-color: #bdc3c7; -fx-border-width: 0 1 0 0;");
        Tooltip.install(block, new Tooltip(tooltip));
        return block;
    }


    private VBox buildLegend() {
        VBox legend = new VBox(8);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

        Label title = new Label("Legend");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        legend.getChildren().add(title);

        legend.getChildren().addAll(
            makeLegendSwatch("Hours of Operation", Color.web("#43e97b")),
            makeLegendSwatch("League Block", Color.web("#667eea")),
            makeLegendSwatch("Tournament Block", Color.web("#fa709a")),
            makeLegendSwatch("Practice Block", Color.web("#feca57")),
            makeLegendSwatch("Dedicated Closed Block", Color.web("#95a5a6")),
            makeLegendSwatch("Closed/Unavailable", Color.web("#dfe6e9"))
        );

        return legend;
    }

    private HBox makeLegendSwatch(String label, Color color) {
        Region swatch = new Region();
        swatch.setPrefSize(16, 16);
        swatch.setStyle("-fx-background-color: " + toCssColor(color) + "; -fx-border-color: #bdc3c7;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        return new HBox(6, swatch, l);
    }

    private String toCssColor(Color c) {
        int r = (int)(c.getRed() * 255);
        int g = (int)(c.getGreen() * 255);
        int b = (int)(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }


    private int[] calculateHourRange(Field field) {
        List<FieldAvailability> allAvailability = availabilityService.findByField(field);
        if (allAvailability.isEmpty()) {
            return new int[]{0, 23};
        }

        int earliestHour = 23;
        int latestHour = 0;

        for (FieldAvailability availability : allAvailability) {
            int openHour = availability.getOpenTime().getHour();
            int closeHour = availability.getCloseTime().getHour();
            if (availability.getCloseTime().getMinute() > 0) {
                closeHour++;
            }
            earliestHour = Math.min(earliestHour, openHour);
            latestHour = Math.max(latestHour, closeHour);
        }


        return new int[]{earliestHour, latestHour};
    }

    private void showAddAvailabilityDialog(Field field, TableView<FieldAvailability> table, Object utilizationRefresh) {
        Dialog<FieldAvailability> dialog = new Dialog<>();
        dialog.setTitle("Add Hours of Operation");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        Spinner<Integer> openHourSpinner = new Spinner<>(0, 23, 9);
        Spinner<Integer> openMinSpinner = new Spinner<>(0, 59, 0, 15);
        Spinner<Integer> closeHourSpinner = new Spinner<>(0, 23, 17);
        Spinner<Integer> closeMinSpinner = new Spinner<>(0, 59, 0, 15);

        grid.add(new Label("Day:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Opens:"), 0, 1);
        HBox openBox = new HBox(5, openHourSpinner, new Label(":"), openMinSpinner);
        grid.add(openBox, 1, 1);
        grid.add(new Label("Closes:"), 0, 2);
        HBox closeBox = new HBox(5, closeHourSpinner, new Label(":"), closeMinSpinner);
        grid.add(closeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.add.availability", 500, 300));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (dayCombo.getValue() == null) {
                    showError("Validation", "Please select a day");
                    return null;
                }

                FieldAvailability availability = new FieldAvailability();
                availability.setField(field);
                availability.setDayOfWeek(dayCombo.getValue());
                availability.setOpenTime(LocalTime.of(openHourSpinner.getValue(), openMinSpinner.getValue()));
                availability.setCloseTime(LocalTime.of(closeHourSpinner.getValue(), closeMinSpinner.getValue()));

                try {
                    return availabilityService.save(availability);
                } catch (Exception ex) {
                    showError("Error", "Failed to save hours: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            // Reload all availabilities from database to ensure consistency
            List<FieldAvailability> items = availabilityService.findByField(field);
            table.setItems(FXCollections.observableArrayList(items));
            // Refresh utilization grid
            if (utilizationRefresh != null) {
                try {
                    java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                    refresh.invoke(utilizationRefresh);
                } catch (Exception ex) {
                    // Silently ignore refresh errors
                }
            }
        });
    }

    private void showAddUsageBlockDialog(Field field, TableView<FieldUsageBlock> table, Object utilizationRefresh) {
        Dialog<FieldUsageBlock> dialog = new Dialog<>();
        dialog.setTitle("Add Reserved Time");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("LEAGUE", "TOURNAMENT", "PRACTICE", "CLOSED"));
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMinSpinner = new Spinner<>(0, 59, 0, 15);
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 17);
        Spinner<Integer> endMinSpinner = new Spinner<>(0, 59, 0, 15);

        grid.add(new Label("Day:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Start:"), 0, 2);
        HBox startBox = new HBox(5, startHourSpinner, new Label(":"), startMinSpinner);
        grid.add(startBox, 1, 2);
        grid.add(new Label("End:"), 0, 3);
        HBox endBox = new HBox(5, endHourSpinner, new Label(":"), endMinSpinner);
        grid.add(endBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.add.block", 500, 350));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (dayCombo.getValue() == null || typeCombo.getValue() == null) {
                    showError("Validation", "Please fill all fields");
                    return null;
                }

                FieldUsageBlock block = new FieldUsageBlock();
                block.setField(field);
                block.setDayOfWeek(dayCombo.getValue());
                block.setUsageType(FieldUsageBlock.UsageType.valueOf(typeCombo.getValue()));
                block.setStartTime(LocalTime.of(startHourSpinner.getValue(), startMinSpinner.getValue()));
                block.setEndTime(LocalTime.of(endHourSpinner.getValue(), endMinSpinner.getValue()));

                try {
                    return usageBlockService.save(block);
                } catch (Exception ex) {
                    showError("Error", "Failed to save block: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(b -> {
            // Reload all usage blocks from database to ensure consistency
            List<FieldUsageBlock> items = usageBlockService.findByField(field);
            table.setItems(FXCollections.observableArrayList(items));
            // Refresh utilization grid
            if (utilizationRefresh != null) {
                try {
                    java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                    refresh.invoke(utilizationRefresh);
                } catch (Exception ex) {
                    // Silently ignore refresh errors
                }
            }
        });
    }

    private void editAvailability(FieldAvailability availability, Field field, TableView<FieldAvailability> table, Object utilizationRefresh) {
        Dialog<FieldAvailability> dialog = new Dialog<>();
        dialog.setTitle("Edit Hours of Operation");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        dayCombo.setValue(availability.getDayOfWeek());
        Spinner<Integer> openHourSpinner = new Spinner<>(0, 23, availability.getOpenTime().getHour());
        Spinner<Integer> openMinSpinner = new Spinner<>(0, 59, availability.getOpenTime().getMinute(), 15);
        Spinner<Integer> closeHourSpinner = new Spinner<>(0, 23, availability.getCloseTime().getHour());
        Spinner<Integer> closeMinSpinner = new Spinner<>(0, 59, availability.getCloseTime().getMinute(), 15);

        grid.add(new Label("Day:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Opens:"), 0, 1);
        HBox openBox = new HBox(5, openHourSpinner, new Label(":"), openMinSpinner);
        grid.add(openBox, 1, 1);
        grid.add(new Label("Closes:"), 0, 2);
        HBox closeBox = new HBox(5, closeHourSpinner, new Label(":"), closeMinSpinner);
        grid.add(closeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.edit.availability", 500, 300));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (dayCombo.getValue() == null) {
                    showError("Validation", "Please select a day");
                    return null;
                }

                availability.setDayOfWeek(dayCombo.getValue());
                availability.setOpenTime(LocalTime.of(openHourSpinner.getValue(), openMinSpinner.getValue()));
                availability.setCloseTime(LocalTime.of(closeHourSpinner.getValue(), closeMinSpinner.getValue()));

                try {
                    return availabilityService.save(availability);
                } catch (Exception ex) {
                    showError("Error", "Failed to save hours: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(saved -> {
            // Reload table from database and refresh utilization grid
            List<FieldAvailability> items = availabilityService.findByField(field);
            table.setItems(FXCollections.observableArrayList(items));
            if (utilizationRefresh != null) {
                try {
                    java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                    refresh.invoke(utilizationRefresh);
                } catch (Exception ex) {
                    // Silently ignore refresh errors
                }
            }
        });
    }

    private void deleteAvailability(FieldAvailability availability, Field field, TableView<FieldAvailability> table, Object utilizationRefresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this hours entry?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    availabilityService.delete(availability.getId());
                    var items = new java.util.ArrayList<>(table.getItems());
                    items.remove(availability);
                    table.setItems(FXCollections.observableArrayList(items));
                    // Refresh utilization grid
                    if (utilizationRefresh != null) {
                        try {
                            java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                            refresh.invoke(utilizationRefresh);
                        } catch (Exception ex) {
                            // Silently ignore refresh errors
                        }
                    }
                } catch (Exception ex) {
                    showError("Error", "Failed to delete: " + ex.getMessage());
                }
            }
        });
    }

    private void editUsageBlock(FieldUsageBlock block, Field field, TableView<FieldUsageBlock> table, Object utilizationRefresh) {
        Dialog<FieldUsageBlock> dialog = new Dialog<>();
        dialog.setTitle("Edit Reserved Time");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        dayCombo.setValue(block.getDayOfWeek());
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("LEAGUE", "TOURNAMENT", "PRACTICE", "CLOSED"));
        typeCombo.setValue(block.getUsageType().toString());
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, block.getStartTime().getHour());
        Spinner<Integer> startMinSpinner = new Spinner<>(0, 59, block.getStartTime().getMinute(), 15);
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, block.getEndTime().getHour());
        Spinner<Integer> endMinSpinner = new Spinner<>(0, 59, block.getEndTime().getMinute(), 15);

        grid.add(new Label("Day:"), 0, 0);
        grid.add(dayCombo, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Start:"), 0, 2);
        HBox startBox = new HBox(5, startHourSpinner, new Label(":"), startMinSpinner);
        grid.add(startBox, 1, 2);
        grid.add(new Label("End:"), 0, 3);
        HBox endBox = new HBox(5, endHourSpinner, new Label(":"), endMinSpinner);
        grid.add(endBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.edit.block", 500, 350));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (dayCombo.getValue() == null || typeCombo.getValue() == null) {
                    showError("Validation", "Please fill all fields");
                    return null;
                }

                block.setDayOfWeek(dayCombo.getValue());
                block.setUsageType(FieldUsageBlock.UsageType.valueOf(typeCombo.getValue()));
                block.setStartTime(LocalTime.of(startHourSpinner.getValue(), startMinSpinner.getValue()));
                block.setEndTime(LocalTime.of(endHourSpinner.getValue(), endMinSpinner.getValue()));

                try {
                    return usageBlockService.save(block);
                } catch (Exception ex) {
                    showError("Error", "Failed to save block: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(saved -> {
            // Reload table from database and refresh utilization grid
            List<FieldUsageBlock> items = usageBlockService.findByField(field);
            table.setItems(FXCollections.observableArrayList(items));
            if (utilizationRefresh != null) {
                try {
                    java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                    refresh.invoke(utilizationRefresh);
                } catch (Exception ex) {
                    // Silently ignore refresh errors
                }
            }
        });
    }

    private void deleteUsageBlock(FieldUsageBlock block, Field field, TableView<FieldUsageBlock> table, Object utilizationRefresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this block?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    usageBlockService.delete(block.getId());
                    var items = new java.util.ArrayList<>(table.getItems());
                    items.remove(block);
                    table.setItems(FXCollections.observableArrayList(items));
                    // Refresh utilization grid
                    if (utilizationRefresh != null) {
                        try {
                            java.lang.reflect.Method refresh = utilizationRefresh.getClass().getDeclaredMethod("refresh");
                            refresh.invoke(utilizationRefresh);
                        } catch (Exception ex) {
                            // Silently ignore refresh errors
                        }
                    }
                } catch (Exception ex) {
                    showError("Error", "Failed to delete: " + ex.getMessage());
                }
            }
        });
    }

    private String abbrev(DayOfWeek day) {
        return day.toString().substring(0, 3);
    }

    private String to12HourLabel(int hour) {
        if (hour == 0) return "12 AM";
        if (hour < 12) return hour + " AM";
        if (hour == 12) return "12 PM";
        return (hour - 12) + " PM";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    // Replace non-generic cell factory helpers with type-safe generic versions
    private <T> javafx.util.Callback<TableColumn<T, String>, TableCell<T, String>> dayOfWeekCellFactory() {
        return col -> new TableCell<T, String>() {
            private ComboBox<String> comboBox;
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createComboBox();
                    setText(null);
                    setGraphic(comboBox);
                }
            }
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (comboBox != null) comboBox.setValue(item);
                    setText(null);
                    setGraphic(comboBox);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
            private void createComboBox() {
                comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
                ));
                comboBox.setValue(getItem());
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setOnAction(event -> commitEdit(comboBox.getValue()));
            }
        };
    }

    private <T> javafx.util.Callback<TableColumn<T, String>, TableCell<T, String>> timeCellFactory() {
        return col -> new TableCell<T, String>() {
            private TextField textField;
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            }
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) textField.setText(item);
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
            private void createTextField() {
                textField = new TextField(getItem());
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) commitEdit(textField.getText());
                });
            }
        };
    }

    private <T> javafx.util.Callback<TableColumn<T, String>, TableCell<T, String>> usageTypeCellFactory() {
        return col -> new TableCell<T, String>() {
            private ComboBox<String> comboBox;
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createComboBox();
                    setText(null);
                    setGraphic(comboBox);
                }
            }
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (comboBox != null) comboBox.setValue(item);
                    setText(null);
                    setGraphic(comboBox);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
            private void createComboBox() {
                comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "LEAGUE", "TOURNAMENT", "PRACTICE", "CLOSED"
                ));
                comboBox.setValue(getItem());
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setOnAction(event -> commitEdit(comboBox.getValue()));
            }
        };
    }

    // Update table column cellFactory bindings to use the type-safe callbacks
    // Hours of Operation table columns setup
    // ...existing code...
    // dayCol.setCellFactory(col -> createDayOfWeekEditingCell());
    // openCol.setCellFactory(col -> createTimeEditingCell());
    // closeCol.setCellFactory(col -> createTimeEditingCell());
    // ...existing code...
    // Reserved Time table columns setup
    // ...existing code...
    // typeCol.setCellFactory(col -> createUsageTypeEditingCell());
    // startCol.setCellFactory(col -> createTimeEditingCell());
    // endCol.setCellFactory(col -> createTimeEditingCell());
    // ...existing code...

    private void refreshUtilization(Field field, Object utilizationRefresh) {
        if (utilizationRefresh instanceof Object obj) {
            try {
                var m = obj.getClass().getDeclaredMethod("refresh");
                m.setAccessible(true);
                m.invoke(obj);
            } catch (Exception ex) {
                // ignore; the visualization will be rebuilt on next navigation
            }
        }
    }

    /**
     * Check if a usage block exceeds the field's hours of operation for that day
     */
    private boolean isBlockOutsideHours(Field field, FieldUsageBlock block) {
        List<FieldAvailability> availabilities = availabilityService.findByFieldAndDayOfWeek(field, block.getDayOfWeek());

        if (availabilities.isEmpty()) {
            // No hours configured for this day, so any block is outside hours
            return true;
        }

        for (FieldAvailability availability : availabilities) {
            // Check if block falls within operating hours
            if (block.getStartTime().isBefore(availability.getOpenTime()) ||
                block.getEndTime().isAfter(availability.getCloseTime())) {
                return true;
            }
        }

        return false;
    }
}
