package com.scheduleengine.navigation;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Breadcrumb navigation component that displays the current navigation path
 */
public class BreadcrumbBar extends HBox {

    private final NavigationHandler navigationHandler;
    private NavigationContext currentContext;

    public BreadcrumbBar(NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
        this.currentContext = new NavigationContext();

        setSpacing(8);
        setPadding(new Insets(15, 20, 15, 20));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Add home button
        Button homeBtn = createHomeButton();
        getChildren().add(homeBtn);
    }

    /**
     * Update the breadcrumb with a new navigation context
     */
    public void updateContext(NavigationContext context) {
        this.currentContext = context;
        rebuild();
    }

    private void rebuild() {
        getChildren().clear();

        // Home button
        Button homeBtn = createHomeButton();
        getChildren().add(homeBtn);

        // Breadcrumb items
        List<NavigationNode> breadcrumb = currentContext.getBreadcrumb();
        for (int i = 0; i < breadcrumb.size(); i++) {
            final int level = i;
            NavigationNode node = breadcrumb.get(i);

            // Add separator
            Label separator = new Label(">");
            separator.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");
            getChildren().add(separator);

            // Add breadcrumb item
            if (i == breadcrumb.size() - 1) {
                // Current item - not clickable
                Label currentLabel = new Label(node.getDisplayName());
                currentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                getChildren().add(currentLabel);
            } else {
                // Previous items - clickable
                Button breadcrumbBtn = new Button(node.getDisplayName());
                breadcrumbBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #667eea; " +
                    "-fx-font-size: 14px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 2 8; " +
                    "-fx-underline: false;"
                );

                breadcrumbBtn.setOnMouseEntered(e ->
                    breadcrumbBtn.setStyle(
                        "-fx-background-color: #f0f0f0; " +
                        "-fx-text-fill: #667eea; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 2 8; " +
                        "-fx-underline: true;"
                    )
                );

                breadcrumbBtn.setOnMouseExited(e ->
                    breadcrumbBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: #667eea; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 2 8; " +
                        "-fx-underline: false;"
                    )
                );

                breadcrumbBtn.setOnAction(e -> {
                    NavigationContext newContext = currentContext.navigateToLevel(level);
                    navigationHandler.navigate(newContext);
                });

                getChildren().add(breadcrumbBtn);
            }
        }

        // Add spacer to push everything to the left
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
    }

    private Button createHomeButton() {
        Button homeBtn = new Button();

        FontAwesomeIconView homeIcon = new FontAwesomeIconView(FontAwesomeIcon.HOME);
        homeIcon.setSize("14");
        homeIcon.setFill(javafx.scene.paint.Color.web("#667eea"));
        homeBtn.setGraphic(homeIcon);

        homeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 4 8; " +
            "-fx-background-radius: 4;"
        );

        homeBtn.setOnMouseEntered(e ->
            homeBtn.setStyle(
                "-fx-background-color: #f0f0f0; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 4 8; " +
                "-fx-background-radius: 4;"
            )
        );

        homeBtn.setOnMouseExited(e ->
            homeBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 4 8; " +
                "-fx-background-radius: 4;"
            )
        );

        homeBtn.setOnAction(e -> {
            // Navigate to the root of the current section (first breadcrumb item)
            if (currentContext != null && !currentContext.getBreadcrumb().isEmpty()) {
                NavigationContext newContext = currentContext.navigateToLevel(0);
                navigationHandler.navigate(newContext);
            } else {
                // Fallback: navigate to leagues view
                NavigationContext newContext = new NavigationContext().navigateTo("leagues", "Leagues");
                navigationHandler.navigate(newContext);
            }
        });

        return homeBtn;
    }
}

