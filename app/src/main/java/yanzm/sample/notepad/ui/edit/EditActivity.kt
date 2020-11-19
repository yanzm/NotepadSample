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
package yanzm.sample.notepad.ui.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity(), EditFragment.EditFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0)
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, EditFragment.newInstance(noteId))
                .commit()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNoteDeleted() {
        finish()
    }

    override fun onNoteSaved() {
        finish()
    }

    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(android.R.id.content) as? EditFragment
        if (f != null) {
            f.confirmDiscard {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_NOTE_ID = "note_id"

        fun create(context: Context, noteId: Long): Intent {
            return Intent(context, EditActivity::class.java)
                .putExtra(EXTRA_NOTE_ID, noteId)
        }
    }
}
