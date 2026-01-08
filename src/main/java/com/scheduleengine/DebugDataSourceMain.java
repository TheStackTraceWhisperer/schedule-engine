package com.scheduleengine;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.sql.DataSource;

public class DebugDataSourceMain {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ScheduleEngineApplication.class, args)) {
            ConfigurableEnvironment env = ctx.getEnvironment();
            System.out.println("Active profiles: " + String.join(", ", env.getActiveProfiles()));
            System.out.println("Default profiles: " + String.join(", ", env.getDefaultProfiles()));

            try {
                DataSource ds = ctx.getBean(DataSource.class);
                System.out.println("Found DataSource: " + (ds != null));
                System.out.println("DataSource type: " + ds.getClass().getName());
            } catch (Exception e) {
                System.out.println("Could not get DataSource: " + e.getMessage());
            }

            String url = env.getProperty("spring.datasource.url");
            System.out.println("spring.datasource.url = " + (url != null ? url : "<missing>"));
        }
    }
}
