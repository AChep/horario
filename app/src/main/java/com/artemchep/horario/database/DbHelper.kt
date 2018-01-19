package com.artemchep.horario.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.artemchep.horario.extensions.escape
import com.artemchep.horario.utils.io.readTextFromAssets
import timber.log.Timber
import java.io.IOException


/**
 * @author Artem Chepurnoy
 */
class DbHelper(
        private val context: Context
) : SQLiteOpenHelper(context, "db", null, DB_VERSION) {

    companion object {
        const val TAG = "DbHelper"
        const val DB_VERSION = 1

        // Alarms table name
        private const val TABLE_ALARMS = "alarms"
        private const val TABLE_EVENTS = "events"
        private const val TABLE_SUBJECTS = "subjects"
        private const val KEY_ID = "id"
        private const val KEY_ID_SUBJECT = "subjectId"
        private const val KEY_ID_SCHEDULE = "scheduleId"
        private const val KEY_ID_FIRESTORE = "id_firestore"
        private const val KEY_CATEGORY = "category"
        private const val KEY_TITLE = "title"
        private const val KEY_PLACE = "place"
        private const val KEY_NAME = "name"
        private const val KEY_TEXT = "text"
        private const val KEY_ALARM = "alarm"
        private const val KEY_COLOR = "color"
        private const val KEY_REPEAT_TYPE = "repeatType"
        private const val KEY_REPEAT_EVERY = "repeatEvery"
        private const val KEY_DATE_START = "dateStart"
        private const val KEY_TIME_START = "timeStart"
        private const val KEY_DATE_END = "dateEnd"
        private const val KEY_TIME_END = "timeEnd"

        /**
         *
         */
        private const val FILE_CREATE_QUERY = "sql/create.sql.txt"

        private var instance: DbHelper? = null

        @Synchronized
        fun getInstance(context: Context): DbHelper {
            if (instance == null) {
                instance = DbHelper(context)
            }
            return instance!!
        }
    }

    val events = DbEvents()
    val eventSubject = DbEventSubject()
    val subject = DbSubjects()

    override fun onCreate(db: SQLiteDatabase) {
        try {
            Timber.tag(TAG).i("Database creation")
            execSqlFile(FILE_CREATE_QUERY, db)
        } catch (e: IOException) {
            throw RuntimeException("Database creation failed", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    /**
     * Loads script from assets, splits by semicolon and
     * executes one by one.
     * @see [execSql]
     */
    @Throws(IOException::class)
    private fun execSqlFile(path: String, db: SQLiteDatabase) {
        val query = readTextFromAssets(context, path)
        val parts = query.split(';')
        parts.forEach { execSql(it, db) }
    }

    /** Executes given query on database and logs the output */
    private fun execSql(query: String, db: SQLiteDatabase) {
        try {
            Timber.tag(TAG).i("Database EXEC sql={$query}")
            db.execSQL(query)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Database EXEC_FAILED sql={$query}, e=${e.message}", e)
        }
    }

    private fun formSqlObj(vararg vars: Any?): String {
        if (vars.isEmpty()) {
            throw IllegalArgumentException()
        }

        val sb = StringBuilder()
        sb.append("(")

        vars.forEachIndexed { index, any ->
            if (any == null) {
                sb.append("null")
            } else if (any is String) {
                sb.append('\'')
                sb.append(any)
                sb.append('\'')
            } else {
                sb.append(any)
            }

            // Add divider between variables
            if (index < vars.size - 1) {
                sb.append(", ")
            }
        }

        sb.append(")")
        return sb.toString()
    }

    /**
     * @author Artem Chepurnoy
     */
    inner class DbEvents : Crud<SqlEvent>() {

        private fun transformToSqlString(it: SqlEvent): String {
            val title = it.title?.escape()
            val place = it.place?.escape()
            return formSqlObj(it.id, it.subjectId, it.scheduleId, title, place,
                    it.repeatType, it.repeatEvery, it.dateStart, it.timeStart,
                    it.dateEnd, it.timeEnd)
        }

        override fun getAll(): List<SqlEvent> {
            val query = """SELECT
        $TABLE_EVENTS.$KEY_ID,
        $TABLE_EVENTS.$KEY_ID_SUBJECT,
        $TABLE_EVENTS.$KEY_ID_SCHEDULE,
        $TABLE_EVENTS.$KEY_TITLE,
        $TABLE_EVENTS.$KEY_PLACE,

        $TABLE_EVENTS.$KEY_REPEAT_TYPE,
        $TABLE_EVENTS.$KEY_REPEAT_EVERY,

        $TABLE_EVENTS.$KEY_DATE_START,
        $TABLE_EVENTS.$KEY_TIME_START,
        $TABLE_EVENTS.$KEY_DATE_END,
        $TABLE_EVENTS.$KEY_TIME_END
    FROM $TABLE_EVENTS"""

            val list = ArrayList<SqlEvent>()
            var cursor: Cursor? = null
            try {
                Timber.tag(TAG).i("Database RAW_QUERY sql={$query}")
                cursor = readableDatabase.rawQuery(query, null)

                // Looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        var i = 0
                        val model = SqlEvent(
                                id = cursor.getString(i++),
                                subjectId = cursor.getString(i++),
                                scheduleId = cursor.getString(i++),
                                title = cursor.getString(i++),
                                place = cursor.getString(i++),

                                repeatType = cursor.getInt(i++),
                                repeatEvery = cursor.getInt(i++),

                                dateStart = cursor.getLong(i++),
                                timeStart = cursor.getInt(i++),
                                dateEnd = cursor.getLong(i++),
                                timeEnd = cursor.getInt(i)
                        )

                        // Adding model to list
                        list.add(model)
                    } while (cursor.moveToNext())
                }
            } finally {
                cursor?.close()
            }

            return list
        }

        override fun insert(models: Collection<SqlEvent>) {
            if (models.isEmpty()) {
                return
            }

            val values = models.joinToString(", ") { transformToSqlString(it) }
            val query = """INSERT
	INTO $TABLE_EVENTS
	(
        $KEY_ID,
        $KEY_ID_SUBJECT,
        $KEY_ID_SCHEDULE,
        $KEY_TITLE,
        $KEY_PLACE,

        $KEY_REPEAT_TYPE,
        $KEY_REPEAT_EVERY,

        $KEY_DATE_START,
        $KEY_TIME_START,
        $KEY_DATE_END,
        $KEY_TIME_END
    )
    VALUES $values"""
            execSql(query, writableDatabase)
        }

        override fun update(models: Collection<SqlEvent>) {
            if (models.isEmpty()) {
                return
            }

            delete(models.map { it.id })
            insert(models)
        }

        override fun delete(ids: Collection<String>) {
            if (ids.isEmpty()) {
                return
            }

            val values = ids.map {"'$it'"}.joinToString(", ")
            val query = """DELETE
    FROM $TABLE_EVENTS
    WHERE
        $KEY_ID
        IN (
            $values
        )
    """
            execSql(query, writableDatabase)
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    inner class DbSubjects : Crud<SqlSubject>() {

        private fun transformToSqlString(it: SqlSubject): String {
            val name = it.name?.escape()
            return formSqlObj(it.id, name, it.color)
        }

        override fun getAll(): List<SqlSubject> {
            val query = """SELECT
        $KEY_ID,
        $KEY_NAME,
        $KEY_COLOR
    FROM $TABLE_SUBJECTS"""

            val list = ArrayList<SqlSubject>()
            var cursor: Cursor? = null
            try {
                cursor = readableDatabase.rawQuery(query, null)

                // Looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        var i = 0
                        val model = SqlSubject(
                                id = cursor.getString(i++),
                                name = cursor.getString(i++),
                                color = cursor.getInt(i)
                        )

                        // Adding model to list
                        list.add(model)
                    } while (cursor.moveToNext())
                }
            } finally {
                cursor?.close()
            }

            return list
        }

        /**
         * Inserts given models into the database
         * in a batch operation
         */
        override fun insert(models: Collection<SqlSubject>) {
            if (models.isEmpty()) {
                return
            }

            val values = models.joinToString(", ") { transformToSqlString(it) }
            val query = """INSERT
	INTO $TABLE_SUBJECTS
	(
        $KEY_ID,
        $KEY_NAME,
        $KEY_COLOR
    )
    VALUES $values"""
            execSql(query, writableDatabase)
        }

        /**
         * Updates given models in the database
         * in a batch operation
         */
        override fun update(models: Collection<SqlSubject>) {
            if (models.isEmpty()) {
                return
            }

            delete(models.map { it.id })
            insert(models)
        }

        override fun delete(ids: Collection<String>) {
            if (ids.isEmpty()) {
                return
            }

            val values = ids.map {"'$it'"}.joinToString(", ")
            val query = """DELETE
    FROM $TABLE_SUBJECTS
    WHERE
        $KEY_ID
        IN (
            $values
        )
    """
            execSql(query, writableDatabase)
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    inner class DbEventSubject : Crud<SqlEventSubject>() {
        override fun getAll(): List<SqlEventSubject> {
            val query = """SELECT
        $TABLE_EVENTS.$KEY_ID,
        $TABLE_EVENTS.$KEY_ID_SUBJECT,
        $TABLE_SUBJECTS.$KEY_NAME,
        $TABLE_SUBJECTS.$KEY_COLOR,
        $TABLE_EVENTS.$KEY_ID_SCHEDULE,
        $TABLE_EVENTS.$KEY_TITLE,
        $TABLE_EVENTS.$KEY_PLACE,

        $TABLE_EVENTS.$KEY_REPEAT_TYPE,
        $TABLE_EVENTS.$KEY_REPEAT_EVERY,

        $TABLE_EVENTS.$KEY_DATE_START,
        $TABLE_EVENTS.$KEY_TIME_START,
        $TABLE_EVENTS.$KEY_DATE_END,
        $TABLE_EVENTS.$KEY_TIME_END
    FROM $TABLE_EVENTS
        LEFT JOIN $TABLE_SUBJECTS
            ON $TABLE_EVENTS.$KEY_ID_SUBJECT=$TABLE_SUBJECTS.$KEY_ID"""

            val list = ArrayList<SqlEventSubject>()
            var cursor: Cursor? = null
            try {
                Timber.tag(TAG).i("Database RAW_QUERY sql={$query}")
                cursor = readableDatabase.rawQuery(query, null)
                cursor.count

                // Looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        var i = 0
                        val model = SqlEventSubject(
                                id = cursor.getString(i++),
                                subjectId = cursor.getString(i++),
                                subjectName = cursor.getString(i++),
                                subjectColor = cursor.getInt(i++),
                                scheduleId = cursor.getString(i++),
                                title = cursor.getString(i++),
                                place = cursor.getString(i++),

                                repeatType = cursor.getInt(i++),
                                repeatEvery = cursor.getInt(i++),

                                dateStart = cursor.getLong(i++),
                                timeStart = cursor.getInt(i++),
                                dateEnd = cursor.getLong(i++),
                                timeEnd = cursor.getInt(i)
                        )

                        // Adding model to list
                        list.add(model)
                    } while (cursor.moveToNext())
                }
            } finally {
                cursor?.close()
            }

            return list
        }

        override fun insert(models: Collection<SqlEventSubject>) = throw RuntimeException("Use EVENT and SUBJECT tables instead")

        override fun update(models: Collection<SqlEventSubject>) = throw RuntimeException("Use EVENT and SUBJECT tables instead")

        override fun delete(ids: Collection<String>) = throw RuntimeException("Use EVENT and SUBJECT tables instead")
    }

    abstract inner class Crud<T> {
        abstract fun getAll(): List<T>
        abstract fun insert(models: Collection<T>)
        abstract fun update(models: Collection<T>)
        abstract fun delete(ids: Collection<String>)
    }

    fun getAll(category: String? = null): List<SqlAlarm> {
        val condition = category?.let { "WHERE $KEY_CATEGORY='$it'" } ?: ""
        val query = """SELECT
        $KEY_ID,
        $KEY_ID_FIRESTORE,
        $KEY_CATEGORY,
        $KEY_TITLE,
        $KEY_TEXT,
        $KEY_ALARM
    FROM $TABLE_ALARMS
    $condition"""

        val list = ArrayList<SqlAlarm>()
        var cursor: Cursor? = null
        try {
            cursor = readableDatabase.rawQuery(query, null)

            // Looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    var i = 0
                    val model = SqlAlarm(
                            id = cursor.getLong(i++),
                            idFirestore = cursor.getString(i++),
                            category = cursor.getString(i++),
                            title = cursor.getString(i++),
                            text = cursor.getString(i++),
                            alarm = cursor.getLong(i)
                    )

                    // Adding model to list
                    list.add(model)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return list
    }

    /**
     * Inserts given models into the database
     * in a batch operation
     */
    fun insert(models: List<SqlAlarm>) {
        if (models.isEmpty()) {
            return
        }

        val values = models.map {
            val id = it.idFirestore
            val cat = it.category
            val title = it.title?.replace("\'", "\\\'")
            val text = it.text
            val alarm = it.alarm
            "('$id', '$cat', '$title', '$text', $alarm)"
        }.joinToString(", \n")
        val query = """INSERT
	INTO $TABLE_ALARMS
	(
        $KEY_ID_FIRESTORE,
        $KEY_CATEGORY,
        $KEY_TITLE,
        $KEY_TEXT,
        $KEY_ALARM
    )
    VALUES $values"""
        execSql(query, writableDatabase)
    }

    /**
     * Updates given models in the database
     * in a batch operation
     */
    fun update(models: List<SqlAlarm>) {
        if (models.isEmpty()) {
            return
        }

        val values = models.map {
            val id = it.idFirestore
            val cat = it.category
            val title = it.title?.replace("\'", "\\\'")
            val text = it.text
            val alarm = it.alarm
            "('$id', '$cat', '$title', '$text', $alarm)"
        }.joinToString(", \n")
        val query = """UPDATE $TABLE_ALARMS
    SET
        $KEY_ID_FIRESTORE = tmp.$KEY_ID_FIRESTORE,
        $KEY_CATEGORY = tmp.$KEY_CATEGORY,
        $KEY_TITLE = tmp.$KEY_TITLE,
        $KEY_TEXT = tmp.$KEY_TEXT,
        $KEY_ALARM = tmp.$KEY_ALARM
    FROM (
        VALUES
        $values
    ) AS tmp (
        $KEY_ID,
        $KEY_ID_FIRESTORE,
        $KEY_CATEGORY,
        $KEY_TITLE,
        $KEY_TEXT,
        $KEY_ALARM,
    ) WHERE $TABLE_ALARMS.$KEY_ID = tmp.$KEY_ID"""
        execSql(query, writableDatabase)
    }

    fun delete(ids: Collection<Int>) {
        if (ids.isEmpty()) {
            return
        }

        val values = ids.joinToString(", ")
        val query = """DELETE
    FROM $TABLE_ALARMS
    WHERE
        $KEY_ID
        IN (
            $values
        )
    """
        execSql(query, writableDatabase)
    }

}

data class SqlAlarm(
        val id: Long,
        val idFirestore: String,
        val category: String,
        val title: String?,
        val text: String?,
        val alarm: Long
) {
    companion object {
        const val CATEGORY_NOTE = "note"
        const val CATEGORY_EVENT = "event"
    }
}

data class SqlSubject(
        val id: String,
        val name: String?,
        val color: Int
)

data class SqlEvent(
        val id: String,
        val subjectId: String,
        val scheduleId: String,
        val title: String?,
        val place: String?,

        val repeatType: Int = 0,
        val repeatEvery: Int = 0,

        val dateStart: Long = 0,
        val timeStart: Int = 0,
        val dateEnd: Long = 0,
        val timeEnd: Int = 0
)

data class SqlEventSubject(
        val id: String,
        val subjectId: String,
        val subjectName: String?,
        val subjectColor: Int,
        val scheduleId: String,
        val title: String?,
        val place: String?,

        val repeatType: Int = 0,
        val repeatEvery: Int = 0,

        val dateStart: Long = 0,
        val timeStart: Int = 0,
        val dateEnd: Long = 0,
        val timeEnd: Int = 0
)
