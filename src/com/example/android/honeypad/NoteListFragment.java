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

package com.example.android.honeypad;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class NoteListFragment extends ListFragment {
	
	public interface NoteListEventsCallback {
		public void onNoteSelected(Uri noteUri);

		public void onNoteDeleted();
	}

	private static final int DELETE_ID = Menu.FIRST + 1;

	private Cursor mNotesCursor;

	private NoteListEventsCallback mContainerCallback;

	public NoteListFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		setEmptyText(getActivity().getString(R.string.no_notes));
		
		fillData();
		registerForContextMenu(getListView());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			// check that the containing activity implements our callback
			mContainerCallback = (NoteListEventsCallback) activity;
		} catch (ClassCastException e) {
			activity.finish();
			throw new ClassCastException(activity.toString()
					+ " must implement NoteSelectedCallback");
		}
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		CursorLoader loader = new CursorLoader(getActivity(), NotesProvider.CONTENT_URI, null, null, null, null);
		mNotesCursor = loader.loadInBackground();

		// startManagingCursor() is deprecated, use CursorLoader instead
//		mNotesCursor = getActivity().getContentResolver().query(
//				NotesProvider.CONTENT_URI, null, null, null, null);
//		getActivity().startManagingCursor(mNotesCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { NotesProvider.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { android.R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = new SimpleCursorAdapter(getActivity(),
				R.layout.list_item, mNotesCursor, from, to);
		setListAdapter(notes);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			getActivity().getContentResolver().delete(
					ContentUris.withAppendedId(NotesProvider.CONTENT_URI,
							info.id), null, null);
			fillData();
			mContainerCallback.onNoteDeleted();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri noteUri = ContentUris.withAppendedId(NotesProvider.CONTENT_URI, id);
		mContainerCallback.onNoteSelected(noteUri);
		((CheckedTextView)v).setChecked(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			fillData();
		}
	}
}
