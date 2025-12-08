package com.example.daily_pet

import Habitos_RecyclerAdapter
import android.app.Dialog
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.daily_pet.utils.DateUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    lateinit var dialog : Dialog;
    lateinit var botaoAdiconar : Button;
    lateinit var botaoCancelar : Button;

    lateinit var  botaoCriar : ImageButton;
    lateinit var myDB : DatabaseHelper;

    lateinit var nomeHabito : EditText;
    lateinit var nomePet : EditText;

    lateinit var recyclerView : RecyclerView

    lateinit var cursor: Cursor;

    lateinit var adapter : Habitos_RecyclerAdapter;

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            //AtualizarDb


            // Atualiza cursor
            cursor.close()
            cursor = myDB.all


            if (cursor != null && cursor.moveToNext()){
                do {
                    var streak = DateUtils.calcularDiasDesdeCriacao(cursor.getString(2))
                    println(streak)
                    myDB.updateOne(cursor.getString(0),"dias_streak",streak.toString())
                } while (cursor.moveToNext())
            }


            adapter.swapCursor(cursor)



            // Agenda próxima atualização em 1 hora (3600000 ms)
            handler.postDelayed(this, 1000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        dialog = Dialog(this);
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handler.post(updateRunnable)
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        myDB = DatabaseHelper.getInstance(this);
        botaoAdiconar = dialog.findViewById(R.id.btnAdicionar)
        botaoCancelar = dialog.findViewById(R.id.btnCancelar)
        botaoCriar = this.findViewById(R.id.btnCriar)
        nomeHabito = dialog.findViewById(R.id.editNomeHabito)
        nomePet = dialog.findViewById(R.id.editNomePet)
        recyclerView = this.findViewById(R.id.myRecicleView)


        cursor = myDB.all;

        adapter = Habitos_RecyclerAdapter(this, cursor);

        recyclerView.layoutManager = LinearLayoutManager(this);
        recyclerView.adapter = adapter;




        botaoCriar.setOnClickListener {
            dialog.show();
        }

        botaoCancelar.setOnClickListener {
            dialog.dismiss();
        }

        botaoAdiconar.setOnClickListener {


            var formater = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            val agora = LocalDateTime.now().format(formater);

            var isInserted = myDB.insertData(nomeHabito.text.toString(), agora, "0", nomePet.text.toString(),30)

            if (isInserted == false){
                Toast.makeText(this,"Algo deu errado", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this,"Sucesso", Toast.LENGTH_SHORT).show();

            nomeHabito.text = null;
            nomePet.text = null;


            if (cursor == null || !cursor.moveToNext()){
                Toast.makeText(this, "Algo deu errado", Toast.LENGTH_SHORT);
            }

            AtualizarRecycle();

            dialog.dismiss();

        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition

                if (cursor.moveToPosition(position)) {

                    println(cursor.getInt(cursor.getColumnIndexOrThrow("id")))
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

                    // Delete row from DB
                    myDB.deleteOne(id.toString())

                    // Update RecyclerView with new cursor
                    cursor = myDB.all
                    adapter.swapCursor(cursor)

                    Toast.makeText(applicationContext, "Deletado!", Toast.LENGTH_SHORT).show()
                }

            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    private fun AtualizarRecycle(){
        cursor.close()  // Close old cursor
        cursor = myDB.all  // Get new cursor
        adapter = Habitos_RecyclerAdapter(this, cursor)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        AtualizarRecycle()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}