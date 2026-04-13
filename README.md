# @serifcicek92/expo-background-service

A high-performance, battery-efficient Expo Native Module for Android that provides a **"Persistent" Foreground Service** to track steps using the hardware-based `TYPE_STEP_COUNTER` sensor, even when the app is completely closed.

## 🚀 Key Features

* **Persistent Foreground Service:** Marked as `START_STICKY`, ensuring the service survives even if the app is swiped away or the system is under memory pressure.
* **Hardware-Based Tracking:** Utilizes the Android `TYPE_STEP_COUNTER` sensor for maximum accuracy and minimal battery consumption compared to accelerometer-based solutions.
* **Dynamic Notifications:** Update notification titles and bodies dynamically from the JavaScript layer.
* **Auto-Prefixing:** Automatically prefixes the step count to your notification body for a seamless user experience.
* **Headless JS Integration:** Wakes up the JavaScript engine at regular intervals (periodic tasks) to sync data with local databases (SQLite) or remote servers (Supabase) while the app is in the background.

## 📦 Installation

To install the package in your Expo project:

```bash
npx expo install [https://github.com/serifcicek92/expo-background-service.git](https://github.com/serifcicek92/expo-background-service.git)
```

⚙️ Android Configuration
To ensure background functionality, add the following permissions and service declarations to your AndroidManifest.xml (or via Expo config plugins):


1. Permissions

   ```xml
     <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_HEALTH" />
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
   

2. Service Declaration

   ```xml
     <service 
    android:name="com.serifcicek.expobackgroundservice.StepCounterService" 
    android:foregroundServiceType="health" 
    android:exported="false" />
    
    <service 
        android:name="com.serifcicek.expobackgroundservice.ExpoBackgroundServiceHeadlessTaskService" 
        android:exported="false" />

🛠️ API Usage
1. Starting and Stopping the Service
    ```typescript
        import * as BackgroundService from '@serifcicek92/expo-background-service';

      // Start Tracking
      // Note: The current step count is automatically prefixed to the body text.
      const startTracking = async () => {
        await BackgroundService.startBackgroundService(
          "StepMap Live",          // Notification Title
          "steps walked so far!",  // Notification Body (Suffix)
          1250                     // Initial Base Steps (from your DB)
        );
      };
      
      // Stop Tracking
      const stopTracking = async () => {
        await BackgroundService.stopBackgroundService();
      };
2. Live Event Listening (Foreground)
Use this to update your UI in real-time while the app is open:
    ```typescript
        import { useEffect } from 'react';
        import { addStepListener } from '@serifcicek92/expo-background-service';
        
        useEffect(() => {
          const subscription = addStepListener((data) => {
            console.log("Raw Sensor Data:", data.steps);
          });
        
          return () => subscription.remove();
        }, []);
  
      3. Background Sync (Headless JS)
      Register the background task in your App.tsx or index.js to handle data persistence when the app is closed:
          ```typescript
              import { AppRegistry } from 'react-native';
      import { Database } from './src/services/Database';
      
      const StepMapBackgroundHandler = async (taskData: any) => {
        if (taskData && taskData.steps !== undefined) {
          // Process raw sensor data through your Delta Engine
          await Database.processNativeSteps(taskData.steps);
        }
      };
      
      // Must match the task name defined in the Native Kotlin code
      AppRegistry.registerHeadlessTask('MyBackgroundStepTask', () => StepMapBackgroundHandler);


  🏗️ Architecture: The Hybrid Delta Logic
This module is designed using a Hybrid Delta Architecture:

Native (Kotlin) Layer: Handles high-frequency sensor events and immediate notification updates to ensure the UI never lags and the service remains persistent.

JavaScript (React Native) Layer: Acts as the "Brain," receiving raw sensor data and calculating the difference (Delta) between readings to handle daily resets and database persistence.

This approach ensures that steps are never lost during device reboots or service restarts, providing a robust history for Leaderboards and Activity Tracking.

👨‍💻 Author
Serif Cicek - Software Developer




Background Service

# API documentation

- [Documentation for the latest stable release](https://docs.expo.dev/versions/latest/sdk/@serifcicek92/background-service/)
- [Documentation for the main branch](https://docs.expo.dev/versions/unversioned/sdk/@serifcicek92/background-service/)

# Installation in managed Expo projects

For [managed](https://docs.expo.dev/archive/managed-vs-bare/) Expo projects, please follow the installation instructions in the [API documentation for the latest stable release](#api-documentation). If you follow the link and there is no documentation available then this library is not yet usable within managed projects &mdash; it is likely to be included in an upcoming Expo SDK release.

# Installation in bare React Native projects

For bare React Native projects, you must ensure that you have [installed and configured the `expo` package](https://docs.expo.dev/bare/installing-expo-modules/) before continuing.

### Add the package to your npm dependencies

```
npm install @serifcicek92/expo-background-service
```

### Configure for Android




### Configure for iOS

Run `npx pod-install` after installing the npm package.

# Contributing

Contributions are very welcome! Please refer to guidelines described in the [contributing guide]( https://github.com/expo/expo#contributing).
