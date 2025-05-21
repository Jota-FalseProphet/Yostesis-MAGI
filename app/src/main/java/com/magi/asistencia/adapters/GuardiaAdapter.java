package com.magi.asistencia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.magi.asistencia.R;
import com.magi.asistencia.model.SessionHorario;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Adapter para el RecyclerView del módulo Guardias. */
public class GuardiaAdapter extends RecyclerView.Adapter<GuardiaAdapter.VH> {

    public interface OnItemClick { void onItemClick(SessionHorario s); }

    private final OnItemClick callback;
    private final List<SessionHorario> datos = new ArrayList<>();

    public GuardiaAdapter(OnItemClick cb) {
        this.callback = cb;
    }

    public void setData(List<SessionHorario> nuevos){
        datos.clear();
        if (nuevos != null) datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /* ——— ViewHolder ——— */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvGrupo, tvAula, tvHora;
        MaterialButton btnCubrir;

        VH(View v) {
            super(v);
            tvGrupo   = v.findViewById(R.id.tvGrupo);
            tvAula    = v.findViewById(R.id.tvAula);
            tvHora    = v.findViewById(R.id.tvHora);
            btnCubrir = v.findViewById(R.id.btnCubrir);
        }

        void bind(SessionHorario s, OnItemClick cb){
            tvGrupo.setText(s.getGrupo());
            tvAula.setText("Aula " + s.getAula());
            tvHora.setText(s.getHoraInicio() + " – " + s.getHoraFin());

            boolean libre = Boolean.FALSE.equals(s.getCubierta());
            // Texto del botón
            btnCubrir.setText(libre ? "Libre" : "Asignada");
            // Color de fondo según estado
            int color = libre
                    ? R.color.red_200
                    : R.color.green_200;
            btnCubrir.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), color)
            );
            // Click sobre el botón
            btnCubrir.setOnClickListener(v -> cb.onItemClick(s));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guardia, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position){
        holder.bind(datos.get(position), callback);
    }

    @Override
    public int getItemCount(){
        return datos.size();
    }
}
