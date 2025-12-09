# Deploy Storage Rules - Quick Instructions

I've set up everything for you! Here's how to deploy:

## Option 1: Run the Script (Easiest)

Just run this command in your terminal:

```bash
cd /Users/abdifatahabdi/Desktop/BadgerOnSale3
./deploy-storage-rules.sh
```

This will:
1. Open a browser for you to log in to Firebase
2. Deploy the storage rules automatically

## Option 2: Manual Steps

If you prefer to do it manually:

### Step 1: Login to Firebase
```bash
cd /Users/abdifatahabdi/Desktop/BadgerOnSale3
firebase login
```
A browser window will open - log in with your Google account that has access to the `badgeronesale` Firebase project.

### Step 2: Deploy Storage Rules
```bash
firebase deploy --only storage
```

## Option 3: Use Firebase Console (No CLI needed)

1. Go to https://console.firebase.google.com/
2. Select project: **badgeronesale**
3. Go to **Storage** → **Rules** tab
4. Copy the contents from `storage.rules` file
5. Paste into the rules editor
6. Click **Publish**

## ✅ After Deployment

Once deployed, your camera feature will work! You can:
- Take photos with camera
- Select images from gallery
- Images will be stored in Firebase Storage
- Images will display throughout the app

## 🎉 That's It!

The easiest way is to just run:
```bash
./deploy-storage-rules.sh
```

