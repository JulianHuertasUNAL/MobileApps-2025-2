package com.example.retosqlite

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DisplayContact : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var empresaId: Int = 0

    private lateinit var editNombre: EditText
    private lateinit var editUrl: EditText
    private lateinit var editTelefono: EditText
    private lateinit var editEmail: EditText
    private lateinit var editProductos: EditText
    private lateinit var spinnerClasificacion: Spinner
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_contact)

        dbHelper = DBHelper(this)

        // Referencias a los campos del layout
        editNombre = findViewById(R.id.editTextNombre)
        editUrl = findViewById(R.id.editTextUrl)
        editTelefono = findViewById(R.id.editTextTelefono)
        editEmail = findViewById(R.id.editTextEmail)
        editProductos = findViewById(R.id.editTextProductos)
        spinnerClasificacion = findViewById(R.id.spinnerClasificacion)
        btnGuardar = findViewById(R.id.buttonGuardar)

        // Configurar opciones del spinner
        val opciones = arrayOf("Consultoría", "Desarrollo a la medida", "Fábrica de software")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClasificacion.adapter = adapter

        // Verificar si se pasó un ID (editar empresa existente)
        empresaId = intent.getIntExtra("id", 0)

        if (empresaId != 0) {
            cargarDatosEmpresa(empresaId)
        }

        // Guardar empresa (nuevo o existente)
        btnGuardar.setOnClickListener {
            guardarEmpresa()
        }
    }

    private fun cargarDatosEmpresa(id: Int) {
        val cursor = dbHelper.obtenerEmpresaPorId(id)
        if (cursor.moveToFirst()) {
            editNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE)))
            editUrl.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_URL)))
            editTelefono.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TELEFONO)))
            editEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL)))
            editProductos.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRODUCTOS)))
            val clasificacion = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CLASIFICACION))

            val pos = (spinnerClasificacion.adapter as ArrayAdapter<String>).getPosition(clasificacion)
            spinnerClasificacion.setSelection(pos)
        }
        cursor.close()
    }

    private fun guardarEmpresa() {
        val nombre = editNombre.text.toString().trim()
        val url = editUrl.text.toString().trim()
        val telefono = editTelefono.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val productos = editProductos.text.toString().trim()
        val clasificacion = spinnerClasificacion.selectedItem.toString()

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "El nombre y el teléfono son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(DBHelper.COLUMN_NOMBRE, nombre)
            put(DBHelper.COLUMN_URL, url)
            put(DBHelper.COLUMN_TELEFONO, telefono)
            put(DBHelper.COLUMN_EMAIL, email)
            put(DBHelper.COLUMN_PRODUCTOS, productos)
            put(DBHelper.COLUMN_CLASIFICACION, clasificacion)
        }

        if (empresaId == 0) {
            dbHelper.insertarEmpresa(values)
            Toast.makeText(this, "Empresa registrada", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.actualizarEmpresa(empresaId, values)
            Toast.makeText(this, "Empresa actualizada", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.display_contact, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_company-> {
                btnGuardar.performClick() // usar el mismo botón guardar
                true
            }

            R.id.delete_company -> {
                if (empresaId != 0) {
                    confirmarEliminacion()
                } else {
                    Toast.makeText(this, "No se puede eliminar una empresa no guardada", Toast.LENGTH_SHORT).show()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar empresa")
            .setMessage("¿Está seguro de eliminar esta empresa?")
            .setPositiveButton("Sí") { _, _ ->
                dbHelper.eliminarEmpresa(empresaId)
                Toast.makeText(this, "Empresa eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
