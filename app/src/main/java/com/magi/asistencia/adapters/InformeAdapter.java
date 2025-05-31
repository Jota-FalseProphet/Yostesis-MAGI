package com.magi.asistencia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magi.asistencia.R;
import com.magi.asistencia.model.Informe;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar la lista de Informes en el RecyclerView de InformesActivity.
 */
public class InformeAdapter extends RecyclerView.Adapter<InformeAdapter.ViewHolder> {

    private final List<Informe> lista = new ArrayList<>();
    private final OnInformeClickListener listener;

    public interface OnInformeClickListener {
        void onClick(Informe informe);
    }

    public InformeAdapter(OnInformeClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Informe> nuevos) {
        lista.clear();
        lista.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InformeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_informe, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull InformeAdapter.ViewHolder holder, int position) {
        Informe inf = lista.get(position);
        holder.bind(inf);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtDocente, txtGrupo, txtFecha, txtTotalFaltas;

        public ViewHolder(@NonNull View itemView, OnInformeClickListener listener) {
            super(itemView);
            txtDocente = itemView.findViewById(R.id.txtDocente);
            txtGrupo = itemView.findViewById(R.id.txtGrupo);
            txtFecha = itemView.findViewById(R.id.txtFecha);


            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onClick((Informe) itemView.getTag());
                }
            });
        }

        public void bind(Informe informe) {
            itemView.setTag(informe);
            txtDocente.setText(informe.getDocente());
            txtGrupo.setText(informe.getGrupo());
            txtFecha.setText(informe.getFecha());

        }
    }
}
