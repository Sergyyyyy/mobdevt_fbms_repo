# MOBDEVT_FINALS_REPO
## Recent Changes & Updates

## The are the changes that have been made/

###  Features & Enhancements
- **Idle Screen Timeout Logic**: Implemented an automatic filler/idle screen that triggers after a period of inactivity to improve kiosk security and user experience.
- **401 Deactivation Recovery**: Added robust handling for 401 Unauthorized responses to gracefully deactivate and recover sessions.
- **Survey & Feedback Improvements**: Deployed fixes for survey submission and introduced a robust offline test mode simulation for feedback handling.

###  Technical Improvements
- Migrated core network requests to use a centralized `FeedbackRepository` for cleaner architecture.
- Added test mode offline mock data simulation for safer development and QA testing without hitting production endpoints.
- Merged the main repository branches to consolidate the core features of the FBMS Mobile App.

