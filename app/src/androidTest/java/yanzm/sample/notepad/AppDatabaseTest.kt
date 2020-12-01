package yanzm.sample.notepad

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import yanzm.sample.notepad.data.AppDatabase
import yanzm.sample.notepad.data.Note
import yanzm.sample.notepad.data.NoteDao
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var noteDao: NoteDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        noteDao = db.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetNight() {
        runBlocking {
            val note = Note(title = "title", body = "body")
            val noteId = noteDao.insert(note)

            val note2: Note? = noteDao.findById(noteId).getOrAwaitValue()
            assertThat(note2).isEqualTo(Note(id = noteId, title = "title", body = "body"))
        }
    }
}
