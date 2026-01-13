# UI Persistence Issues Review - Action Plan

## Issues Identified

### 1. FieldView Missing Table Persistence
**Problem:** FieldView has 3 tables but only `table` (fields list) has column persistence setup.
**Missing:**
- `availabilityTable` (Hours of Operation)
- `usageTable` (Dedicated Use Blocks)

**Fix:** Add `TablePreferencesUtil.setupTableColumnPersistence()` calls for both tables.

### 2. Dialog Window Size Persistence
**Problem:** User mentioned that modal dialogs should persist their sizes.
**Current:** Dialog sizes are not being persisted.
**Fix:** Need to implement dialog size persistence for all dialog windows.

### 3. Table Column Persistence Issues
**Problem:** Multiple views may not be persisting column widths correctly.
**Need to Verify:**
- LeagueView ✓ (has persistence)
- TeamView ✓ (has persistence)
- FieldView ✗ (only main table, missing 2 sub-tables)
- SeasonView ✓ (has persistence)
- GameView ✓ (has persistence)
- RosterView ✓ (has persistence)
- TournamentView ✓ (has persistence)
- ScheduleGeneratorResultView ✓ (has persistence)

### 4. Window Position and Size Persistence
**Status:** ✓ Already implemented in WindowPreferencesUtil
- Window width/height
- Window position (x, y)
- Maximized state
- Periodic saving

### 5. UI Scale Persistence
**Status:** ✓ Already implemented in UIScaleUtil

## Implementation Plan

1. ✓ Add table persistence to FieldView availability table
2. ✓ Add table persistence to FieldView usage blocks table
3. ✓ Create DialogPreferencesUtil for modal dialog size persistence
4. ✓ Apply dialog persistence to all dialogs
5. ✓ Verify all table views have unique IDs
6. ✓ Test and validate all persistence features

## Files to Modify

1. `/src/main/java/com/scheduleengine/field/FieldView.java` - Add table persistence
2. `/src/main/java/com/scheduleengine/common/DialogPreferencesUtil.java` - NEW FILE
3. Multiple view files - Add dialog persistence

