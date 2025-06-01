package com.magi.asistencia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.magi.asistencia.R;
import com.magi.asistencia.model.SessionHorario;
import java.util.ArrayList;
import java.util.List;

public class GuardiaHistoricoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClick { void onItemClick(SessionHorario s); }

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_GUARDIA = 1;

    private final OnItemClick callback;
    private final List<Object> items = new ArrayList<>();

    public GuardiaHistoricoAdapter(OnItemClick cb) {
        this.callback = cb;
    }

    public void setData(List<SessionHorario> nuevos) {
        items.clear();
        if (nuevos != null) items.addAll(nuevos);
        notifyDataSetChanged();
    }

    public void setMixedData(List<Object> mixed) {
        items.clear();
        if (mixed != null) items.addAll(mixed);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String)
                ? VIEW_TYPE_HEADER
                : VIEW_TYPE_GUARDIA;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_guardia, parent, false);
            return new GuardiaVH(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int pos) {
        if (getItemViewType(pos) == VIEW_TYPE_HEADER) {
            ((HeaderVH)holder).bind((String) items.get(pos));
        } else {
            ((GuardiaVH)holder).bind(
                    (SessionHorario) items.get(pos), callback);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        private final TextView tvHeader;
        HeaderVH(View v) {
            super(v);
            tvHeader = v.findViewById(R.id.tvHeader);
        }
        void bind(String header) {
            tvHeader.setText(header);
        }
    }

    static class GuardiaVH extends RecyclerView.ViewHolder {
        TextView tvGrupo, tvAula, tvHora;
        MaterialButton btnCubrir;

        GuardiaVH(View v) {
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
            btnCubrir.setText(libre ? "Libre" : "Asignada");
            int color = libre
                    ? R.color.red_200
                    : R.color.green_200;
            btnCubrir.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), color)
            );
            btnCubrir.setOnClickListener(v -> cb.onItemClick(s));
        }
    }
}
