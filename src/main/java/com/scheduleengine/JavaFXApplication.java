package com.scheduleengine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private MainView mainView;
    private boolean shouldCloseContext = true;

    /**
     * For testing: inject an existing context instead of creating a new one
     */
    public void setContext(ConfigurableApplicationContext injectedContext) {
        this.context = injectedContext;
        this.shouldCloseContext = false; // Don't close a context we didn't create
        this.mainView = context.getBean(MainView.class);
    }

    @Override
    public void init() {
        // Only create context if one wasn't injected (normal app startup)
        if (context == null) {
            context = SpringApplication.run(ScheduleEngineApplication.class);
            mainView = context.getBean(MainView.class);
            shouldCloseContext = true;
        }
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
        // Only close context if we created it
        if (context != null && shouldCloseContext) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}



