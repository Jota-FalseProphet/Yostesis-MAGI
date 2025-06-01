package com.magi.asistencia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.magi.asistencia.R;
import com.magi.asistencia.model.GuardiaHistorico;

import java.util.ArrayList;
import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private final List<Object> data = new ArrayList<>();
    private final OnItemClick callback;

    public interface OnItemClick { void onItemClick(GuardiaHistorico g); }

    public HistoricoAdapter(OnItemClick cb) {
        this.callback = cb;
    }

    public void setData(List<GuardiaHistorico> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (data.get(position) instanceof String)
                ? TYPE_HEADER
                : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_guardia_historico, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (getItemViewType(pos) == TYPE_HEADER) {
            ((HeaderVH) holder).bind((String) data.get(pos));
        } else {
            ((ItemVH) holder).bind((GuardiaHistorico) data.get(pos), callback);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(View v) {
            super(v);
            tvHeader = v.findViewById(R.id.tvHeader);
        }
        void bind(String dni) {
            tvHeader.setText("DNI: " + dni);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvGrupo, tvAula, tvFecha;
        TextView tvSessionId, tvHora;

        ItemVH(View v) {
            super(v);
            tvGrupo       = v.findViewById(R.id.tvGrupo);
            tvAula        = v.findViewById(R.id.tvAula);
            tvFecha       = v.findViewById(R.id.tvFecha);
            tvSessionId   = v.findViewById(R.id.tvSessionId);
            tvHora        = v.findViewById(R.id.tvHora);
        }

        void bind(GuardiaHistorico g, OnItemClick cb) {
            tvGrupo.setText(g.getGrupo());
            tvAula.setText("Aula " + g.getAula());
            tvFecha.setText(g.getFechaGuardia());
            tvSessionId.setText("Sesión #" + g.getSessionId());
            tvHora.setText(g.getHora());

            itemView.setOnClickListener(v -> cb.onItemClick(g));
        }
    }

}
