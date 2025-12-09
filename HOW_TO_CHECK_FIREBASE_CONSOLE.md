# How to Check Firebase Console

## Step 1: Open Firebase Console

1. **Go to Firebase Console:**
   - Open your web browser
   - Visit: **https://console.firebase.google.com/**
   - Sign in with your Google account (same account you used for Firebase)

2. **Select Your Project:**
   - You should see a list of projects
   - Click on **"badgeronesale"** (or "BadgerOnesale")

---

## Step 2: Check Firestore Database

### Navigate to Firestore:
1. In the left sidebar, click on **"Firestore Database"**
2. You should see your collections:
   - `listings` - Your product listings
   - `users` - User profiles
   - `images` - Image data (base64)
   - `favorites` - User favorites
   - `messages` - Chat messages

### Check Listings Collection:
1. **Click on `listings` collection**
2. You'll see all your listings as documents
3. **Click on a specific listing** (the one you created with an image)

### What to Look For:
In the listing document, you should see fields like:
- `title` - The listing title
- `price` - The price
- `description` - Item description
- **`imageUrl`** - **This is what we're checking!**

### Check the imageUrl Field:
- **If `imageUrl` exists:**
  - It should start with: `data:image/jpeg;base64,`
  - Followed by a long string of characters
  - This means the image was uploaded successfully ✅

- **If `imageUrl` is missing or empty:**
  - The image upload failed ❌
  - You'll need to recreate the listing with an image

---

## Step 3: Check Images Collection (Optional)

1. **Click on `images` collection** in Firestore
2. You should see documents with names like:
   - `listing_{listingId}_{timestamp}`
   - `profile_{userId}_{timestamp}`
3. **Click on an image document** to see:
   - `userId` - Who uploaded it
   - `base64Data` - The actual image data (very long string)
   - `contentType` - Should be "image/jpeg"
   - `createdAt` - When it was uploaded

---

## Step 4: Verify Image Upload

### If imageUrl is in the listing:
✅ **Image upload worked!**
- The image should display in your app
- If it doesn't display, it's a display issue (which we just fixed)

### If imageUrl is missing:
❌ **Image upload failed**
- Check the app logs (Logcat) for error messages
- Try creating a new listing with an image
- Make sure the image is under 700KB

---

## Quick Visual Guide

```
Firebase Console
├── Project: badgeronesale
│   ├── Firestore Database
│   │   ├── listings (collection)
│   │   │   └── [Your Listing Document]
│   │   │       ├── title: "Your Listing Title"
│   │   │       ├── price: "$50"
│   │   │       ├── description: "..."
│   │   │       └── imageUrl: "data:image/jpeg;base64,ABC123..." ✅
│   │   │
│   │   └── images (collection)
│   │       └── listing_{id}_{timestamp}
│   │           ├── userId: "user123"
│   │           ├── base64Data: "iVBORw0KGgo..."
│   │           └── contentType: "image/jpeg"
```

---

## Troubleshooting

### Can't find Firestore Database?
- Make sure you're in the correct project
- Firestore should be enabled (it should be already)

### Can't see listings collection?
- Create a listing in the app first
- Refresh the Firebase Console page
- Check if you're looking at the right project

### imageUrl field is empty?
- The image upload might have failed
- Check image size (must be under 700KB)
- Check if you're signed in to Firebase Auth in the app
- Try uploading again

---

## Alternative: Check via Terminal

You can also check Firestore data using Firebase CLI:

```bash
# Install Firebase CLI tools (if not already installed)
npm install -g firebase-tools

# Login
firebase login

# View Firestore data (read-only)
firebase firestore:get listings
```

But the web console is easier to use!

---

## Summary

**To check if images are stored:**
1. Go to https://console.firebase.google.com/
2. Select project "badgeronesale"
3. Click "Firestore Database"
4. Click "listings" collection
5. Click on a listing document
6. Look for `imageUrl` field
7. If it exists and starts with `data:image/jpeg;base64,` → ✅ Success!

