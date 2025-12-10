package com.example.daily_pet

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "habitos_db"
        private const val DATABASE_VERSION = 1

        private var instance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS habitos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                data_criacao TEXT,
                dias_streak INTEGER,
                nome_pet TEXT,
                objetivo TEXT,
                pet_id INTEGER,
                FOREIGN KEY (pet_id) REFERENCES pets(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome_pet TEXT,
                requisito INTEGER
            )
        """.trimIndent())

        popularDadosIniciais(db)
    }

    private fun popularDadosIniciais(db: SQLiteDatabase) {
        db.execSQL("""
        INSERT INTO pets (nome_pet, requisito) VALUES
        ('notgodzilla', 0),
        ('notbillcipher', 60)
    """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS habitos")
        db.execSQL("DROP TABLE IF EXISTS pets")
        onCreate(db)
    }

    // Insert using ContentValues or Map (very flexible)
    fun insertData(table: String, values: ContentValues): Boolean {
        val db = writableDatabase
        val result = db.insert(table, null, values)
        return result != -1L
    }

    // Overload: Accept Map<String, Any?> - super clean!
    fun insertData(table: String, data: Map<String, Any?>): Boolean {
        return insertData(table, data.toContentValues())
    }

    fun getAllHabitos(table: String): Cursor {
        val db = readableDatabase
        return db.query(table, null, null, null, null, null, null)
    }

    fun getHabitoById(table: String, id: String?): Cursor? {
        val db = readableDatabase
        return db.query(
            table,
            null,
            "id = ?",
            arrayOf(id),
            null,
            null,
            null
        ).apply {
            moveToFirst() // optional: move to first row
        }
    }

    fun updateHabito(table: String, id: String, campo: String, valor: Any?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(campo, valor as? String) // adjust type as needed or use extension
        }
        val rowsAffected = db.update(table, values, "id = ?", arrayOf(id))
        return rowsAffected > 0
    }


    fun deleteHabito(table: String, id: String): Boolean {
        val db = writableDatabase
        val deletedRows = db.delete(table, "id = ?", arrayOf(id))
        return deletedRows > 0
    }
}

// Extension function: Map -> ContentValues (reuse everywhere!)
fun Map<String, Any?>.toContentValues(): ContentValues = ContentValues().apply {
    for ((key, value) in this@toContentValues) {
        when (value) {
            null -> putNull(key)
            is String -> put(key, value)
            is Int -> put(key, value)
            is Long -> put(key, value)
            is Double -> put(key, value)
            is Float -> put(key, value)
            is Boolean -> put(key, value)
            is ByteArray -> put(key, value)
            else -> put(key, value.toString()) // fallback
        }
    }
}