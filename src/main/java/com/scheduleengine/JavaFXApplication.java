package com.scheduleengine;

import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {
    
    private ApplicationContext context;
    private MainView mainView;
    
    @Override
    public void init() {
        // Initialize Micronaut context
        context = ApplicationContext.run();
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
