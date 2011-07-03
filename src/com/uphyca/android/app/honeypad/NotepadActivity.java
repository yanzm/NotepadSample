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

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.uphyca.android.app.honeypad.NoteListFragment.NoteListEventsCallback;

public class NotepadActivity extends Activity implements NoteListEventsCallback {

	private static final int MENU_ADD_ID = Menu.FIRST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);

		final ActionBar actionBar = getActionBar(); 
		BitmapDrawable background = (BitmapDrawable)getResources().getDrawable(R.drawable.bg2);
		background.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT); 
		actionBar.setBackgroundDrawable(background);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem add = menu.add(0, MENU_ADD_ID, 0, R.string.menu_add);
		add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		add.setIcon(R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_ID:
			showNote(null);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void showNote(final Uri noteUri) {
		// check if the NoteEditFragment has been added
		FragmentManager fm = getFragmentManager();
		NoteEditFragment edit = (NoteEditFragment) fm.findFragmentByTag("Edit");
		if (edit == null) {
			// add the NoteEditFragment to the container
			FragmentTransaction ft = fm.beginTransaction();
			edit = new NoteEditFragment();
			ft.add(R.id.note_detail_container, edit, "Edit");
			ft.commit();
		} else if (noteUri == null) {
			edit.clear();
		}

		if (noteUri != null) {
			edit.loadNote(noteUri);
		}
		else {
			NoteListFragment list = (NoteListFragment) fm.findFragmentById(R.id.list);
			if(list != null) {
				list.getListView().clearChoices();
			}
		}
	}

	@Override
	public void onNoteSelected(Uri noteUri) {
		showNote(noteUri);
	}

	public void onNoteDeleted() {
		// remove the NoteEditFragment after a deletion
		FragmentManager fm = getFragmentManager();
		NoteEditFragment edit = (NoteEditFragment) fm.findFragmentByTag("Edit");
		if (edit != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(edit);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}
	}

}
