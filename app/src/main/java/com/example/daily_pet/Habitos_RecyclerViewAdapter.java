package com.example.daily_pet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Habitos_RecyclerViewAdapter extends RecyclerView.Adapter<Habitos_RecyclerViewAdapter.MyViewHolder> {

    Context context;
    Cursor cursor;
    public Habitos_RecyclerViewAdapter(Context context, Cursor cursor){
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public Habitos_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.listagem_habito,parent,false);
        return new Habitos_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Habitos_RecyclerViewAdapter.MyViewHolder holder, int position) {

        if(cursor.moveToPosition(position)){

            String nomeHabito = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            holder.nomeHabito.setText(nomeHabito);
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nomeHabito;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeHabito = itemView.findViewById(R.id.nomeHabito);
        }
    }
}
