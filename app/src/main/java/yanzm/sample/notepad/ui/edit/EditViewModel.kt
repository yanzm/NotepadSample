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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import yanzm.sample.notepad.data.AppDatabase
import yanzm.sample.notepad.data.AppDatabaseHolder
import yanzm.sample.notepad.data.Note

class EditViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase by lazy {
        AppDatabaseHolder.database(application)
    }

    private val _noteId = MutableLiveData<Long>(0)

    val note: LiveData<Note?> = _noteId.distinctUntilChanged()
        .switchMap {
            if (it == 0L) {
                MutableLiveData(null)
            } else {
                db.noteDao().findById(it)
            }
        }

    fun setNoteId(noteId: Long) {
        _noteId.value = noteId
    }

    fun save(title: String, body: String, onSaved: () -> Unit) {
        val note = this.note.value
        if (note == null) {
            viewModelScope.launch {
                _noteId.value = db.noteDao().insert(Note(0, title, body))
                onSaved()
            }
        } else {
            viewModelScope.launch {
                db.noteDao().update(note.copy(title = title, body = body))
                onSaved()
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val note = this.note.value ?: return
        viewModelScope.launch {
            db.noteDao().delete(note)
            onDeleted()
        }
    }
}
