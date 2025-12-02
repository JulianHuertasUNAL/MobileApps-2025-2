package com.example.retosqlite

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var editTextNameFilter: EditText
    private lateinit var checkBoxConsultoria: CheckBox
    private lateinit var checkBoxDesarrollo: CheckBox
    private lateinit var checkBoxFabrica: CheckBox
    private lateinit var buttonFilter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewCompanies)
        editTextNameFilter = findViewById(R.id.editTextNameFilter)
        checkBoxConsultoria = findViewById(R.id.checkBoxConsultoria)
        checkBoxDesarrollo = findViewById(R.id.checkBoxDesarrollo)
        checkBoxFabrica = findViewById(R.id.checkBoxFabrica)
        buttonFilter = findViewById(R.id.buttonFilter)

        buttonFilter.setOnClickListener {
            val nameFilter = editTextNameFilter.text.toString()
            val classifications = mutableListOf<String>()
            if (checkBoxConsultoria.isChecked) classifications.add("Consultoría")
            if (checkBoxDesarrollo.isChecked) classifications.add("Desarrollo a la medida")
            if (checkBoxFabrica.isChecked) classifications.add("Fábrica de software")

            mostrarEmpresas(nameFilter, classifications)
        }

        // Al hacer clic en una empresa -> abrir DisplayContact (detalle / edición)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, id ->
                val intent = Intent(applicationContext, CompanyDetailActivity::class.java)
                intent.putExtra("id", id.toInt())
                startActivity(intent)
            }
    }

    override fun onResume() {
        super.onResume()
        mostrarEmpresas()
    }

    private fun mostrarEmpresas(name: String = "", classifications: List<String> = emptyList()) {
        val cursor: Cursor = dbHelper.obtenerEmpresas(name, classifications)

        val from = arrayOf(
            DBHelper.COLUMN_NOMBRE,
            DBHelper.COLUMN_CLASIFICACION
        )
        val to = intArrayOf(
            R.id.textViewNombre,
            R.id.textViewClasificacion
        )

        val adapter = SimpleCursorAdapter(
            this,
            R.layout.item_empresa, // layout personalizado para cada fila
            cursor,
            from,
            to,
            0
        )

        listView.adapter = adapter
    }

    // Cargar menú (botón de "Agregar nueva empresa")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Acción al pulsar "Agregar nueva empresa"
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_add -> {
                val intent = Intent(applicationContext, CompanyFormActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}