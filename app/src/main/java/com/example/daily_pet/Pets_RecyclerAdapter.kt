package com.example.daily_pet

import android.app.Activity.RESULT_OK
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class Pets_RecyclerAdapter (
    private val context: Context,
    private var cursor: Cursor,
    private val objetivoAtual: Int = 30,
    private val habito_id : String,
    private val db : DatabaseHelper,
    private val maior_streak : Int
) : RecyclerView.Adapter<Pets_RecyclerAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto_conquista: TextView = itemView.findViewById(R.id.text_conquista)
        val icone_pet : ImageView = itemView.findViewById(R.id.iconepetView)


    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pets_RecyclerAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listagem_pet, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: Pets_RecyclerAdapter.MyViewHolder,
        position: Int
    ) {
        if (cursor.moveToPosition(position)){
            val pet_id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val nome_pey = cursor.getString(cursor.getColumnIndexOrThrow("nome_pet")).toString()
            val requisito = cursor.getString(cursor.getColumnIndexOrThrow("requisito")).toInt()
            holder.texto_conquista.text = "Atinja $requisito dias de progresso"

            if (maior_streak < requisito){
                holder.icone_pet.setImageResource(context.resources.getIdentifier(nome_pey + "_silhouette", "drawable", context.packageName))
            }

            else{
                holder.icone_pet.setImageResource(context.resources.getIdentifier(nome_pey + "stage3", "drawable", context.packageName))
            }



            holder.icone_pet.setOnClickListener {
                if (maior_streak < requisito){
                    Toast.makeText(context,"Você ainda não atingiu os dias necessários", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                db.updateOne("habitos", habito_id.toString(), "pet_id", pet_id)
                Toast.makeText(context,"Pet trocado com sucesso", Toast.LENGTH_SHORT).show()
                (context as ActivityColecao).setResult(RESULT_OK)
            }

        }
    }

    override fun getItemCount(): Int = cursor.count

    fun swapCursor(newCursor: Cursor) {
        cursor.close()
        cursor = newCursor
        notifyDataSetChanged()
    }
}