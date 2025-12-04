# BadgerOnSale - Milestone Completion Status

## ✅ Completed Features

### Milestone 1 - COMPLETE ✅
- ✅ Sign in page (with Firebase Authentication)
- ✅ Create an account page (with Firebase Authentication)
- ✅ Home page (loading listings dynamically from Firestore)
- ✅ Listing detail page (with real data from Firestore)
- ✅ User Profile (view) + Edit Profile
- ✅ Menu page (navigation fully wired up)
- ✅ UW-Madison color scheme (red, white, grey) implemented
- ✅ Firebase project connected (google-services.json, Gradle configured)
- ✅ Firestore schema implemented:
  - ✅ users collection
  - ✅ listings collection
  - ✅ messages collection
  - ✅ favorites collection

### Milestone 2 - COMPLETE ✅
- ✅ Create listing page (saves to Firestore)
- ✅ Favorites page (loads from Firestore, add/remove favorites)
- ✅ View profile page (shows user's listings)
- ✅ Listings load dynamically from Firebase
- ✅ Create listing with price, description, and post button
- ✅ Favorites: tap to save/remove favorited items
- ✅ Profile page: displays current user's information and listings
- ✅ Firestore holds listings and user profiles
- ✅ User profile information stored (name, UW email, etc.)

### Milestone 3 - COMPLETE ✅
- ✅ Direct Messages Inbox (loads conversations from Firestore)
- ✅ 1:1 conversation DM Page (real-time messaging)
- ✅ Home Filters (category) on home page
- ✅ Direct messaging set-up complete and integrated
- ✅ Users can tap "Message" icon to open chat with seller
- ✅ Real-time chat working between users (using Firebase)
- ✅ Favorites feature functional
- ✅ Message threads stored per user pair
- ✅ Favorites stored under user profile
- ✅ Listing filtering and search logic in place

### Milestone 4 - IN PROGRESS 🚧

#### Completed:
- ✅ Full app functionality complete
- ✅ All pages finished
- ✅ Loading indicators added
- ✅ Error handling implemented
- ✅ Firestore security rules created
- ✅ Firebase Storage security rules created
- ✅ Firebase Storage integration added

#### Remaining (Dec 1 - Dec 10):
- 🚧 Image upload functionality (Firebase Storage helper created, needs image picker integration)
- 🚧 Dark Mode support
- 🚧 Final testing on real devices
- 🚧 Final demo recording
- 🚧 Final project write-up

## Database Schema Implementation

### ✅ Users Entity
- UserID (Primary Key)
- Name
- Email
- ProfilePicURL
- RegistrationDate

### ✅ Listings Entity
- ListingID (Primary Key)
- UserID (Foreign Key → Users)
- Title, Price, Description
- Category, ImageUrl
- CreatedAt

### ✅ Messages Entity
- MessageID (Primary Key)
- SenderID (Foreign Key → Users)
- ReceiverID (Foreign Key → Users)
- ListingID (Foreign Key → Listings, optional)
- MessageText
- Timestamp

### ✅ Favorites Entity
- FavoriteID (Primary Key)
- UserID (Foreign Key → Users)
- ListingID (Foreign Key → Listings)

## Database Relationships

### ✅ Users ↔ Listings (One-to-Many)
- A User can create multiple Listings
- Listings contains UserID field (foreign key)

### ✅ Users ↔ Favorites ↔ Listings (Many-to-Many)
- A User can favorite multiple Listings
- Each Listing can be favorited by multiple Users
- Favorites entity acts as junction table

### ✅ Users ↔ Messages (Many-to-Many)
- A User can send and receive Messages
- Messages contains SenderID and ReceiverID (foreign keys)

### ✅ Listings ↔ Messages (One-to-Many)
- A Listing can have multiple Messages
- Messages contains ListingID field (foreign key)

## Security Rules

### ✅ Firestore Security Rules
- Users can only edit their own profiles
- Users can only create/edit/delete their own listings
- Users can only manage their own favorites
- Users can only read messages they're part of
- Users can only send messages as themselves

### ✅ Firebase Storage Security Rules
- Listing images: Authenticated users can read, owners can upload
- Profile pictures: Users can only upload their own
- File size limits: 5MB for listings, 2MB for profiles
- Content type validation: Images only

## Out-of-Scope Feature: Firebase Firestore

### Purpose
Using Firebase lets the app work like a real marketplace instead of a single-user app. Students can:
- Upload listings
- Browse listings created by other users
- Save favorites
- Message people in real time

All of this depends on having a cloud database that syncs instantly.

### Technologies Used
- ✅ Firebase Firestore for storing users, listings, favorites, and messages
- ✅ Firebase Storage for item images (helper created, ready for image picker)
- ✅ Firebase Authentication to link data to each user
- ✅ Kotlin coroutines + snapshot listeners for real-time updates

## Next Steps (Dec 1 - Dec 10)

### Dec 1: Polish UI & Firebase Configuration
- [ ] Integrate image picker with Firebase Storage upload
- [ ] Add Dark Mode support
- [ ] Deploy security rules to Firebase console
- [ ] Final UI polish and consistency checks

### Dec 10: Testing & Final Deliverables
- [ ] Test on real Android device
- [ ] Test camera access and storage permissions
- [ ] Test all user workflows end-to-end
- [ ] Record final demo
- [ ] Complete final project write-up

## Files Created/Modified

### New Files:
- `firestore.rules` - Firestore security rules
- `storage.rules` - Firebase Storage security rules
- `FirebaseStorageHelper.kt` - Helper for image uploads
- `repository/ListingRepository.kt` - Listings data management
- `repository/FavoritesRepository.kt` - Favorites data management
- `repository/MessagesRepository.kt` - Messages data management

### Modified Files:
- All screens updated to use Firestore instead of dummy data
- `FirebaseAuthHelper.kt` - Updated to match Users schema
- `MainActivity.kt` - Integrated repositories and real-time updates
- `build.gradle.kts` - Added Firebase Storage dependency

