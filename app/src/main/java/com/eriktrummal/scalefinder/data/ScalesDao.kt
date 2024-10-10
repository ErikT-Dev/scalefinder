package com.eriktrummal.scalefinder.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import com.eriktrummal.scalefinder.helpers.MusicalNotationFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScalesDao private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ScalesDao? = null

        fun getInstance(context: Context): ScalesDao {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScalesDao(context).also { INSTANCE = it }
            }
        }
    }

    private val dbHelper = ScalesDatabaseHelper(context.applicationContext)
    private val database: SQLiteDatabase by lazy { dbHelper.writableDatabase }

    private suspend fun <T> withDatabase(block: (SQLiteDatabase) -> T): T = withContext(Dispatchers.IO) {
        try {
            block(database)
        } catch (e: SQLiteException) {
            Log.e("ScalesDao", "Database operation failed", e)
            throw e
        }
    }

    suspend fun getAllScales(): List<Scale> = withDatabase { db ->
        val scales = mutableListOf<Scale>()
        db.query("scales", null, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                scales.add(cursorToScale(cursor))
            }
        }
        scales
    }

    suspend fun updateScalesInclusion(family: String, isIncluded: Boolean) = withDatabase { db ->
        db.execSQL(
            "UPDATE scales SET isIncluded = ? WHERE family = ?",
            arrayOf(if (isIncluded) 1 else 0, family)
        )
    }

    suspend fun getMyScales(): List<Scale> = withDatabase { db ->
        val scales = mutableListOf<Scale>()
        val query = """
            SELECT s.*
            FROM scales s
            INNER JOIN my_scales ms ON s.id = ms.scale_id
        """
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                scales.add(cursorToScale(cursor))
            }
        }
        scales
    }

    suspend fun addToMyScales(scale: Scale) = withDatabase { db ->
        db.execSQL("INSERT OR IGNORE INTO my_scales (scale_id) VALUES (?)", arrayOf(scale.id))
    }

    suspend fun removeFromMyScales(scale: Scale) = withDatabase { db ->
        db.delete("my_scales", "scale_id = ?", arrayOf(scale.id.toString()))
    }

    suspend fun isInMyScales(scaleId: Int): Boolean = withDatabase { db ->
        db.query(
            "my_scales",
            arrayOf("scale_id"),
            "scale_id = ?",
            arrayOf(scaleId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    suspend fun getScaleById(id: Int): Scale? = withDatabase { db ->
        db.query(
            "scales",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursorToScale(cursor)
            } else {
                null
            }
        }
    }

    suspend fun getUniqueModeNamesForFamily(family: String): List<Pair<Int, String>> = withDatabase { db ->
        val modes = mutableListOf<Pair<Int, String>>()
        db.query(
            "scales",
            arrayOf("modeNr", "modeName"),
            "family = ?",
            arrayOf(family),
            "modeNr, modeName",
            null,
            "modeNr ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val modeNr = cursor.getInt(cursor.getColumnIndexOrThrow("modeNr"))
                val modeName = cursor.getString(cursor.getColumnIndexOrThrow("modeName"))
                modes.add(Pair(modeNr, modeName))
            }
        }
        modes.distinctBy { it.second }
    }

    suspend fun getScalesForFamilyAndMode(family: String, mode: String): List<Scale> = withDatabase { db ->
        val scales = mutableListOf<Scale>()
        db.query("scales", null, "family = ? AND modeName = ?", arrayOf(family, mode), null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                scales.add(cursorToScale(cursor))
            }
        }
        scales
    }

    private fun cursorToScale(cursor: Cursor): Scale {
        return Scale(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            root = cursor.getInt(cursor.getColumnIndexOrThrow("root")),
            rootN = cursor.getString(cursor.getColumnIndexOrThrow("rootN")) ?: "",
            family = cursor.getString(cursor.getColumnIndexOrThrow("family")) ?: "",
            modeNr = cursor.getInt(cursor.getColumnIndexOrThrow("modeNr")),
            modeName = cursor.getString(cursor.getColumnIndexOrThrow("modeName")) ?: "",
            notes = (1..8).mapNotNull { i ->
                cursor.getColumnIndexOrThrow("n$i").let { columnIndex ->
                    if (cursor.isNull(columnIndex)) null else cursor.getInt(columnIndex).takeIf { it != 0 }
                }
            },
            noteNames = (1..8).mapNotNull { i ->
                cursor.getColumnIndexOrThrow("nn$i").let { columnIndex ->
                    if (cursor.isNull(columnIndex)) null else cursor.getString(columnIndex).takeIf { it.isNotBlank() }
                }
            },
            chords = (1..8).mapNotNull { i ->
                cursor.getColumnIndexOrThrow("c$i").let { columnIndex ->
                    if (cursor.isNull(columnIndex)) null else cursor.getString(columnIndex).takeIf { it.isNotBlank() }
                }
            },
            isIncluded = cursor.getInt(cursor.getColumnIndexOrThrow("isIncluded")) == 1
        )
    }

    suspend fun getAllFamiliesWithLowestId(): List<Pair<String, Int>> = withDatabase { db ->
        val familiesWithId = mutableListOf<Pair<String, Int>>()
        val query = """
            SELECT family, MIN(id) as lowest_id
            FROM scales
            GROUP BY family
            ORDER BY MIN(id)
        """
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                val family = cursor.getString(cursor.getColumnIndexOrThrow("family"))
                val lowestId = cursor.getInt(cursor.getColumnIndexOrThrow("lowest_id"))
                familiesWithId.add(Pair(family, lowestId))
            }
        }
        familiesWithId
    }
    fun close() {
        dbHelper.close()
    }
}



data class Scale(
    val id: Int,
    val root: Int,
    val rootN: String,
    val family: String,
    val modeNr: Int,
    val modeName: String,
    val notes: List<Int>,
    val noteNames: List<String>,
    val chords: List<String>,
    var isIncluded: Boolean = true
) {
    val formattedRootN: String
        get() = MusicalNotationFormatter.formatNoteName(rootN)

    val formattedModeName: String
        get() = MusicalNotationFormatter.formatModeName(modeName)

    val formattedFullName: String
        get() = MusicalNotationFormatter.formatFullName(rootN, modeName)
}