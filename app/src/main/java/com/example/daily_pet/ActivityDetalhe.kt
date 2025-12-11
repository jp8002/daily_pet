package com.example.daily_pet

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityDetalhe : AppCompatActivity() {

    lateinit var botao_editar_pet : ImageButton
    lateinit var myDB : DatabaseHelper
    lateinit var nome_habito_value : String
    lateinit var streak : String
    lateinit var nome_pet_value : String
    var objetivo = 0
    lateinit var pet_id : String
    lateinit var id_habito : String
    lateinit var nome_do_pet : String
    var maior_streak = 0

    lateinit var text_view_nome_habito : EditText
    lateinit var detalhe_progress : ProgressBar
    lateinit var text_progress : TextView
    lateinit var descricao_detalhe : String
    lateinit var text_view_descricao : TextView
    lateinit var text_view_nome_pet : TextView
    lateinit var salvar_nome_habito : ImageButton
    lateinit var gif : ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        text_view_nome_habito = findViewById(R.id.nomeHabito)
        text_view_nome_pet = findViewById<TextView>(R.id.editTextNomePet)
        myDB = DatabaseHelper.getInstance(this)
        id_habito = intent.getStringExtra("idHabito").toString()
        detalhe_progress = findViewById(R.id.detalheProgress)
        text_progress = findViewById(R.id.textViewPercent)
        text_view_descricao = findViewById(R.id.textViewDescricao)
        objetivo = 0
        nome_habito_value = ""
        streak = ""
        nome_pet_value = ""
        pet_id = ""
        nome_do_pet = ""
        gif  = findViewById<ImageView>(R.id.gifDetalhado)
        descricao_detalhe = ""
        botao_editar_pet = findViewById(R.id.btnEditarNomePet)
        salvar_nome_habito = findViewById(R.id.btnSalvarNomeHabito)

        val cursor = myDB.getById("habitos", id_habito)
        val botao_reiniciar = findViewById<Button>(R.id.buttonReiniciar)
        val botao_excluir = findViewById<ImageButton>(R.id.buttonExluir)
        val intent = Intent(this, ActivityColecao::class.java)


        carregarDadosDoHabito()

        botao_reiniciar.setOnClickListener {
            val agora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            myDB.updateOne("habitos",id_habito.toString(), "dias_streak", "0")
            myDB.updateOne("habitos", id_habito.toString(), "data_criacao", agora)
            myDB.updateOne("habitos", id_habito.toString(), "objetivo", "30")

            detalhe_progress.progress = 0
            text_progress.text = "0/30"
            text_view_descricao.text = "$nome_pet_value está com 0. Ele evoluirá em 30 dias."

            Glide.with(this)
                .asGif()
                .load(this.resources.getIdentifier(nome_do_pet + "stage1", "drawable", this.packageName))
                .into(gif)
        }

        botao_excluir.setOnClickListener {
            // Delete row from DB
            myDB.deleteOne("habitos", id_habito.toString())
            this.finish()
        }

        botao_editar_pet.setOnClickListener {
            intent.putExtra("idHabito",id_habito)
            intent.putExtra("objetivo",objetivo)
            intent.putExtra("pet_id",pet_id)
            intent.putExtra("nome_pet", nome_pet_value)
            intent.putExtra("maior_streak", maior_streak)
            this.startActivity(intent)
        }

        salvar_nome_habito.setOnClickListener {
            val novo_nome = text_view_nome_habito.text.toString()
            myDB.updateOne("habitos", id_habito, "nome",novo_nome)
            Toast.makeText(this, "Nome do hábito alterado", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        // RECARREGA OS DADOS DO HÁBITO TODA VEZ QUE VOLTAR PRA ESSA TELA
        carregarDadosDoHabito()
    }

    fun carregarDadosDoHabito(){
        myDB.getById("habitos",id_habito)?.use { cursor ->
            if (cursor.moveToFirst()) {
                nome_habito_value = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                streak = cursor.getString(cursor.getColumnIndexOrThrow("dias_streak"))
                nome_pet_value = cursor.getString(cursor.getColumnIndexOrThrow("nome_pet"))
                objetivo = (cursor.getString(cursor.getColumnIndexOrThrow("objetivo"))).toInt()
                pet_id = cursor.getString(cursor.getColumnIndexOrThrow("pet_id"))
                maior_streak = cursor.getInt(cursor.getColumnIndexOrThrow("maior_streak"))

            } else {
                Toast.makeText(this, "Hábito não encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        text_view_nome_habito.setText(nome_habito_value)
        println(streak)
        detalhe_progress.progress = ((streak.toFloat()/objetivo)*100).toInt()
        text_progress.text = "$streak/$objetivo"

        if (objetivo < 90){
            val lasting_days = objetivo - streak.toInt()

            descricao_detalhe = """$nome_pet_value está com $streak. Ele evoluirá em $lasting_days dias."""
        }

        else{
            descricao_detalhe = """$nome_pet_value está com $streak. Ele atingiu sua forma final."""
        }


        text_view_descricao.text = descricao_detalhe
        text_view_nome_pet.text = nome_pet_value

        val pet = myDB.getById("pets",pet_id)
        nome_do_pet = pet?.getString(pet.getColumnIndexOrThrow("nome_pet")).toString()

        Glide.with(this)
            .asGif()
            .load(
                when(objetivo){
                    30 -> this.resources.getIdentifier( nome_do_pet + "stage1", "drawable", this.packageName)
                    60 -> this.resources.getIdentifier(nome_do_pet + "stage2", "drawable", this.packageName)
                    90 -> this.resources.getIdentifier(nome_do_pet + "stage3", "drawable", this.packageName)
                    else -> this.resources.getIdentifier(nome_do_pet + "stage3", "drawable", this.packageName)
                }
            )
            .into(gif)
    }
}