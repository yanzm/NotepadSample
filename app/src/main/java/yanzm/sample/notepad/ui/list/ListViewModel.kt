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
package yanzm.sample.notepad.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import yanzm.sample.notepad.data.AppDatabase
import yanzm.sample.notepad.data.AppDatabaseHolder
import yanzm.sample.notepad.data.Note

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase by lazy {
        AppDatabaseHolder.database(application)
    }

    val list: LiveData<List<Note>>
        get() = db.noteDao().findAll()

}
