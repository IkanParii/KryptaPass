<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase"/>
  <img src="https://img.shields.io/badge/AES%20256-10a94c?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0id2hpdGUiPjxwYXRoIGQ9Ik0xMiAxTDMgNXY2YzAgNS41NSAzLjg0IDEwLjc0IDkgMTIgNS4xNi0xLjI2IDktNi40NSA5LTEyVjVsLTktNHoiLz48L3N2Zz4=&logoColor=white" alt="AES-256"/>
</p>

<h1 align="center">🔐 KryptaPass</h1>
<p align="center">
  <strong>Enterprise-Grade Password Manager for Android</strong><br>
  Secure. Encrypted. Biometric-Ready.
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#security">Security</a> •
  <a href="#setup">Setup</a> •
  <a href="#license">License</a>
</p>

---

## 📋 Overview

KryptaPass is a modern, offline-first password manager for Android built with **Kotlin** and **Jetpack Compose**. It stores your credentials securely in **Firebase Firestore** using **AES-256/GCM** encryption, with all decryption happening exclusively on-device — your master password never leaves your phone.

> **Why KryptaPass?** Unlike cloud-based managers that hold your decryption keys, KryptaPass uses a zero-knowledge architecture: the server stores only encrypted blobs. Only you hold the key.

## ✨ Features

### 🔑 Core Management
| Feature | Description |
|---------|-------------|
| **Account Vault** | Store platform name, username/email, password, URL, category, and notes |
| **Smart Search** | Filter accounts by name, category, or username in real-time |
| **Password Generator** | Generate strong passwords with customizable length & complexity |
| **Strength Analyzer** | Real-time password strength, crack-time estimation, entropy calculation, and username-similarity check |

### 🛡️ Security
| Feature | Description |
|---------|-------------|
| **AES-256/GCM Encryption** | All passwords encrypted before leaving your device using your master password |
| **PBKDF2 Key Derivation** | 65,536 iterations of PBKDF2 with HMAC-SHA256 to derive your encryption key |
| **Per-User Salt** | Unique 16-byte random salt generated at registration, stored in Firestore |
| **Biometric Lock** | Fingerprint / Face Unlock to access sensitive password details |
| **Auto-Lock** | Encryption session wiped automatically when app goes to background |
| **Data Corruption Interception** | Blocks write operations if the encryption key is invalid, preventing data corruption |

### 🖥️ User Experience
| Feature | Description |
|---------|-------------|
| **Firebase Authentication** | Email/password login with Indonesian-language error messages |
| **Real-Time Sync** | Instant cross-device sync via Firestore snapshot listeners |
| **Audit Log** | Track login, add, update, and delete activities in Firestore |
| **Session Expiry Dialog** | Premium dialog when session expires after 1 minute in background |
| **Offline Detection** | Interactive dialog when network is unavailable during critical operations |
| **Profile Screen** | View email, registered password count, and biometric-protected credentials |

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + MVI (Intent/StateFlow) |
| **Dependency Injection** | Manual (AppContainer service locator) |
| **Database** | Firebase Firestore (no local database) |
| **Authentication** | Firebase Authentication |
| **Encryption** | AES-256/GCM + PBKDF2 (65,536 iterations) + per-user salt |
| **Biometric** | AndroidX Biometric |
| **Navigation** | Jetpack Navigation Compose |

## 🧬 Architecture

### Project Structure

```
app/src/main/java/com/fachrirasyiq/kryptapass/
├── di/              # AppContainer — manual dependency injection
├── model/           # Data classes (AccountModel, AuditModel)
├── navigation/      # NavGraph (Login → Home → AddEditAccount → Profile)
├── repository/      # PasswordRepository — Firestore-only, real-time Flow
├── screen/          # UI screens (Login, Home, AddEditAccount, Profile)
├── security/        # CryptoManager, SessionManager, PasswordChecker, BiometricPromptManager
├── theme/           # Material 3 theme, colors, typography
└── viewmodel/       # AuthViewModel, HomeViewModel, PasswordDetailViewModel, ProfileViewModel
```

### Data Flow

```
User Intent → Screen → ViewModel.handleIntent() → StateFlow<UiState> → collectAsState()
```

### Encryption Flow

```
1. Register   → CryptoManager.generateSalt()  → 16-byte random salt
               → Store salt in Firestore (users/{uid}/profile)
               → Derive AES-256 key via PBKDF2

2. Login      → Fetch salt from Firestore
               → Re-derive encryption key
               → Store key in SessionManager (in-memory only — never persisted)

3. Read/Write → Encrypt/decrypt using key from SessionManager
               → All operations are in-memory; key is destroyed on auto-lock
```

### Firestore Data Model

```
users/
  {uid}/
    salt: "base64..."               ← encryption profile (document root)
    accounts/
      {accountId}/                  ← encrypted account data
        platform: "encrypted..."
        email: "encrypted..."
        password: "encrypted..."
        category: "encrypted..."
    audit/
      {logId}/                      ← activity log
        action: "LOGIN|CREATE|UPDATE|DELETE"
        timestamp: FirestoreTimestamp
```

## 🚀 Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- A Firebase project with **Authentication** and **Firestore** enabled
- Android device/emulator running API 24+

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/IkanParii/KryptaPass.git

# 2. Open in Android Studio
# File → Open → select KryptaPass/

# 3. Add Firebase config
# Download google-services.json from Firebase Console → Project settings
# Place it in: app/google-services.json

# 4. Configure Firestore Security Rules
# Go to Firebase Console → Firestore → Rules → paste:
```

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

```bash
# 5. Build & Run
./gradlew assembleDebug        # Build debug APK
./gradlew installDebug         # Install to connected device
```

## 📱 Minimum Requirements

| Requirement | Value |
|-------------|-------|
| **Android OS** | 7.0 (API 24) or higher |
| **Target SDK** | 36 |
| **Internet** | Required for Firebase Auth & Firestore |
| **Biometric** | Optional (fingerprint/face unlock for password details) |

## 🧪 Running Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## ✉️ Contact

**Fachri Rasyiq** — [@IkanParii](https://github.com/IkanParii)

Project Link: [https://github.com/IkanParii/KryptaPass](https://github.com/IkanParii/KryptaPass)

---

<p align="center">
  Made with ❤️ in Indonesia
</p>
