# schedule-engine
A basic scheduling system capable of managing leagues, teams, fields, game scores, seasons, etc.

## Technology Stack
- **Java 21** (NOT 17)
- **Maven** build system
- **Micronaut** for dependency injection
- **JPA/Hibernate** for persistence
- **H2 Database** for storage
- **JavaFX** for the desktop UI
- **TestFX** for UI automation testing

## Features
- **JavaFX Desktop Application** - Better user experience than Excel spreadsheets
- Manage Leagues
- Manage Teams (with coach and contact information)
- Manage Fields (with location and address)
- Manage Seasons
- Manage Games (with scores, dates, and status tracking)
- **Automated UI Testing** with screenshot capture for tutorial generation

## Requirements
- Java 21 or later
- Maven 3.6+

## Building the Application
```bash
mvn clean package
```

## Running the Application
```bash
mvn javafx:run
```

Or run the packaged JAR:
```bash
java -jar target/schedule-engine-0.1.jar
```

## Testing

### Run UI Automation Tests
The application includes comprehensive UI automation tests with screenshot capture:

```bash
mvn test
```

Screenshots are saved to `target/screenshots/` and can be used for creating tutorials and documentation.

For detailed testing documentation, see [UI Testing Guide](src/test/java/com/scheduleengine/README.md).

### Run Specific Test
```bash
mvn test -Dtest=ScheduleEngineUITest
```

### Disable Headless Mode (requires display)
```bash
mvn test -Dtestfx.headless=false
```

## Database
The application uses an H2 database stored in `./data/scheduledb`. The database schema is automatically created on first run.
