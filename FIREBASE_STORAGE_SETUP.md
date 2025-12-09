# Firebase Storage Setup Guide

## ✅ What's Been Implemented

### 1. **Image Upload to Firebase Storage**
- ✅ Listing images are uploaded to `listings/{userId}/{filename}`
- ✅ Profile pictures are uploaded to `profiles/{userId}/{filename}`
- ✅ All images are stored in Firebase Storage with unique filenames
- ✅ Download URLs are retrieved and saved to Firestore

### 2. **Image Selection Options**
- ✅ **Camera**: Take photos directly with device camera
- ✅ **Gallery**: Select images from device storage/photos
- ✅ Both options work for:
  - Creating listings (CreateListingScreen)
  - Updating profile pictures (EditProfileScreen)

### 3. **Upload Flow**

#### For Listings:
1. User selects image (camera or gallery)
2. Image is stored locally temporarily
3. When user clicks "Create Listing":
   - Image is uploaded to Firebase Storage
   - Download URL is retrieved
   - Listing is created in Firestore with the imageUrl

#### For Profile Pictures:
1. User selects image (camera or gallery)
2. Image is immediately uploaded to Firebase Storage
3. Download URL is retrieved and saved to user profile
4. Profile picture is updated in real-time

## 📋 Firebase Storage Rules

The storage rules are configured in `storage.rules`:

- **Listing Images**: 
  - Max size: 5MB
  - Location: `listings/{userId}/{imageName}`
  - Users can only upload to their own folder
  
- **Profile Pictures**:
  - Max size: 2MB
  - Location: `profiles/{userId}/{imageName}`
  - Users can only upload their own profile picture

## 🚀 Deploying Storage Rules

To deploy the storage rules to Firebase:

1. **Using Firebase CLI:**
   ```bash
   firebase deploy --only storage
   ```

2. **Using Firebase Console:**
   - Go to Firebase Console → Storage → Rules
   - Copy the contents of `storage.rules`
   - Paste into the rules editor
   - Click "Publish"

## ✅ Verification Checklist

- [x] Firebase Storage is enabled in Firebase Console
- [x] Storage rules are configured (`storage.rules`)
- [x] FileProvider is set up for camera images
- [x] Permissions are requested at runtime
- [x] Images upload to Firebase Storage
- [x] Download URLs are saved to Firestore
- [x] Images display correctly using Coil library

## 🔍 Testing the Upload

1. **Test Listing Image Upload:**
   - Go to Create Listing screen
   - Click "Add photo"
   - Choose "Take Photo" or "Choose from Gallery"
   - Fill in listing details
   - Click "Create Listing"
   - Check Firebase Console → Storage → listings folder

2. **Test Profile Picture Upload:**
   - Go to Edit Profile screen
   - Click "CHANGE PHOTO"
   - Choose "Take Photo" or "Choose from Gallery"
   - Wait for upload to complete (loading indicator)
   - Check Firebase Console → Storage → profiles folder

## 📝 Notes

- Images are automatically compressed by Firebase Storage
- Unique filenames prevent conflicts (timestamp-based)
- Images are accessible to all authenticated users (read permission)
- Only the owner can upload/delete their own images (write permission)

