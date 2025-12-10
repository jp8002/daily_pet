import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.daily_pet.ActivityDetalhe
import com.example.daily_pet.DatabaseHelper
import com.example.daily_pet.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Habitos_RecyclerAdapter(
    private val context: Context,
    private var cursor: Cursor
) : RecyclerView.Adapter<Habitos_RecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeHabito: TextView = itemView.findViewById(R.id.nomeHabito)
        val progressBar: ProgressBar = itemView.findViewById(R.id.barra)
        val botaoReiniciar: ImageButton = itemView.findViewById(R.id.btnReiniciar)
        val textoProgresso: TextView = itemView.findViewById(R.id.text_progresso)

        val gif : ImageView = itemView.findViewById(R.id.gifView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listagem_habito, parent, false)
        return MyViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (cursor.moveToPosition(position)) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val dias = cursor.getString(cursor.getColumnIndexOrThrow("dias_streak"))
            var objetivo = (cursor.getString(cursor.getColumnIndexOrThrow("objetivo"))).toInt()
            val pet_id = cursor.getString(cursor.getColumnIndexOrThrow("pet_id"))
            val  db = DatabaseHelper.getInstance(context)
            var formater = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            val agora = LocalDateTime.now().format(formater);


            if (dias.toInt() >= objetivo){
                objetivo = (objetivo.toInt() + 30)
                db.updateHabito("habitos", id,"objetivo", objetivo.toString())
            }



            val progresso = ((dias.toFloat()/objetivo)*100).toInt()
            holder.nomeHabito.text = nome
            holder.progressBar.progress = progresso
            holder.textoProgresso.text = dias + "/$objetivo"

            holder.botaoReiniciar.setOnClickListener {

                db.updateHabito("habitos", id.toString(), "dias_streak", "0")
                db.updateHabito("habitos", id.toString(), "data_criacao", agora)
                db.updateHabito("habitos", id.toString(), "objetivo", "30")
                swapCursor(db.getAllHabitos("habitos")) // atualiza RecyclerView
            }
            val pet = db.getHabitoById("pets",pet_id)


            Glide.with(context)
                .asGif()
                .load(
                    when(objetivo){
                                    30 -> context.resources.getIdentifier(pet?.getString(pet.getColumnIndexOrThrow("nome_pet")) + "stage1", "drawable", context.packageName)
                                    60 -> context.resources.getIdentifier(pet?.getString(pet.getColumnIndexOrThrow("nome_pet")) + "stage2", "drawable", context.packageName)
                                    90 -> context.resources.getIdentifier(pet?.getString(pet.getColumnIndexOrThrow("nome_pet")) + "stage3", "drawable", context.packageName)
                                    else -> context.resources.getIdentifier(pet?.getString(pet.getColumnIndexOrThrow("nome_pet")) + "stage3", "drawable", context.packageName)
                                }
                )
                .into(holder.gif)

            holder.gif.setOnClickListener {
                val intent = Intent(context, ActivityDetalhe::class.java)
                intent.putExtra("idHabito",id)
                context.startActivity(intent)
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
