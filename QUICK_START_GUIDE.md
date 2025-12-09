# Quick Start Guide - Camera Feature Setup

## ✅ Step 1: Deploy Firebase Storage Rules

You need to deploy the storage rules so images can be uploaded:

### Option A: Using Firebase CLI (Recommended)
```bash
# Make sure you're in the project directory
cd /Users/abdifatahabdi/Desktop/BadgerOnSale3

# Deploy storage rules
firebase deploy --only storage
```

### Option B: Using Firebase Console (Manual)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Storage** → **Rules** tab
4. Copy the contents from `storage.rules` file
5. Paste into the rules editor
6. Click **Publish**

## ✅ Step 2: Build and Run the App

1. **Open Android Studio**
2. **Sync Gradle** (if prompted)
3. **Build the project**: `Build` → `Rebuild Project`
4. **Run on device/emulator**: Click the green play button

## ✅ Step 3: Test the Camera Feature

### Test 1: Create Listing with Image
1. Sign in to the app
2. Go to **Create Listing** screen
3. Click **"Add photo"**
4. Choose:
   - **"Take Photo"** - Uses camera
   - **"Choose from Gallery"** - Selects from device photos
5. Fill in listing details (title, price, category, description)
6. Click **"Create Listing"**
7. ✅ Image should upload and listing should be created

### Test 2: Update Profile Picture
1. Go to **Profile** → **Edit Profile**
2. Click **"CHANGE PHOTO"**
3. Choose camera or gallery
4. Wait for upload (you'll see a loading indicator)
5. ✅ Profile picture should update

## ✅ Step 4: Verify in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Storage**
4. You should see:
   - `listings/` folder (with uploaded listing images)
   - `profiles/` folder (with uploaded profile pictures)

## 🔧 Troubleshooting

### If upload fails:
- Check that you're signed in
- Verify storage rules are deployed
- Check internet connection
- Look at error messages in the app

### If camera doesn't work:
- Grant camera permission when prompted
- Grant storage permission when prompted
- Check device has a camera (if using emulator, make sure it's configured)

### If images don't display:
- Check Firebase Storage rules allow read access
- Verify images uploaded successfully in Firebase Console
- Check internet connection

## 📱 Permissions

The app will automatically request permissions when needed:
- **Camera permission** - When you choose "Take Photo"
- **Storage permission** - When you choose "Choose from Gallery"

Just click **"Allow"** when prompted!

## ✅ That's It!

Once you've deployed the storage rules and tested the features, everything should be working! Images will be stored in Firebase Storage and accessible throughout the app.

