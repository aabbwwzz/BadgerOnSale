# Fix Build Error - Step by Step

## Error: "Could not find com.google.firebase:firebase-*-ktx"

This error means Gradle can't download Firebase dependencies. Follow these steps:

---

## Step 1: Invalidate Caches and Restart (2 minutes)

1. **In Android Studio:**
   - Go to: `File` → `Invalidate Caches...`
   - Check all boxes:
     - ✅ Clear file system cache and Local History
     - ✅ Clear downloaded shared indexes
   - Click **"Invalidate and Restart"**
   - Wait for Android Studio to restart

---

## Step 2: Clean Build (1 minute)

1. **After Android Studio restarts:**
   - Go to: `Build` → `Clean Project`
   - Wait for it to finish

---

## Step 3: Sync Gradle Again (2 minutes)

1. **Sync Gradle:**
   - Go to: `File` → `Sync Project with Gradle Files`
   - OR click "Sync Now" if prompted
   - Wait for sync to complete

---

## Step 4: Check Internet Connection

1. **Make sure you're connected to the internet**
2. **Try opening in browser:**
   - https://maven.google.com
   - https://repo1.maven.org/maven2
   - If these don't load, you have a network issue

---

## Step 5: Try Building Again

1. **Build the project:**
   - Go to: `Build` → `Rebuild Project`
   - Wait for build to complete

---

## Step 6: If Still Failing - Check Gradle Version

1. **Check Gradle version:**
   - Go to: `File` → `Project Structure` → `Project`
   - Make sure Gradle version is 8.0 or higher

2. **If Gradle is old:**
   - Update it in `gradle/wrapper/gradle-wrapper.properties`
   - Change `distributionUrl` to use Gradle 8.0+

---

## Step 7: Manual Dependency Check

If still failing, try this:

1. **Open Terminal in Android Studio:**
   - Go to: `View` → `Tool Windows` → `Terminal`

2. **Run this command:**
   ```bash
   ./gradlew --refresh-dependencies build
   ```

   This will force Gradle to re-download all dependencies.

---

## Alternative: Check if Firebase BoM Version is Correct

The Firebase BoM version might be too new. Try changing it:

1. **Open:** `app/build.gradle.kts`
2. **Find this line:**
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
   ```
3. **Try changing to:**
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
   ```
4. **Sync Gradle again**

---

## Most Common Solution

**90% of the time, this fixes it:**
1. `File` → `Invalidate Caches...` → `Invalidate and Restart`
2. `Build` → `Clean Project`
3. `File` → `Sync Project with Gradle Files`
4. `Build` → `Rebuild Project`

---

## If Nothing Works

1. **Check Android Studio version:**
   - Should be Android Studio Hedgehog or newer
   - Update if needed: `Help` → `Check for Updates`

2. **Check Gradle wrapper:**
   - File: `gradle/wrapper/gradle-wrapper.properties`
   - Should have Gradle 8.0+

3. **Try offline mode:**
   - Go to: `File` → `Settings` → `Build, Execution, Deployment` → `Gradle`
   - Uncheck "Offline work" if checked

---

## Quick Checklist

- [ ] Step 1: Invalidate caches and restart
- [ ] Step 2: Clean project
- [ ] Step 3: Sync Gradle
- [ ] Step 4: Check internet
- [ ] Step 5: Rebuild project
- [ ] Step 6: Check Gradle version
- [ ] Step 7: Try manual refresh

---

**Start with Step 1 - it fixes most issues!**

