# Note Management Technical Documentation

## Overview
This documentation provides an overview of the Note Management functionality in the application. It covers the main activities, database structure, and key functions used to manage notes.

## Activities

### MainActivity
**Description**: The entry point of the application. Displays the main screen with a login button and an animated icon.

**Key Components**:
- **Button**: `login` - Initiates the login process and navigates to `NoteListActivity`.
- **ImageView**: `icon` - Displays an animated icon.

### NoteListActivity
**Description**: Displays a list of notes. Allows the user to view, edit, and delete notes.

**Key Components**:
- **ListView**: `listView` - Displays the list of notes.
- **Menu**: Provides options to add, edit, and delete notes.

**Key Functions**:
- **onCreate**: Initializes the activity, sets up the toolbar, and loads notes from the database.
- **onResume**: Reloads notes when the activity is resumed.
- **showDeleteDialog**: Displays a dialog to confirm note deletion.

### NoteEditActivity
**Description**: Allows the user to edit a note's title, message, and image, and to save the note.

**Key Components**:
- **EditText**: `noteEditTitle`, `noteEditMessage` - Input fields for the note's title and message.
- **Button**: `buttonSave`, `buttonCamera`, `buttonGallery` - Buttons to save the note, capture an image, and pick an image from the gallery.
- **ImageView**: `imagePreview` - Displays the selected image.
- **TextView**: `latitudeTextView`, `longitudeTextView` - Displays the note's location.
- **MapView**: `map` - Displays the note's location on a map.

**Key Functions**:
- **onCreate**: Initializes the activity, sets up the toolbar, and loads the note details if editing an existing note.
- **saveNote**: Saves the note to the database.
- **dispatchTakePictureIntent**: Captures an image using the camera.
- **dispatchPickPictureIntent**: Picks an image from the gallery.
- **getLastLocation**: Retrieves the last known location of the device and updates the UI and database.

## Database

### NotesDatabase
**Description**: Defines the database configuration and serves as the app's main access point to the persisted data.

**Entities**:
- **Note**: Represents a note entity in the database.

### NoteDao
**Description**: Data Access Object (DAO) for accessing the Note database.

**Key Functions**:
- **getAll**: Retrieves all notes from the database.
- **loadAllByIds**: Retrieves notes by their IDs.
- **insertAll**: Inserts new notes into the database.
- **update**: Updates existing notes in the database.
- **delete**: Deletes notes from the database.

## Data Model

### Note
**Description**: Data class representing a Note entity in the database.

**Fields**:
- **title**: The title of the note.
- **message**: The message content of the note.
- **image**: The file path of the image associated with the note.
- **latitude**: The latitude of the note's location.
- **longitude**: The longitude of the note's location.
- **id**: The unique identifier of the note.

## Permissions
The application requests the following permissions:
- **Camera**: To capture images.
- **Read External Storage**: To pick images from the gallery.
- **Access Fine Location**: To retrieve the device's location.

## String Resources
String resources are used for better localization support. Key strings include:
- **latitude**: "Latitude: %1$s"
- **longitude**: "Longitude: %1$s"
- **note_saved**: "Note saved"
- **failed_to_get_location**: "Failed to get location"
- **delete_message**: "Are you sure you want to delete this note?"
- **yes**: "Yes"
- **no**: "No"

## Conclusion
This documentation provides an overview of the Note Management functionality, including the main activities, database structure, key functions, and permissions. The application allows users to create, edit, view, and delete notes, with support for capturing and picking images, and displaying the note's location on a map.