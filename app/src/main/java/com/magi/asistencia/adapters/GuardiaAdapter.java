package com.magi.asistencia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.magi.asistencia.R;
import com.magi.asistencia.model.SessionHorario;

import java.util.ArrayList;
import java.util.List;

/** Adapter para el RecyclerView del módulo Guardias. */
public class GuardiaAdapter extends RecyclerView.Adapter<GuardiaAdapter.VH> {

    public interface OnItemClick { void onItemClick(SessionHorario s); }

    private final OnItemClick callback;
    private final List<SessionHorario> datos = new ArrayList<>();

    public GuardiaAdapter(OnItemClick cb) { this.callback = cb; }

    public void setData(List<SessionHorario> nuevos){
        datos.clear();
        if (nuevos != null) datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /* ——— ViewHolder ——— */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvGrupo, tvAula, tvHora;
        Chip chipEstado;

        VH(View v) {
            super(v);
            tvGrupo = v.findViewById(R.id.tvGrupo);
            tvAula  = v.findViewById(R.id.tvAula);
            tvHora  = v.findViewById(R.id.tvHora);
            chipEstado = v.findViewById(R.id.chipEstado);
        }

        void bind(SessionHorario s, OnItemClick cb){
            tvGrupo.setText(s.getGrupo());
            tvAula.setText("Aula " + s.getAula());
            tvHora.setText(s.getHoraInicio() + " – " + s.getHoraFin());

            boolean libre = Boolean.FALSE.equals(s.getCubierta());
            chipEstado.setText(libre ? "Libre" : "Asignada");
            chipEstado.setChipBackgroundColorResource(
                    libre ? R.color.red_200 : R.color.green_200);

            itemView.setOnClickListener(v -> cb.onItemClick(s));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vType){
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_guardia, p, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        h.bind(datos.get(pos), callback);
    }
    @Override public int getItemCount(){ return datos.size(); }
}
