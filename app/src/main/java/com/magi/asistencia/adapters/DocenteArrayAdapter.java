package com.magi.asistencia.adapters;
import android.content.Context;
import android.widget.ArrayAdapter;
import com.magi.asistencia.model.Docente;
import java.util.List;
public class DocenteArrayAdapter extends ArrayAdapter<Docente>{
    public DocenteArrayAdapter(Context c, List<Docente> l){
        super(c, android.R.layout.simple_dropdown_item_1line, l);
    }
}
