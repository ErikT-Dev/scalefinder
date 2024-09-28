package com.eriktrummal.scalefinder.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class ScalesDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "scales.db"
        private const val DATABASE_VERSION = 20
    }

    init {
        if (!checkDatabase()) {
            copyDatabase()
        }
    }

    private fun checkDatabase(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    private fun copyDatabase() {
        try {
            val inputStream = context.assets.open(DATABASE_NAME)
            val outputFile = context.getDatabasePath(DATABASE_NAME)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            val db = SQLiteDatabase.openDatabase(outputFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            db.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw Error("Error copying database")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE scales ADD COLUMN isIncluded INTEGER NOT NULL DEFAULT 1")
        createMyScalesTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < DATABASE_VERSION) {
            context.deleteDatabase(DATABASE_NAME)
            copyDatabase()
        }
    }

    private fun createMyScalesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS my_scales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                scale_id INTEGER UNIQUE,
                FOREIGN KEY (scale_id) REFERENCES scales(id)
            )
        """)
    }
}