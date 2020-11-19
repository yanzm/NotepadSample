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
package yanzm.sample.notepad.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

const val NOTE_DB_NAME = "note-database"

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun findAll(): LiveData<List<Note>>

    @Query("SELECT * FROM Note WHERE id == :noteId")
    fun findById(noteId: Long): LiveData<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

class AppDatabaseHolder {

    companion object {
        private var db: AppDatabase? = null

        fun database(applicationContext: Context): AppDatabase {
            if (db != null) {
                return db!!
            } else {
                synchronized(this) {
                    if (db != null) {
                        return db!!
                    }

                    db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        NOTE_DB_NAME
                    ).build()

                    return db!!
                }
            }
        }
    }
}
