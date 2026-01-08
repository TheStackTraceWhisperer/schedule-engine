package com.scheduleengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataSourceSmokeTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void hasDefaultDataSource() {
        assertNotNull(dataSource, "DataSource should be injected");
        assertTrue(dataSource.getClass().getName().contains("HikariDataSource") ||
                   dataSource.getClass().getName().contains("DataSource"),
                   "Should have a DataSource bean");
    }
}

