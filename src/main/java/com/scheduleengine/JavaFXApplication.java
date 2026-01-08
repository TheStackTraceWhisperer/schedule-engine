package com.scheduleengine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private MainView mainView;

    @Override
    public void init() {
        // Initialize Spring Boot context
        context = SpringApplication.run(ScheduleEngineApplication.class);
        mainView = context.getBean(MainView.class);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            mainView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

