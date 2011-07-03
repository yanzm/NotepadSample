/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Copyright (C) 2011 Yuki Anzai, uPhyca Inc.
 *      http://www.uphyca.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uphyca.android.app.honeypad;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NotesProvider extends ContentProvider {

	public static final String AUTHORITY = "com.uphyca.android.app.honeypad.notesprovider";

	private static final String NOTE_MIME_TYPE = "vnd.android.cursor.item/vnd.honeypad.note";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/notes");

	// The underlying database
//	private SQLiteDatabase notesDB;
	private SQLiteDatabase mDb;

	// Create the constants used to differentiate between the different URI
	// requests.
	private static final int ALL_NOTES = 1;
	private static final int NOTE_ID = 2;

	private static final UriMatcher uriMatcher;

	// Allocate the UriMatcher object, where a URI ending in 'notes' will
	// correspond to a request for all notes, and 'notes' with a trailing
	// '/[rowID]' will represent a single note row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "notes", ALL_NOTES);
		uriMatcher.addURI(AUTHORITY, "notes/#", NOTE_ID);
	}

	@Override
	public boolean onCreate() {
		NotesDatabaseHelper helper = new NotesDatabaseHelper(getContext());
		mDb = helper.getWritableDatabase();
		return mDb != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DATABASE_TABLE);

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case NOTE_ID:
			qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			break;
		}

		// Apply the query to the underlying database.
		Cursor c = qb.query(mDb, projection, selection, selectionArgs,
				null, null, sort);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Insert the new row, will return the row number if
		// successful.
		long rowID = mDb.insert(DATABASE_TABLE, "note", initialValues);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;

		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			count = mDb.delete(DATABASE_TABLE, where, whereArgs);
			break;

		case NOTE_ID:
			String segment = uri.getPathSegments().get(1);
			StringBuilder whereClause = new StringBuilder(KEY_ID).append("=")
					.append(segment);
			if (!TextUtils.isEmpty(where)) {
				whereClause.append(" AND (").append(where).append(")");
			}

			count = mDb.delete(DATABASE_TABLE, whereClause.toString(),
					whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			count = mDb.update(DATABASE_TABLE, values, where, whereArgs);
			break;

		case NOTE_ID:
			String segment = uri.getPathSegments().get(1);
			StringBuilder whereClause = new StringBuilder(KEY_ID).append("=")
					.append(segment);
			if (!TextUtils.isEmpty(where)) {
				whereClause.append(" AND (").append(where).append(")");
			}
			count = mDb.update(DATABASE_TABLE, values,
					whereClause.toString(), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALL_NOTES:
			return "vnd.android.cursor.dir/vnd.honeypad.notes";
		case NOTE_ID:
			return NOTE_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// column names
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";

	// column indexes
	public static final int ID_COLUMN = 0;
	public static final int TITLE_COLUMN = 1;
	public static final int BODY_COLUMN = 2;

	private static final String TAG = "NotesDbAdapter";

	private static final String DATABASE_NAME = "honeypad.db";
	private static final String DATABASE_TABLE = "notes";
	private static final int DATABASE_VERSION = 1;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = String
			.format("create table %s (%s integer primary key autoincrement, %s text not null, %s text not null);",
					DATABASE_TABLE, KEY_ID, KEY_TITLE, KEY_BODY);

	private static class NotesDatabaseHelper extends SQLiteOpenHelper {

		NotesDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the note
	 * @param body
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createNote(String title, String body) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_BODY, body);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes() {

		return mDb.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_BODY }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNote(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_BODY }, KEY_ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param title
	 *            value to set note title to
	 * @param body
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateNote(long rowId, String title, String body) {
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_BODY, body);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + rowId, null) > 0;
	}
}
