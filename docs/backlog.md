# Backlog

Features that are groomed and ready to start

---

## Backlog Items

### Back of House (Administrative/Operational Features)

| Feature             | Priority | Effort | Dependencies    | Status      |
|---------------------|----------|--------|-----------------|-------------|
| Referee Scheduling  | High     | Small  | Scheduling      | Backlog     |
| Payments (Unified)  | High     | Medium | —               | Backlog     |

---

## Detailed Feature Descriptions

---

## Back of House (Administrative/Operational Features)

---

### Payments (Unified)
A minimal, ergonomic layer on top of the current Excel workflow. Focus: fast manual entry, quick filtering, and simple CSV import/export. No heavy accounting.

- **Concept**: Agreements (optional) -> Transactions (invoices/payments) -> Simple reports
- **Scope**: Manual-first UI, CSV in/out, basic statuses, zero complex integrations by default.

#### Minimal Data Model
- Transaction(id, partyType: Team|Player, partyId, category: Agreement|Invoice|Payment, date, amount, status: Pending|Paid|Overdue, notes)
- Optional: Agreement(id, partyType, partyId, name, amount, schedule[])

#### Ergonomics
- Inline editable tables with validation (date/amount/status)
- Fast filtering and sorting by party, status, date range
- Bulk actions: mark paid, export selected
- CSV import (map columns) and CSV export (current view)

#### Reports (Lightweight)
- Due items (Pending/Overdue by party)
- Aging buckets (30/60/90)
- Totals by month/league/team

- **Priority**: High (replace Excel with better ergonomics)
- **Dependencies**: None (optional SMTP/PDF later)
- **Estimated Effort**: Medium (2–3 weeks)

#### Checklist
- [ ] Data & persistence:
  - [ ] Transaction entity + repository
  - [ ] (Optional) Agreement entity + repository
- [ ] UI:
  - [ ] Transaction list with inline edit (date/amount/status/notes)
  - [ ] Filters: party, status, date range; quick totals footer
  - [ ] Bulk actions: mark paid; export selected
- [ ] Import/Export:
  - [ ] CSV import wizard (column mapping, validation preview)
  - [ ] CSV export of current filtered view
- [ ] Reports:
  - [ ] Due/Overdue view with totals
  - [ ] Aging buckets 30/60/90
  - [ ] Monthly totals by party/league/team
- [ ] Seed data:
  - [ ] Sample transactions covering pending/paid/overdue
- [ ] Tests:
  - [ ] CSV import mapping and validation
  - [ ] Inline edit validations (date/amount/status)
  - [ ] Bulk actions behavior
  - [ ] Reporting totals and aging calculations

---

### Referee Scheduling (Scheduling Constraint)
- **Description**: Assign referees to games as a scheduling constraint (no role management, availability tracking, or compensation)
- **Use Cases**:
  - Assign referee to each game during scheduling
  - Ensure referee isn't double-booked for overlapping games
- **Features**:
  - Referee roster (simple list of names)
  - Assign referee to game during creation/editing
  - Prevent scheduling conflicts (ref can't be assigned to overlapping games)
- **Priority**: High
- **Dependencies**: Scheduling system
- **Estimated Effort**: Small (1 week)

#### Checklist
- [ ] Domain & persistence:
  - [ ] Simple Referee entity (name-only)
  - [ ] Link Game -> Referee (optional association)
- [ ] UI:
  - [ ] Referee CRUD list (add/edit/delete)
  - [ ] Game create/edit: referee selector
- [ ] Logic:
  - [ ] Conflict detection for overlapping games by referee
  - [ ] Prevent saving conflicting assignments (or warn + block)
  - [ ] Visual warning in schedule views for conflicts
- [ ] Seed data:
  - [ ] A few referees; sample game with assignments
- [ ] Tests:
  - [ ] Assign referee to a game
  - [ ] Detect double-booking across overlapping games
  - [ ] Reassign referee clears conflict

---

## Notes

- Features marked as "High Priority" should be tackled first
- "Backlog" items are ready to be groomed into sprint tasks
- "Future" items need more research and refinement
- Avoid heavy integrations until ergonomics are solid (CSV first)
