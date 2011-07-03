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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NoteEditFragment extends Fragment {

	private EditText mTitleText;
	private EditText mBodyText;
	private Uri mCurrentNote;

	public NoteEditFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.note_edit, container, false);

		mTitleText = (EditText) v.findViewById(R.id.title);
		mBodyText = (EditText) v.findViewById(R.id.body);

		Button saveButton = (Button) v.findViewById(R.id.save);
		saveButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				InputMethodManager inputMethodManager = 
					(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

				saveNote();
			}
		});
		populateFields();
		return v;
	}

	private void populateFields() {
		if (mCurrentNote != null) {
			Cursor c = null;
			try {
				c = getActivity().getContentResolver().query(mCurrentNote,
						null, null, null, null);
				if (c.moveToFirst()) {
					mTitleText.setText(c.getString(NotesProvider.TITLE_COLUMN));
					mBodyText.setText(c.getString(NotesProvider.BODY_COLUMN));
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}
		}
	}
	
	private String buildTitleFromDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf.setTimeZone(TimeZone.getDefault());
		String suffix = sdf.format(Calendar.getInstance().getTime());
		return suffix;
	}

	private void saveNote() {
		String title = mTitleText.getText().toString();
		if(TextUtils.isEmpty(title)) {
			title = getActivity().getString(R.string.new_note_name, buildTitleFromDate());
		}
		
		// save/update the note
		ContentValues values = new ContentValues(2);
		values.put(NotesProvider.KEY_TITLE, title);
		values.put(NotesProvider.KEY_BODY, mBodyText.getText().toString());
		if (mCurrentNote != null) {
			getActivity().getContentResolver().update(mCurrentNote, values,
					null, null);
		} else {
			getActivity().getContentResolver().insert(
					NotesProvider.CONTENT_URI, values);
		}
		Toast.makeText(getActivity(), "Note Saved", Toast.LENGTH_SHORT).show();
	}

	protected void loadNote(Uri noteUri) {
		mCurrentNote = noteUri;
		if (isAdded()) {
			populateFields();
		}
	}

	protected void clear() {
		mTitleText.setText(null);
		mBodyText.setText(null);
		mCurrentNote = null;
	}
}
