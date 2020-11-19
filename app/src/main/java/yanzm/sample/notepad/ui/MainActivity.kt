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
package yanzm.sample.notepad.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import yanzm.sample.notepad.R
import yanzm.sample.notepad.data.Note
import yanzm.sample.notepad.ui.edit.EditActivity
import yanzm.sample.notepad.ui.edit.EditFragment
import yanzm.sample.notepad.ui.list.ListFragment
import yanzm.sample.notepad.ui.list.ListFragment.NoteListFragmentListener

class MainActivity : AppCompatActivity(),
    NoteListFragmentListener,
    EditFragment.EditFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onAddNote() {
        startNoteEdit(0)
    }

    override fun onNoteSelected(note: Note) {
        startNoteEdit(note.id)
    }

    private fun startNoteEdit(noteId: Long) {
        val view = findViewById<View?>(R.id.editFragmentContainer)
        if (view == null) {
            startActivity(EditActivity.create(this, noteId))
        } else {
            val f = findEditFragment()
            if (f != null) {
                f.confirmDiscard {
                    replaceEditFragment(noteId)
                }
            } else {
                replaceEditFragment(noteId)
            }
        }
    }

    private fun findEditFragment(): EditFragment? {
        return supportFragmentManager.findFragmentById(R.id.editFragmentContainer) as? EditFragment
    }

    private fun replaceEditFragment(noteId: Long) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.editFragmentContainer, EditFragment.newInstance(noteId))
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()

        val f = supportFragmentManager.findFragmentById(R.id.noteListFragment) as? ListFragment
        f?.onNoteSelected(noteId)
    }

    override fun onNoteDeleted() {
        val f = findEditFragment()
        if (f != null) {
            supportFragmentManager.beginTransaction()
                .remove(f)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }
    }

    override fun onNoteSaved() {
        // nop
    }

    override fun onBackPressed() {
        val f = findEditFragment()
        if (f != null) {
            f.confirmDiscard {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
