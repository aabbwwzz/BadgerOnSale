# Photo Upload Troubleshooting Guide

## ✅ What I Fixed

1. **Added better error messages** - Now shows exact file size and clearer error messages
2. **Added debug logging** - Check Logcat to see what's happening during upload
3. **Improved error handling** - Better exception catching and reporting

## How to Upload a Photo

### Step 1: Create a Listing
1. Open the app
2. Go to **Menu** → **Create Listing**
3. Tap the **"Add photo"** card at the top

### Step 2: Choose Photo Source
You'll see a dialog with two options:
- **Take Photo** - Use camera to take a new photo
- **Choose from Gallery** - Select existing photo from gallery

### Step 3: Select/Take Photo
- If taking photo: Grant camera permission → Take photo
- If choosing from gallery: Grant storage permission → Select photo

### Step 4: Fill in Listing Details
- Title
- Price
- Category
- Description

### Step 5: Create Listing
- Tap **"Create Listing"** button
- Wait for upload (you'll see a loading indicator)
- Photo should upload and listing will be created

## Common Issues & Solutions

### Issue 1: "Image is too large"
**Error Message:** "Image is too large (XXX KB). Maximum size is 700KB."

**Solution:**
- Photos must be under **700KB**
- Try:
  1. Take a new photo (camera usually creates smaller files)
  2. Compress the image before uploading
  3. Use a photo editing app to reduce quality/size

**Why?** Firestore has a 1MB document limit, and base64 encoding increases file size by ~33%.

### Issue 2: "Failed to upload image"
**Error Message:** "Failed to upload image: [error message]"

**Solutions:**
1. **Check internet connection** - Upload requires internet
2. **Check if signed in** - You must be logged in to upload
3. **Check Logcat** - Look for detailed error messages
4. **Try again** - Sometimes retrying works

### Issue 3: "Camera permission is required"
**Solution:**
- Go to **Settings** → **Apps** → **BadgerOnSale** → **Permissions**
- Enable **Camera** permission
- Try again

### Issue 4: "Storage permission is required"
**Solution:**
- Go to **Settings** → **Apps** → **BadgerOnSale** → **Permissions**
- Enable **Storage** or **Photos and Videos** permission
- Try again

### Issue 5: Photo doesn't appear after upload
**Check:**
1. **Firebase Console** - Verify `imageUrl` field exists in the listing
2. **Image size** - Make sure it's under 700KB
3. **Logcat** - Check for Coil loading errors
4. **Rebuild app** - Sometimes a rebuild fixes display issues

## Debugging Steps

### Check Logcat
1. Open **Android Studio**
2. Open **Logcat** tab
3. Filter by: `Uploading image` or `Image upload`
4. Look for error messages

### Check Firebase Console
1. Go to **Firebase Console** → **Firestore Database**
2. Click **listings** collection
3. Click on your listing
4. Check if `imageUrl` field exists
5. The value should start with: `data:image/jpeg;base64,`

### Test Photo Upload
1. **Take a small photo** (camera usually creates ~200-500KB photos)
2. **Fill in listing details**
3. **Create listing**
4. **Check Firebase Console** to verify `imageUrl` was saved
5. **View listing** - Image should appear

## Best Practices

✅ **Do:**
- Take photos with camera (usually smaller file size)
- Keep photos under 700KB
- Check internet connection before uploading
- Wait for upload to complete (loading indicator)

❌ **Don't:**
- Upload very large photos (>700KB)
- Close app during upload
- Upload without internet connection
- Upload without being signed in

## File Size Limits

- **Maximum:** 700KB (original file size)
- **Why:** Firestore document limit is 1MB
- **Base64 overhead:** ~33% size increase
- **Safe limit:** 700KB original = ~930KB base64

## Still Having Issues?

1. **Check Logcat** for detailed error messages
2. **Verify Firebase Console** - Is `imageUrl` being saved?
3. **Try a smaller photo** - Camera photos are usually smaller
4. **Check internet connection**
5. **Make sure you're signed in**

## Summary

The photo upload should work! If you see errors:
- Check the error message (it will tell you what's wrong)
- Check Logcat for details
- Verify file size is under 700KB
- Make sure you're signed in and have internet

The upload process:
1. Select/take photo ✅
2. Upload to Firestore as base64 ✅
3. Save data URL to listing ✅
4. Display in app ✅

