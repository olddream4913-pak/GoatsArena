# Cloud Device Manager Android MVP

## Stack
- Kotlin
- Jetpack Compose + Material 3
- MVVM
- Firebase Authentication (Email/Password + Phone OTP)
- Cloud Firestore

## Setup
1. Create a Firebase project and add an Android app with package `com.clouddevicemanager`.
2. Download `google-services.json` and place it at `android/app/google-services.json`.
3. Enable Email/Password and Phone auth providers in Firebase Authentication.
4. Create Firestore in production mode and deploy security rules.

## Suggested Firestore Rules
```txt
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/devices/{deviceId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Firestore Collection Shape
- `users/{uid}/devices/{deviceId}`
  - `name: string`
  - `osVersion: string`
  - `region: string`
  - `isOnline: boolean`
  - `createdAt: timestamp`