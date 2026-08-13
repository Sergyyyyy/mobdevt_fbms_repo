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

### 📁 Modified Files
- `app/src/main/java/ph/edu/benilde/fbms/MainActivity.kt`
  *(Updated to handle inactivity and trigger the idle timeout logic)*
- `app/src/main/java/ph/edu/benilde/fbms/ui/WelcomeScreen.kt`
  *(Added filler/idle screen UI and timeout reset handlers)*
- `app/src/main/java/ph/edu/benilde/fbms/data/repository/FeedbackRepository.kt`
  *(Implemented test mode offline simulation and mocked feedback responses)*
- `app/src/main/java/ph/edu/benilde/fbms/data/AuthInterceptor.kt`
  *(Added recovery logic and token validation for 401 Unauthorized API responses)*
- `gradle/wrapper/gradle-wrapper.properties`
  *(Minor build configuration updates)*
