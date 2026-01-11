# Drill-Down Navigation System

## Overview

The Schedule Engine application has been refactored to support a hierarchical drill-down navigation pattern with breadcrumb support. This allows users to navigate from high-level views (like Leagues) down into specific details (like a specific league's teams or seasons).

## Architecture

### Core Components

#### 1. NavigationContext
**Location**: `com.scheduleengine.navigation.NavigationContext`

Represents the current navigation state with:
- **Breadcrumb trail**: A list of navigation nodes showing the path taken
- **Context data**: Associated objects (e.g., League, Team, Season) at each level
- **Immutable navigation**: Each navigation action returns a new context

**Key Methods**:
```java
// Navigate to a new view
NavigationContext navigateTo(String viewId, String displayName)
NavigationContext navigateTo(String viewId, String displayName, Object contextObject)

// Navigate back
NavigationContext navigateBack()
NavigationContext navigateToLevel(int level)
NavigationContext navigateToRoot()

// Access data
String getCurrentViewId()
<T> T getContextData(String viewId, Class<T> type)
List<NavigationNode> getBreadcrumb()
```

#### 2. NavigationNode
**Location**: `com.scheduleengine.navigation.NavigationNode`

Represents a single step in the breadcrumb trail:
- `viewId`: Internal view identifier
- `displayName`: User-friendly name shown in breadcrumb
- `contextObject`: Associated domain object (optional)

#### 3. NavigationHandler
**Location**: `com.scheduleengine.navigation.NavigationHandler`

Functional interface for handling navigation events:
```java
@FunctionalInterface
public interface NavigationHandler {
    void navigate(NavigationContext context);
}
```

#### 4. BreadcrumbBar
**Location**: `com.scheduleengine.navigation.BreadcrumbBar`

Visual component showing the current navigation path:
- Home button (returns to root)
- Clickable breadcrumb items (navigate to that level)
- Current location (non-clickable, bold)

#### 5. DrillDownCard
**Location**: `com.scheduleengine.navigation.DrillDownCard`

Reusable card component for drill-down options:
- Icon, title, and description
- Hover effects
- Click action to navigate deeper

## Navigation Patterns

### Pattern 1: Leagues > League Detail > Teams/Seasons

```
Leagues (List View)
  └─> [View Details] → League Detail (Liverpool FC)
       ├─> View Teams → Teams (filtered by Liverpool FC)
       ├─> View Seasons → Seasons (filtered by Liverpool FC)
       ├─> Edit League → Edit dialog
       └─> View Statistics → Stats view
```

**Breadcrumb**: `Home > Leagues > Liverpool FC > Teams`

### Pattern 2: Teams > Team Detail > Roster/Games

```
Teams (List View)
  └─> [View Details] → Team Detail (Red Sox)
       ├─> View Roster → Roster (filtered by Red Sox)
       ├─> View Games → Games (filtered by Red Sox)
       ├─> View Seasons → Seasons this team participates in
       └─> Edit Team → Edit dialog
```

**Breadcrumb**: `Home > Teams > Red Sox > Roster`

### Pattern 3: Seasons > Season Detail > Teams/Games

```
Seasons (List View)
  └─> [View Details] → Season Detail (Spring 2026)
       ├─> View Teams → Teams in this season
       ├─> View Games → Games in this season
       ├─> Generate Schedule → Schedule generator
       └─> View Schedule → Calendar view
```

**Breadcrumb**: `Home > Seasons > Spring 2026 > Teams`

## Implementation Guide

### Adding Drill-Down to a View

#### Step 1: Add NavigationHandler Support

```java
public class MyView {
    private NavigationHandler navigationHandler;
    
    public void setNavigationHandler(NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
    }
}
```

#### Step 2: Add "View Details" Button

```java
Button viewDetailsBtn = new Button("View Details");
viewDetailsBtn.setOnAction(e -> {
    MyEntity entity = table.getSelectionModel().getSelectedItem();
    NavigationContext newContext = new NavigationContext()
        .navigateTo("my-list", "My List")
        .navigateTo("my-detail", entity.getName(), entity);
    navigationHandler.navigate(newContext);
});
```

#### Step 3: Create Detail View

```java
public class MyDetailView {
    public VBox getView(MyEntity entity, NavigationContext context) {
        VBox container = new VBox(20);
        
        // Header
        Label title = new Label(entity.getName());
        
        // Drill-down cards
        DrillDownCard relatedItems = new DrillDownCard(
            "View Related Items",
            "Browse items related to " + entity.getName(),
            FontAwesomeIcon.LIST,
            () -> {
                NavigationContext newContext = context.navigateTo(
                    "related-items",
                    "Related Items",
                    entity
                );
                navigationHandler.navigate(newContext);
            }
        );
        
        container.getChildren().addAll(title, relatedItems);
        return container;
    }
}
```

#### Step 4: Register Routes in MainView

```java
private void showView(String viewId, NavigationContext context) {
    switch (viewId) {
        case "my-list":
            myView.refresh();
            contentArea.getChildren().add(myView.getView());
            break;
        case "my-detail":
            MyEntity entity = context.getContextData("my-detail", MyEntity.class);
            if (entity != null) {
                contentArea.getChildren().add(myDetailView.getView(entity, context));
            }
            break;
    }
}
```

## Example Navigation Flows

### Example 1: Browse League → View Teams

1. User clicks "Leagues" in sidebar
   - **Context**: `leagues`
   - **Breadcrumb**: `Home > Leagues`

2. User clicks "View Details" on "Premier League"
   - **Context**: `leagues > league-detail` (with League object)
   - **Breadcrumb**: `Home > Leagues > Premier League`

3. User clicks "View Teams" card
   - **Context**: `leagues > league-detail > league-teams` (with League object)
   - **Breadcrumb**: `Home > Leagues > Premier League > Teams`

4. User clicks "Premier League" in breadcrumb
   - **Context**: Returns to `leagues > league-detail`
   - **Breadcrumb**: `Home > Leagues > Premier League`

5. User clicks "Home" button
   - **Context**: Returns to root (empty)
   - **Breadcrumb**: `Home` only

### Example 2: Direct Navigation

User can also navigate directly via sidebar:
1. Clicks "Teams" → Shows all teams
2. Then uses drill-down from there

This provides flexibility - users can:
- **Top-down**: Leagues → Specific League → Teams in that League
- **Direct access**: Teams → All Teams → Specific Team

## View Registry

Currently implemented drill-down views:

| View ID | Display Name | Context Object | Parent Views |
|---------|-------------|----------------|--------------|
| `leagues` | Leagues | - | Root |
| `league-detail` | [League Name] | League | leagues |
| `league-teams` | Teams | League | league-detail |
| `league-seasons` | Seasons | League | league-detail |
| `teams` | Teams | - | Root |
| `team-detail` | [Team Name] | Team | teams, league-teams |
| `seasons` | Seasons | - | Root |
| `season-detail` | [Season Name] | Season | seasons, league-seasons |

## Benefits

### 1. **Contextual Navigation**
Users maintain context as they drill down. The breadcrumb always shows where they are.

### 2. **Flexible Access Patterns**
Multiple paths to the same data:
- Leagues → League → Teams
- Teams → All Teams
- Seasons → Season → Teams

### 3. **Reduced Cognitive Load**
Clear visual indication of location and easy navigation back up the hierarchy.

### 4. **Scalability**
Easy to add new drill-down levels:
- Team → Games → Game Detail → Player Stats
- Season → Weeks → Week Detail → Games

### 5. **Filtering by Context**
When viewing "Teams" from a League detail, automatically filter to show only that league's teams.

## Future Enhancements

### 1. Deep Linking
Save/restore navigation state:
```java
String permalink = context.toPermalink();
// "leagues/123/teams"
```

### 2. History Stack
Browser-like back/forward buttons:
```java
NavigationHistory history = new NavigationHistory();
history.back();
history.forward();
```

### 3. Contextual Actions
Show different actions based on navigation path:
```java
if (context.contains("league-detail")) {
    // Show "Add Team to League" button
}
```

### 4. Breadcrumb Overflow
For deep navigation hierarchies:
```
Home > ... > Season > Teams
```

### 5. Keyboard Shortcuts
- `Alt+←`: Navigate back
- `Alt+Home`: Navigate to root
- `Alt+↑`: Navigate up one level

## Testing

The navigation system maintains compatibility with existing tests. The breadcrumb component is automatically updated when navigation occurs, ensuring UI consistency.

Example test for drill-down:
```java
@Test
void shouldNavigateToLeagueDetail() {
    clickOn("View Details"); // On first league
    verifyThat("Premier League", isVisible()); // Breadcrumb
    verifyThat("View Teams", isVisible()); // Drill-down card
}
```

## Summary

The drill-down navigation system provides:
- ✅ Hierarchical navigation with breadcrumbs
- ✅ Context preservation across views
- ✅ Multiple access patterns
- ✅ Visual drill-down cards
- ✅ Immutable navigation state
- ✅ Easy to extend with new patterns

This creates a more intuitive and professional user experience that scales as the application grows.

