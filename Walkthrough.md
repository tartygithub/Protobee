# Document Processor - Walkthrough

Welcome to the **Corporate Document Processor Walkthrough**! This document provides a step-by-step tour of the user experience, from logging in to processing complex documents and managing the system.

## 1. Landing & Authentication
- **Accessing the Hub:** Navigate to `http://localhost:8080/`. You will be automatically redirected to the secure login screen.
- **Configurable Aesthetics:** The screen showcases a custom enterprise Title and Description read dynamically from the application settings.
- **Default Login Credentials:**
  - **Username:** `admin`
  - **Password:** `admin123`
- **Dynamic Security features:**
  - Login attempts are tracked. If a user enters the wrong password 5 times, the account is automatically locked.
  - If a password is marked as expired, the login failure handler blocks the login and requests administrator intervention.

## 2. Dynamic Account Registration
- Click on **"Create one here"** at the bottom of the login page.
- Enter a unique username and password to provision a new account instantly.
- On successful registration, you are returned to the login screen to sign in.

## 3. The Interactive Dashboard
Once logged in, you enter the main operations screen:
- **Header Metric Bar:** Displays the configurable system Version and Document Code dynamically.
- **Left Panel (Upload Area):** Allows you to select any document (XML, PDF, Word, Excel, PowerPoint) and click **"Process Document"**.
- **Catalog of Processed Files:** Lists all files stored in the database.
- **Search & Filter Controls:**
  - Type matching keywords to filter by filename or deeply search content extracted across all documents.
  - Filter down to specific file formats (e.g., XML, PDF, PowerPoint, Excel, Word).

## 4. Deep Document Inspection
- Click on any document in the processed list.
- The details panel on the right populates instantly.
- The document is broken down into structured tabs, slides, or pages:
  - **PDF documents** show Page 1, Page 2, and so on.
  - **Excel files** show separate sheets as tab nodes.
  - **PowerPoint decks** display Slide 1, Slide 2, containing text extracted from individual shape elements.
  - **XML files** split nodes dynamically.

## 5. User Directory & Maintenance Panel
- Click **"User Maintenance"** in the top navigation bar.
- Inside the administrator directory, you can:
  - Check user statuses (Active or Locked).
  - Use **Lock/Unlock toggles** to instantly suspend or restore user access.
  - Click **"Expire Pwd"** to force immediate password expiration.
  - **Reset Passwords** with basic validation.
  - **Delete User** accounts with confirmation dialogs.

## 6. Embedded Swagger UI
- Click **"Swagger API"** in the navigation bar to inspect and interact with the production REST endpoints directly at `http://localhost:8080/swagger-ui/index.html`.
