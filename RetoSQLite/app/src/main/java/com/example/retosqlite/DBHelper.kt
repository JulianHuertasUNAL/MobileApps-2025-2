package com.example.retosqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EmpresasDB.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "empresas"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_URL = "url"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PRODUCTOS = "productos"
        const val COLUMN_CLASIFICACION = "clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_URL TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PRODUCTOS TEXT,
                $COLUMN_CLASIFICACION TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insertar una nueva empresa
    fun insertarEmpresa(
       values: ContentValues
    ): Boolean {
        val db = writableDatabase
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }

    // Obtener todas las empresas o filtrar por nombre y/o clasificación
    fun obtenerEmpresas(nombre: String = "", clasificaciones: List<String> = emptyList()): Cursor {
        val db = readableDatabase
        val selectQuery = StringBuilder("SELECT $COLUMN_ID as _id, * FROM $TABLE_NAME")
        val selectionArgs = mutableListOf<String>()

        var whereClause = ""

        if (nombre.isNotEmpty()) {
            whereClause += "$COLUMN_NOMBRE LIKE ?"
            selectionArgs.add("%$nombre%")
        }

        if (clasificaciones.isNotEmpty()) {
            if (whereClause.isNotEmpty()) {
                whereClause += " AND "
            }
            whereClause += "$COLUMN_CLASIFICACION IN (${clasificaciones.joinToString(",") { "?" }})"
            selectionArgs.addAll(clasificaciones)
        }

        if (whereClause.isNotEmpty()) {
            selectQuery.append(" WHERE $whereClause")
        }

        selectQuery.append(" ORDER BY $COLUMN_NOMBRE ASC")

        return db.rawQuery(selectQuery.toString(), selectionArgs.toTypedArray())
    }

    // Obtener una empresa por ID
    fun obtenerEmpresaPorId(id: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Actualizar una empresa
    fun actualizarEmpresa(
        id: Int,
        values: ContentValues
    ): Boolean {
        val db = writableDatabase
        val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return rowsAffected > 0
    }

    // Eliminar una empresa
    fun eliminarEmpresa(id: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted > 0
    }
}
