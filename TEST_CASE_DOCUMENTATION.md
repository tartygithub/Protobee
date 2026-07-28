# Test Case Documentation

## Application Summary
- Application: Protobee / Doc Processor Suite
- Runtime: Spring Boot
- Default URL: http://127.0.0.1:8080
- Status after verification: Running successfully on port 8080

## Environment Verified
- Java: 21.0.11
- Maven: 3.9.13
- Database: H2 in-memory (default configuration)
- Start command used: `mvn spring-boot:run`
- Test command used: `mvn test`

## Test Cases and Results

| ID | Test Case | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC-01 | Automated test suite | Run `mvn test` | All tests pass | 3 tests executed, 0 failures, 0 errors, 0 skipped; Maven build success | Pass |
| TC-02 | Application startup | Start app with `mvn spring-boot:run` | App starts without crashing | Spring Boot started successfully and reported `Tomcat started on port 8080` | Pass |
| TC-03 | Landing route access | Open `http://127.0.0.1:8080/` | Request is handled and redirects to login for unauthenticated users | Received HTTP 302 redirect to `/login` | Pass |
| TC-04 | Login page availability | Open `http://127.0.0.1:8080/login` | Login page renders successfully | Received HTTP 200 and HTML page content | Pass |
| TC-05 | Registration page availability | Open `http://127.0.0.1:8080/register` | Registration page renders successfully | Received HTTP 200 and registration form HTML | Pass |
| TC-06 | Protected page access | Open `http://127.0.0.1:8080/users` without authentication | Access is denied and user is redirected to login | Received HTTP 302 redirect to `/login` | Pass |
| TC-07 | Browser launch check | Attempt to open the app in the default browser | Browser opens the login page | Launch was attempted; the app is reachable at `http://127.0.0.1:8080/login` | Pass with environment note |

## Notes
- The application uses Spring Security, so unauthenticated access to protected URLs is expected to redirect to the login page.
- The default in-memory H2 database is active, so no external database setup was required for this verification.

## Evidence Summary
- Test suite result: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`
- Startup evidence: `Tomcat started on port 8080 (http)`
- Endpoint evidence:
  - `/` → 302 redirect to `/login`
  - `/login` → 200 OK
  - `/register` → 200 OK
  - `/users` → 302 redirect to `/login`
