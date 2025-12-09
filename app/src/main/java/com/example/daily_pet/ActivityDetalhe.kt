package com.example.daily_pet

import android.os.Build
import android.os.Bundle
import android.widget.Button
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

        val text_view_nome_habito = findViewById<TextView>(R.id.nomeHabito)
        val text_view_nome_pet = findViewById<TextView>(R.id.textViewNomePet)
        val myDB = DatabaseHelper.getInstance(this)
        val id_habito = intent.getStringExtra("idHabito")
        val cursor = myDB.getHabitoById("habitos", id_habito)
        val detalhe_progress = findViewById<ProgressBar>(R.id.detalheProgress)
        val text_progress = findViewById<TextView>(R.id.textViewPercent)
        val text_view_descricao = findViewById<TextView>(R.id.textViewDescricao)
        val botao_reiniciar = findViewById<Button>(R.id.buttonReiniciar)
        val botao_excluir = findViewById<ImageButton>(R.id.buttonExluir)
        var objetivo = 0
        var nome_habito_value : String = ""
        var streak : String = ""
        var nome_pet_value = ""
        val gif  = findViewById<ImageView>(R.id.gifDetalhado)
        val descricao_detalhe : String

        myDB.getHabitoById("habitos",id_habito)?.use { cursor ->
            if (cursor.moveToFirst()) {
                nome_habito_value = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                streak = cursor.getString(cursor.getColumnIndexOrThrow("dias_streak"))
                nome_pet_value = cursor.getString(cursor.getColumnIndexOrThrow("nome_pet"))
                objetivo = (cursor.getString(cursor.getColumnIndexOrThrow("objetivo"))).toInt()


            } else {
                Toast.makeText(this, "Hábito não encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        text_view_nome_habito.text = nome_habito_value
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

        botao_reiniciar.setOnClickListener {
            val agora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            myDB.updateHabito("habitos",id_habito.toString(), "dias_streak", "0")
            myDB.updateHabito("habitos", id_habito.toString(), "data_criacao", agora)
            myDB.updateHabito("habitos", id_habito.toString(), "objetivo", "30")

            detalhe_progress.progress = 0
            text_progress.text = "0/30"
            text_view_descricao.text = "$nome_habito_value está com 0. Ele evoluirá em 30 dias."

            Glide.with(this)
                .asGif()
                .load(R.drawable.stage1)
                .into(gif)
        }

        botao_excluir.setOnClickListener {
            // Delete row from DB
            myDB.deleteHabito("habitos", id_habito.toString())
            this.finish()
        }

        Glide.with(this)
            .asGif()
            .load(
                when(objetivo){
                    30 -> R.drawable.stage1
                    60 -> R.drawable.stage2
                    90 -> R.drawable.stage3
                    else -> R.drawable.stage3
                }
            )
            .into(gif)

    }
}