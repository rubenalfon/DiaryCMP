# Diary CMP - A Compose Multiplatform Application

Diary CMP is an example of how **Compose Multiplatform** can be used as a real, practical, and powerful framework for developing multiplatform applications. This app includes:

- **Firebase authentication** (login/register)
- **In-device encryption** for enhanced security
- **Beautiful statistics** for insights
- **Platform personalization** for a better user experience

## About the App

The goal of this app is to provide a **secure bullet journal** across multiple platforms. Users can:

- Write and manage their diary entries
- Adjust the order of diary components
- View all registered diary entries
- Analyze statistics
- Set up notifications to remind them to write their diary

## Firebase Setup (Required)

For security reasons, you **must** provide a Firebase repository. It's simple to set up:

1. **Create a Firebase repository** (any name and details work).
2. **Initialize Firestore** and add the following security rules:

```plaintext
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // Allow access to subcollections under each user.
      match /{document=**} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

3. **Enable Email Authentication**, but **disable "Email link (passwordless sign-in)"**.
4. **Retrieve the Web API Key and the project ID**:
  - Go to **Project Settings** in Firebase.
  - Copy the **Web API Key** (example: `AIzaSyCr9H27...` **(this one won’t work, use your own!)**).
  - Copy the **Project ID**
5. **Update the project**:
  - Open the project in your IDE.
  - Locate the file: `diary/composeApp/src/commonMain/kotlin/es/diaryCMP/utilsModule/utils/FirebaseConstants.kt`
  - Replace the constant `APIKEY` with **your Web API Key**
  - Replace the constant `PROJECT_ID` with **your Project ID**

---

## Running the Project

### Prerequisites

Before running the app, make sure you have:

- **JDK 17 or higher** installed.
- A `local.properties` file in the project root, specifying the **Android SDK path**.

### Android

To run on an **Android device or emulator**:

- Open the project in **Android Studio**.
- Run the imported Android **Run Configuration**.

### Desktop (JVM)

To run on **desktop**:

- Add a Gradle **run configuration**.
- In the **Tasks and arguments** input, enter: `:composeApp:run`.
- Select and **run the configuration**.
- Alternatively, run from the terminal:
  ```sh
  ./gradlew :desktopApp:run
  ```

### iOS

To run on an **iPhone device or simulator**:

- Check your system compatibility using [KDoctor](https://github.com/Kotlin/kdoctor).
- Open `iosApp/iosApp.xcproject` in **Xcode** and run the standard configuration.
- Alternatively, use the [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) in Android Studio.

---

## 🚀 Get Started & Contribute

Feel free to explore, contribute, or improve this project! PRs are welcome. 😊

