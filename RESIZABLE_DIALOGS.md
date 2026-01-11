ue # ✅ Resizable Dialogs with Preferences - Implementation Complete!

## Overview

All modal dialogs in the Schedule Engine application are now **resizable** with **persistent size preferences**. Users can resize any dialog to their preference, and the application will remember those dimensions for future use.

## What Was Implemented

### 1. **DialogUtil Utility Class**
**Location**: `com.scheduleengine.common.DialogUtil`

A centralized utility for making dialogs resizable with automatic preference management:

```java
// Make a dialog resizable with default size
DialogUtil.makeResizable(dialog, "league.add", 550, 400);

// The dialog ID is used to save/restore preferences
// Default sizes: width=550, height=400
```

**Features**:
- Sets dialog as resizable
- Sets minimum size (400x300)
- Restores last used size from preferences
- Automatically saves size when dialog closes
- Uses Java Preferences API for persistence

### 2. **Updated All Dialogs**

All add/edit dialogs across the application have been updated:

| Dialog | Dialog ID | Default Size | Fields Widened |
|--------|-----------|--------------|----------------|
| **League - Add** | `league.add` | 550 x 400 | ✓ (350px) |
| **League - Edit** | `league.edit` | 550 x 400 | ✓ (350px) |
| **Season - Add** | `season.add` | 600 x 450 | ✓ (350px) |
| **Season - Edit** | `season.edit` | 600 x 450 | ✓ (350px) |
| **Team - Add** | `team.add` | 600 x 450 | ✓ (350px) |
| **Team - Edit** | `team.edit` | 600 x 450 | ✓ (350px) |
| **Field - Add** | `field.add` | 600 x 400 | ✓ (350px) |
| **Field - Edit** | `field.edit` | 600 x 400 | ✓ (350px) |

### 3. **Wider Input Fields**

All input fields now have `setPrefWidth(350)` instead of relying on default sizing:
- TextFields: 350px wide
- TextAreas: 350px wide
- ComboBoxes: 350px wide
- DatePickers: 350px wide

**Before**: Fields were too narrow, text would be cut off
**After**: Fields are comfortably wide, accommodating longer text

## User Experience

### Resizing Dialogs

1. **First Time Opening**: Dialog opens at default size (e.g., 550x400)
2. **User Resizes**: Drag corner or edge to resize
3. **Dialog Closes**: Size is automatically saved
4. **Next Time**: Dialog opens at the last saved size

### Per-Dialog Preferences

Each dialog type has its own saved preferences:
- Resize the "Add League" dialog → Only affects "Add League"
- Resize the "Edit Season" dialog → Only affects "Edit Season"
- Each dialog remembers its own dimensions independently

### Minimum Size Enforcement

All dialogs have a minimum size of **400x300** to prevent them from becoming too small to use.

## Technical Implementation

### Preference Storage

Preferences are stored using Java's `Preferences` API:
- **Storage Location**: Platform-specific user preferences
  - **Linux**: `~/.java/.userPrefs/com/scheduleengine/common/prefs.xml`
  - **Windows**: Windows Registry
  - **macOS**: `~/Library/Preferences`

- **Keys Format**:
  - Width: `{dialogId}.width` (e.g., `league.add.width`)
  - Height: `{dialogId}.height` (e.g., `league.add.height`)

### Dialog Initialization Pattern

All dialogs follow this pattern:

```java
Dialog<Entity> dialog = new Dialog<>();
dialog.setTitle("Add/Edit Entity");

// ... setup dialog content ...

dialog.getDialogPane().setContent(grid);

// Make dialog resizable and restore size
dialog.getDialogPane().getScene().getWindow().setOnShown(e -> 
    DialogUtil.makeResizable(dialog, "entity.add", 600, 450));

dialog.showAndWait().ifPresent(entity -> {
    // ... save logic ...
});
```

**Why `setOnShown`?**: The dialog's window isn't available until after it's shown, so we configure resizability in the `onShown` event handler.

## Files Modified

### New Files Created
1. **`DialogUtil.java`** - Utility class for dialog resizing and preferences

### Modified Files
1. **`LeagueView.java`**
   - Added DialogUtil import
   - Updated `showAddDialog()` - resizable with 350px fields
   - Updated `showEditDialog()` - resizable with 350px fields

2. **`LeagueDetailView.java`**
   - Added DialogUtil import
   - Updated `showEditDialog()` - resizable with 350px fields

3. **`SeasonView.java`**
   - Added DialogUtil import
   - Updated `showAddDialog()` - resizable with 350px fields
   - Updated `showEditDialog()` - resizable with 350px fields

4. **`TeamView.java`**
   - Added DialogUtil import
   - Updated `showAddDialog()` - resizable with 350px fields
   - Updated `showEditDialog()` - resizable with 350px fields

5. **`FieldView.java`**
   - Added DialogUtil import
   - Updated `showAddDialog()` - resizable with 350px fields
   - Updated `showEditDialog()` - resizable with 350px fields

## Benefits

### 1. **Better Usability**
- Users with longer league/team names can resize dialogs to see full text
- No more cut-off text in fields
- Accommodates different screen sizes and resolutions

### 2. **Persistent Preferences**
- Each user's preferences are remembered
- No need to resize every time
- Works across application restarts

### 3. **Flexible Layout**
- Different default sizes for different dialog types
- Season/Team dialogs (600x450) are taller for more fields
- League dialogs (550x400) are slightly smaller

### 4. **Minimum Size Protection**
- Dialogs can't be made too small (400x300 minimum)
- Ensures usability even if user tries to make it tiny

## Testing

✅ **All 64 tests pass**
✅ **Compilation successful** (45 classes compiled)
✅ **No errors or warnings**

### Manual Testing Checklist

To verify the feature works:

1. **Open a dialog** (e.g., Add League)
2. **Resize it** to a larger size
3. **Click Save or Cancel** to close
4. **Open the same dialog again**
5. **Verify** it opens at the new size

### Preference Reset

If needed, preferences can be reset:

```java
// Reset all dialog preferences
DialogUtil.resetAllPreferences();

// Reset specific dialog
DialogUtil.resetPreferences("league.add");
```

## Edge Cases Handled

### 1. **First-Time Use**
- No preferences exist → Uses default size
- User resizes → Preference is created

### 2. **Invalid Stored Size**
- Preference contains invalid data → Falls back to default
- User has very small screen → Minimum size (400x300) enforced

### 3. **Multiple Monitors**
- Dialogs can be resized on any monitor
- Size is remembered regardless of which monitor was used

### 4. **Dialog ID Uniqueness**
- Each dialog type has unique ID
- Add vs Edit dialogs have separate preferences
- No conflicts between different entity types

## Future Enhancements

### 1. **Position Persistence**
Also save the dialog's screen position:
```java
// Save position as well as size
stage.setX(savedX);
stage.setY(savedY);
```

### 2. **Maximize State**
Remember if user maximized the dialog:
```java
stage.setMaximized(savedMaximizedState);
```

### 3. **Export/Import Preferences**
Allow users to export their preferences:
```java
DialogUtil.exportPreferences("preferences.xml");
DialogUtil.importPreferences("preferences.xml");
```

### 4. **Reset Button in Settings**
Add a settings menu with "Reset Dialog Sizes" button

### 5. **Per-User Profiles**
Support multiple user profiles with different preferences

## Examples

### Before Changes
```
┌─────────────────────────────┐
│ Add League                  │
├─────────────────────────────┤
│ Name:      [Short Field]    │
│ Description: [Short Area]   │
├─────────────────────────────┤
│        [Save] [Cancel]      │
└─────────────────────────────┘

Problem: 
- Cannot resize
- Fields too narrow for long names
- Fixed size uncomfortable for some users
```

### After Changes
```
┌─────────────────────────────────────┐
│ Add League                    [↔][↕]│ ← Resize handles
├─────────────────────────────────────┤
│ Name:      [Wider Field.........]   │
│ Description: [Wider Area........]   │
│              [..................]   │
├─────────────────────────────────────┤
│            [Save] [Cancel]          │
└─────────────────────────────────────┘

Benefits:
✓ Can resize to any size
✓ Fields are wider (350px)
✓ Size is remembered
✓ Minimum size enforced
```

## Code Example: DialogUtil Usage

```java
private void showAddDialog() {
    Dialog<League> dialog = new Dialog<>();
    dialog.setTitle("Add League");
    
    // Setup dialog content
    GridPane grid = new GridPane();
    TextField nameField = new TextField();
    nameField.setPrefWidth(350); // Wider field
    // ... add more fields ...
    
    dialog.getDialogPane().setContent(grid);
    
    // Magic happens here!
    dialog.getDialogPane().getScene().getWindow().setOnShown(e -> 
        DialogUtil.makeResizable(dialog, "league.add", 550, 400));
    
    dialog.showAndWait().ifPresent(league -> {
        leagueService.save(league);
    });
}
```

## Summary

🎉 **All dialogs are now resizable with persistent size preferences!**

**Key Achievements**:
- ✅ Created reusable `DialogUtil` utility
- ✅ Updated 8 dialog types (add/edit for 4 entities)
- ✅ Widened all input fields to 350px
- ✅ Implemented preference persistence
- ✅ Set sensible default sizes per dialog type
- ✅ Enforced minimum size (400x300)
- ✅ All 64 tests passing

**User Benefits**:
- No more struggling with narrow fields
- Each user's preferences are saved
- Dialogs adapt to user's workflow
- Works across application restarts
- Professional, modern UX

The application now provides a much better user experience with dialogs that adapt to individual user needs and remember their preferences! 🚀

