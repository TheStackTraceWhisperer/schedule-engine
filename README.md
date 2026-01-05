# schedule-engine
A basic scheduling system capable of managing leagues, teams, fields, game scores, seasons, etc.

## Technology Stack
- **Java 21** (NOT 17)
- **Maven** build system
- **Micronaut** for dependency injection
- **JPA/Hibernate** for persistence
- **H2 Database** for storage
- **JavaFX** for the desktop UI

## Features
- **JavaFX Desktop Application** - Better user experience than Excel spreadsheets
- Manage Leagues
- Manage Teams (with coach and contact information)
- Manage Fields (with location and address)
- Manage Seasons
- Manage Games (with scores, dates, and status tracking)

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

## Database
The application uses an H2 database stored in `./data/scheduledb`. The database schema is automatically created on first run.
