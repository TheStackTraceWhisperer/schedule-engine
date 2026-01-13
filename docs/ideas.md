# Ideas & Feature Backlog

Ideas in development that need further refinement before implementation. Items can be groomed here and migrated to the backlog.

---

## Implementation Priority Matrix


| Feature             | Priority | Effort | Dependencies    | Status      |
|---------------------|----------|--------|-----------------|-------------|
| Automatic Standings | High     | Medium | Games           | In Progress |
| Email Notifications | High     | Medium | Email API       | Backlog     |
| Schedule Publishing | Medium   | Medium | PDF lib         | Backlog     |
| Blackout Dates      | Medium   | Medium | Scheduling      | Backlog     |
| League Statistics   | Medium   | Medium | Game data       | Backlog     |
| Team Statistics     | Medium   | Medium | Game data       | Backlog     |
| SMS Notifications   | Medium   | Medium | SMS API         | Backlog     |
| Social Media        | Low      | Medium | Socials API     | Future      |
| Website Integration | Low      | Medium | Web hosting     | Future      |
| On-Call System      | Low      | Medium | Notifications   | Future      |
| Player Statistics   | Medium   | Large  | Game data       | Backlog     |


---

## Detailed Feature Descriptions

---

## 1. Registration System

### Online Registration
- **Description**: Allow teams, players, and coaches to register online instead of manual entry
- **Use Cases**:
  - New teams join a league mid-season
  - Individual players register for specific seasons
  - Coaches self-register and manage their team
- **Features**:
  - Registration forms with validation
  - Email confirmation workflow
  - Payment integration before approval
  - Registration deadlines per season/event
- **Priority**: High (improves user experience)
- **Dependencies**: Payment system, Email system
- **Estimated Effort**: Medium (2-3 weeks)

### Liability Waivers
- **Description**: Digital liability waiver collection and tracking
- **Use Cases**:
  - New players must sign waiver before playing
  - Annual waiver renewal requirements
  - Waiver compliance reports
- **Features**:
  - PDF waiver generation and signing
  - E-signature integration
  - Expiration date tracking
  - Signed waiver archival
- **Priority**: High (legal requirement)
- **Dependencies**: E-signature provider (DocuSign, HelloSign)
- **Estimated Effort**: Medium (2 weeks)

### Payment Agreements
- **Description**: Define and track payment terms and agreements
- **Use Cases**:
  - League entry fees
  - Team registration fees
  - Payment plans and installments
- **Features**:
  - Payment term templates
  - Agreement acceptance tracking
  - Automatic payment scheduling
  - Payment status monitoring
- **Priority**: High (revenue tracking)
- **Dependencies**: Payment system
- **Estimated Effort**: Medium (2-3 weeks)

---

## 2. Payments System

### Due Reports
- **Description**: Generate reports of outstanding payments by league, team, or individual
- **Use Cases**:
  - Finance reports for league management
  - Team manager needs to know if payments are due
  - Individual player payment status
- **Features**:
  - Customizable report date ranges
  - Payment status filtering (paid, overdue, pending)
  - Aging reports (30/60/90 days overdue)
  - Export to CSV/PDF
- **Priority**: High (financial tracking)
- **Dependencies**: Payment tracking, User roles
- **Estimated Effort**: Medium (2 weeks)

### Invoices
- **Description**: Generate and send invoices for fees and charges
- **Use Cases**:
  - Invoice teams for league fees
  - Send payment reminders
  - Track payment history
- **Features**:
  - Auto-generated invoices from registration/agreements
  - Invoice templates by league/event
  - Invoice history and versioning
  - Payment receipt generation
  - Recurring invoice scheduling
- **Priority**: High (professional operation)
- **Dependencies**: Email system, Payment tracking
- **Estimated Effort**: Medium (2-3 weeks)

---

## 3. Events System

### Classes
- **Description**: Manage skill-building or training classes separate from regular games
- **Use Cases**:
  - Coaching clinics
  - Skill development sessions
  - Referee training courses
- **Features**:
  - Class scheduling and roster management
  - Class attendance tracking
  - Instructor assignment
  - Class capacity limits
- **Priority**: Medium (future feature)
- **Dependencies**: Scheduling, User roles
- **Estimated Effort**: Medium (2-3 weeks)

### Camps
- **Description**: Multi-day or multi-week sports camps and tournaments
- **Use Cases**:
  - Summer soccer camps
  - Basketball clinics
  - Skills development camps
- **Features**:
  - Camp creation with multi-day schedules
  - Team/group assignments
  - Accommodation tracking (if residential)
  - Camp fee management
  - Participant attendance tracking
- **Priority**: Medium (seasonal/optional)
- **Dependencies**: Events system, Registration, Payments
- **Estimated Effort**: Large (3-4 weeks)

---

## 4. Mass Communication

### Email Notifications
- **Description**: Automated email communications to leagues, teams, and players
- **Use Cases**:
  - Schedule reminders before games
  - Payment due notifications
  - Registration confirmation
  - Season announcements
- **Features**:
  - Email templates by event type
  - Scheduled batch sending
  - Email delivery tracking
  - Unsubscribe management
  - Attachment support (rosters, schedules)
- **Priority**: High (essential communication)
- **Dependencies**: Email provider (SendGrid, Mailgun)
- **Estimated Effort**: Medium (2 weeks)

### SMS Text Notifications
- **Description**: Short text message alerts for urgent/immediate notifications
- **Use Cases**:
  - Game cancellation alerts
  - Last-minute roster changes
  - Payment reminders
  - Venue changes
- **Features**:
  - SMS template system
  - Phone number management per user
  - SMS delivery tracking
  - Opt-in/opt-out management
- **Priority**: Medium (nice to have)
- **Dependencies**: SMS provider (Twilio)
- **Estimated Effort**: Medium (2 weeks)

### Social Media Integration
- **Description**: Publish schedule updates to social media platforms
- **Use Cases**:
  - Post schedule to Facebook/Twitter/Instagram
  - Share game results
  - Announce standings
- **Features**:
  - Social media account linking
  - Auto-post schedule updates
  - Result announcements
  - Post scheduling and drafts
- **Priority**: Low (marketing/engagement)
- **Dependencies**: Social media APIs (Facebook Graph, Twitter API)
- **Estimated Effort**: Medium (2-3 weeks)

### Website Integration
- **Description**: Publish league information to external website
- **Use Cases**:
  - Public league website updates
  - Standings published online
  - Schedule available to public
- **Features**:
  - Website data export
  - Static site generation (Jekyll, Hugo)
  - API for custom website integration
  - Public/private access controls
- **Priority**: Low (marketing/communication)
- **Dependencies**: Web hosting, API design
- **Estimated Effort**: Medium (2-3 weeks)

---

## 5. Advanced Scheduling Features

### Schedule Publishing
- **Description**: Export and distribute schedules in various formats
- **Use Cases**:
  - Download schedule as PDF/ICS
  - Embed schedule on website
  - Email schedule to teams
- **Features**:
  - PDF schedule generation
  - iCal format for calendar apps
  - Excel export
  - Printable schedule templates
  - Schedule versioning
- **Priority**: Medium (useful feature)
- **Dependencies**: PDF generation library, Calendar support
- **Estimated Effort**: Medium (2 weeks)

### Referee Management
- **Description**: Assign and track referees for games
- **Use Cases**:
  - Assign referee to each game
  - Track referee availability
  - Manage referee payments
  - Referee performance ratings
- **Features**:
  - Referee roster management
  - Availability calendar
  - Game assignment tracking
  - Referee statistics and ratings
  - Payment tracking per referee
- **Priority**: Medium (if league uses refs)
- **Dependencies**: User roles, Payments system
- **Estimated Effort**: Large (3-4 weeks)

### Blackout Dates
- **Description**: Prevent scheduling on specific dates for teams, referees, or fields
- **Use Cases**:
  - Team traveling during blackout week
  - Referee unavailable on certain dates
  - Field maintenance scheduled
  - Holiday breaks
- **Features**:
  - Blackout date calendar per team/referee/field
  - Season-wide blackout dates
  - Recurring blackout dates (holidays)
  - Schedule conflict detection
  - Auto-rescheduling options
- **Priority**: Medium (prevents scheduling conflicts)
- **Dependencies**: Scheduling system
- **Estimated Effort**: Medium (2-3 weeks)

### On-Call / Substitution System
- **Description**: Manage player/referee on-call rotation for substitutions
- **Use Cases**:
  - Player call-up rotation tracking
  - Referee backup assignments
  - Last-minute substitutions
- **Features**:
  - On-call rotation tracking
  - Notification system
  - Call acceptance/decline tracking
  - History of call-ups
- **Priority**: Low (optional feature)
- **Dependencies**: Notifications system
- **Estimated Effort**: Medium (2 weeks)

---

## 6. Standings & Rankings

### Automatic Standings Generation
- **Description**: Automatically calculate and update league standings based on game results
- **Use Cases**:
  - Live standings updates
  - Playoff seeding
  - Final season rankings
- **Features**:
  - Win/loss record calculation
  - Goal/point differential
  - Strength of schedule
  - Tiebreaker rules (configurable)
  - Historical standings per week
  - Playoff bracket generation
- **Priority**: High (core feature)
- **Dependencies**: Game results, Tiebreaker rules
- **Estimated Effort**: Medium (2 weeks) - **Currently In Progress**

---

## 7. Statistics & Analytics

### League Statistics
- **Description**: Aggregate statistics across entire league
- **Use Cases**:
  - League-wide goal average
  - Top scorers across all teams
  - Most consistent teams
  - League trends analysis
- **Features**:
  - League-wide stat aggregation
  - Trend analysis (this season vs last)
  - Custom stat reports
  - Export capabilities
- **Priority**: Medium (analytics/reporting)
- **Dependencies**: Game data, Goals/scoring data
- **Estimated Effort**: Medium (2-3 weeks)

### Team Statistics
- **Description**: Track detailed statistics for each team
- **Use Cases**:
  - Team performance tracking
  - Win streaks/losing streaks
  - Home vs away performance
  - Seasonal trends
- **Features**:
  - Win/loss records
  - Goal differential
  - Home/away splits
  - Performance vs specific opponents
  - Season-to-season comparison
- **Priority**: Medium (team management)
- **Dependencies**: Game data, Standings system
- **Estimated Effort**: Medium (2-3 weeks)

### Individual Player Statistics
- **Description**: Track player-level statistics
- **Use Cases**:
  - Goals scored by player
  - Games played/missed
  - MVP tracking
  - Performance trends
- **Features**:
  - Goals/points per player
  - Attendance tracking
  - Performance ratings
  - All-star selections
  - Player awards
- **Priority**: Medium (player engagement)
- **Dependencies**: Game data, Player roster, Scoring system
- **Estimated Effort**: Large (3-4 weeks)

---

## Front of House (User-Facing Features)

### Summary

| Feature             | Priority | Effort | Dependencies    | Status      |
|---------------------|----------|--------|-----------------|-------------|
| Online Registration | High     | Medium | Payments        | Backlog     |
| Liability Waivers   | High     | Medium | E-sig           | Backlog     |
| Payment Agreements  | High     | Medium | Payments        | Backlog     |
| Classes             | Medium   | Medium | Events          | Future      |
| Camps               | Low      | Large  | Events          | Future      |

---

### Detailed Feature Descriptions

#### Online Registration
- **Description**: Allow teams, players, and coaches to register online instead of manual entry
- **Use Cases**:
  - New teams join a league mid-season
  - Individual players register for specific seasons
  - Coaches self-register and manage their team
- **Features**:
  - Registration forms with validation
  - Email confirmation workflow
  - Payment integration before approval
  - Registration deadlines per season/event
- **Priority**: High (improves user experience)
- **Dependencies**: Payment system, Email system
- **Estimated Effort**: Medium (2-3 weeks)

#### Liability Waivers
- **Description**: Digital liability waiver collection and tracking
- **Use Cases**:
  - New players must sign waiver before playing
  - Annual waiver renewal requirements
  - Waiver compliance reports
- **Features**:
  - PDF waiver generation and signing
  - E-signature integration
  - Expiration date tracking
  - Signed waiver archival
- **Priority**: High (legal requirement)
- **Dependencies**: E-signature provider (DocuSign, HelloSign)
- **Estimated Effort**: Medium (2 weeks)

#### Payment Agreements
- **Description**: Define and track payment terms and agreements
- **Use Cases**:
  - League entry fees
  - Team registration fees
  - Payment plans and installments
- **Features**:
  - Payment term templates
  - Agreement acceptance tracking
  - Automatic payment scheduling
  - Payment status monitoring
- **Priority**: High (revenue tracking)
- **Dependencies**: Payment system
- **Estimated Effort**: Medium (2-3 weeks)

#### Classes
- **Description**: Manage skill-building or training classes separate from regular games
- **Use Cases**:
  - Coaching clinics
  - Skill development sessions
  - Referee training courses
- **Features**:
  - Class scheduling and roster management
  - Class attendance tracking
  - Instructor assignment
  - Class capacity limits
- **Priority**: Medium (future feature)
- **Dependencies**: Scheduling, User roles
- **Estimated Effort**: Medium (2-3 weeks)

#### Camps
- **Description**: Multi-day or multi-week sports camps and tournaments
- **Use Cases**:
  - Summer soccer camps
  - Basketball clinics
  - Skills development camps
- **Features**:
  - Camp creation with multi-day schedules
  - Team/group assignments
  - Accommodation tracking (if residential)
  - Camp fee management
  - Participant attendance tracking
- **Priority**: Low (seasonal/optional)
- **Dependencies**: Events system, Registration, Payments
- **Estimated Effort**: Large (3-4 weeks)

---

## Notes

- Features marked as "High Priority" should be tackled first
- "In Progress" items are currently under development
- "Backlog" items are ready to be groomed into sprint tasks
- "Future" items need more research and refinement
- All features should consider user roles and permissions
- Email/SMS/Social features require external API integrations

