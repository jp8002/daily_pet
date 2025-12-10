package com.example.daily_pet

import Habitos_RecyclerAdapter
import android.database.Cursor
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivityColecao : AppCompatActivity() {

    lateinit var botao_editar_nome_pet : ImageButton
    lateinit var campo_nome : EditText
    lateinit var myDB : DatabaseHelper;
    lateinit var recyclerView : RecyclerView

    lateinit var cursor: Cursor;

    lateinit var adapter : Pets_RecyclerAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_colecao)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val objetivo = intent.getIntExtra("objetivo",30)
        val id_habito = intent.getStringExtra("idHabito")
        val nome_pet = intent.getStringExtra("nome_pet")
        botao_editar_nome_pet = findViewById(R.id.btnEditarNomePet)
        campo_nome = findViewById(R.id.editTextNomePet)
        campo_nome.setText(nome_pet)

        recyclerView = this.findViewById(R.id.recyclerViewPets)

        myDB = DatabaseHelper.getInstance(this);

        cursor = myDB.getAllHabitos("pets");

        adapter = Pets_RecyclerAdapter(this, cursor, objetivo, id_habito.toString(), myDB);

        recyclerView.layoutManager = LinearLayoutManager(this);
        recyclerView.adapter = adapter;



        botao_editar_nome_pet.setOnClickListener {
            myDB.updateHabito("habitos", id_habito.toString(), "nome_pet", campo_nome.text.toString())

            setResult(RESULT_OK)
        }
    }
}