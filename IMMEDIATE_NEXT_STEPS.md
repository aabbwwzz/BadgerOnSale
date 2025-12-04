# Immediate Next Steps - Step by Step

## Step 1: Sync Gradle Files (2 minutes)

1. **Open Android Studio**
2. **Sync Gradle:**
   - Look for a notification at the top that says "Gradle files have changed since last project sync"
   - Click **"Sync Now"** button
   - OR go to: `File` → `Sync Project with Gradle Files`
3. **Wait for sync to complete** (you'll see progress in the bottom status bar)

---

## Step 2: Build the Project (3 minutes)

1. **Clean the project:**
   - Go to: `Build` → `Clean Project`
   - Wait for it to finish

2. **Rebuild the project:**
   - Go to: `Build` → `Rebuild Project`
   - Wait for build to complete
   - Check the "Build" tab at the bottom for any errors

3. **If you see errors:**
   - Read the error message
   - Common fix: Click "Sync Now" again if Gradle sync didn't complete

---

## Step 3: Deploy Security Rules to Firebase (10 minutes) ⚠️ **IMPORTANT**

### Part A: Deploy Firestore Security Rules

1. **Open Firebase Console:**
   - Go to: https://console.firebase.google.com/
   - Sign in with your Google account
   - Select your project: **"badgeronesale"**

2. **Navigate to Firestore:**
   - Click **"Firestore Database"** in the left menu
   - Click on the **"Rules"** tab at the top

3. **Copy your security rules:**
   - Open the file `firestore.rules` in your project folder
   - Select all the text (Ctrl+A / Cmd+A)
   - Copy it (Ctrl+C / Cmd+C)

4. **Paste in Firebase Console:**
   - Delete everything in the Firebase Console rules editor
   - Paste your rules (Ctrl+V / Cmd+V)
   - Click **"Publish"** button

### Part B: Deploy Storage Security Rules

1. **Navigate to Storage:**
   - In Firebase Console, click **"Storage"** in the left menu
   - Click on the **"Rules"** tab at the top

2. **Copy your storage rules:**
   - Open the file `storage.rules` in your project folder
   - Select all the text and copy it

3. **Paste and publish:**
   - Delete everything in the Firebase Console rules editor
   - Paste your rules
   - Click **"Publish"** button

---

## Step 4: Test the App (15 minutes)

1. **Run the app:**
   - Click the green **Play** button (▶️) in Android Studio
   - OR press `Shift + F10` (Windows/Linux) or `Ctrl + R` (Mac)
   - Select an emulator or connected device
   - Wait for app to install and launch

2. **Test these features:**

   **a) Create Account:**
   - Click "Create Account"
   - Enter:
     - Name: Test User
     - Email: test@wisc.edu (or any @wisc.edu email)
     - Phone: (608) 555-1234
     - Password: test123456
   - Click "Create Account"
   - Should navigate to Home screen

   **b) Sign Out and Sign In:**
   - Go to Menu → Log Out
   - Sign in with the account you just created
   - Should work correctly

   **c) Browse Listings:**
   - Home screen should show listings (if any exist in Firestore)
   - If empty, that's okay - you'll create one next

   **d) Create a Listing:**
   - Go to Menu → Create Listing
   - Enter:
     - Title: Test Item
     - Price: $50
     - Item Details: This is a test listing
   - Click "Create Listing"
   - Should navigate back to Home

   **e) View Listing Details:**
   - Click on any listing
   - Should show details page

   **f) Add to Favorites:**
   - On listing details page, click the heart icon
   - Go to Menu → Favorites
   - Should see the item you favorited

   **g) View Profile:**
   - Go to Menu → View Profile
   - Should show your profile

   **h) Edit Profile:**
   - Go to Menu → Edit Profile
   - Change your name
   - Click "SAVE"
   - Should update successfully

---

## Step 5: Check for Errors

1. **Open Logcat:**
   - In Android Studio, click the **"Logcat"** tab at the bottom
   - Look for any red error messages

2. **Common issues and fixes:**

   **"FirebaseApp not initialized"**
   - ✅ Already fixed - Firebase is initialized in MainActivity

   **"Permission denied" or "Missing or insufficient permissions"**
   - ⚠️ Security rules not deployed - Go back to Step 3

   **"Package name mismatch"**
   - ✅ Already fixed - google-services.json updated

   **"Network error" or "Connection failed"**
   - Check internet connection
   - Verify Firebase project is active

---

## Step 6: If Everything Works ✅

1. **Take screenshots** of your app screens for documentation
2. **Continue with remaining tasks:**
   - Test on real device (if available)
   - Record demo video
   - Complete final write-up

---

## Step 7: If You Have Errors ❌

1. **Read the error message** in Logcat
2. **Check these common fixes:**
   - Did you sync Gradle? (Step 1)
   - Did you deploy security rules? (Step 3)
   - Is your internet connected?
   - Is Firebase project active?

3. **If still stuck:**
   - Copy the error message
   - Check the error in the Build output tab
   - Try cleaning and rebuilding (Step 2)

---

## Quick Checklist

- [ ] Step 1: Sync Gradle files
- [ ] Step 2: Build project (clean + rebuild)
- [ ] Step 3: Deploy Firestore security rules
- [ ] Step 3: Deploy Storage security rules
- [ ] Step 4: Run app and test features
- [ ] Step 5: Check Logcat for errors
- [ ] Step 6: Take screenshots if working
- [ ] Step 7: Fix any errors found

---

## Estimated Time

- **Step 1:** 2 minutes
- **Step 2:** 3 minutes
- **Step 3:** 10 minutes
- **Step 4:** 15 minutes
- **Total:** ~30 minutes

---

## What You Should See

✅ **Success indicators:**
- App builds without errors
- App launches on emulator/device
- Can create account and sign in
- Can create listings
- Can view listings
- No red errors in Logcat

❌ **Problem indicators:**
- Build fails
- App crashes on startup
- "Permission denied" errors
- Can't connect to Firebase

---

**Start with Step 1 and work through each step in order!**

