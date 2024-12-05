# Code Structure

## Activity Setup:  
 * The onCreate method sets up the toolbar, initializes the Room database, and sets up the ListView with an adapter.
 * The onResume method reloads the notes to ensure the list is up-to-date.

## Menu Handling:  
 * The onCreateOptionsMenu method inflates the menu from a resource file.
 * The onOptionsItemSelected method handles menu item selections for adding, editing, and deleting notes.

## Database Interaction:  
 * The Room database is initialized with allowMainThreadQueries(), which is generally not recommended for production due to potential UI thread blocking. Consider using asynchronous queries with LiveData or Coroutines.

## ListView Interaction:  
 * The onItemClickListener updates the selected note ID and refreshes the adapter.

## Delete Confirmation:  
 * The showDeleteDialog method shows a confirmation dialog before deleting a note.
