package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.magi.asistencia.R;
import com.magi.asistencia.adapters.DocenteArrayAdapter;
import com.magi.asistencia.adapters.InformeAdapter;
import com.magi.asistencia.model.Docente;
import com.magi.asistencia.model.Informe;
import com.magi.asistencia.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Pantalla de informes:
 * - Selección de periodo (Semana / Mes / Trimestre / Curso / Personalizado)
 * - Filtro avanzado Personalizado:  tipo (Docente | Grupo) + valor + rango de fechas
 * - Listado JSON para periodos predefinidos; PDF para cualquier filtro
 * - Compatible con idDocente / idGrupo en la API
 */
public class InformesActivity extends AppCompatActivity {

    //Constantes y tal
    private static final String TAG          = "InformesActivity";
    private static final String BASE_URL     = "https://magi.it.com/api/informes/faltas";
    private static final String DOC_URL      = "https://magi.it.com/api/docentes";
    private static final String GRUPOS_URL   = "https://magi.it.com/api/grupos";
    private static final String PREFS        = "MAGI_PREFS";
    private static final String PREF_DNI     = "PREF_DNI";

   //cosas de la UI
    private ChipGroup                  chipGroup;
    private View                       customRow;
    private MaterialAutoCompleteTextView autoTipo;
    private MaterialAutoCompleteTextView autoValor;
    private TextInputEditText          etRango;
    private RecyclerView               recyclerInformes;
    private MaterialButton             btnVerPDF;

   //adaptadores
    private InformeAdapter             informeAdapter;
    private ArrayAdapter<String>       tipoAdapter;
    private ArrayAdapter<String>       adapterValores;
    private final List<Docente>        listaDocentes = new ArrayList<>();
    private final List<Grupo>          listaGrupos   = new ArrayList<>();

    //estado
    private String  periodoSeleccionado = "SEMANA";
    private int     idDocenteSel        = -1;
    private int     idGrupoSel          = -1;
    private String  dni;
    private boolean isAdmin;

    //para la persistencia
    private SharedPreferences prefs;

   //el oncreate papá
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

       //tema siempre claro y el status bar tambien
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(w, w.getDecorView()).setAppearanceLightStatusBars(true);
        View root = findViewById(R.id.activity_informes_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v,insets)->{
            Insets s = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0,s.top,0,0);
            return insets;
        });

        //preferencias para la sesion
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        dni     = getIntent().getStringExtra("DNI");
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        if (dni == null) dni = prefs.getString(PREF_DNI, null);
        else prefs.edit().putString(PREF_DNI, dni).apply();

       //toolbar bonitom
        findViewById(R.id.logoText_toolbar).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        findViewById(R.id.ic_menu_toolbar).setOnClickListener(this::showModulesMenu);

       //encontrar vistas y tal
        chipGroup        = findViewById(R.id.chipGroupPeriodo);
        customRow        = findViewById(R.id.customFilterRow);
        autoTipo         = findViewById(R.id.autoTipo);
        autoValor        = findViewById(R.id.autoValor);
        etRango          = findViewById(R.id.etRango);
        recyclerInformes = findViewById(R.id.recyclerInformes);
        btnVerPDF        = findViewById(R.id.btnVerPDF);

        informeAdapter = new InformeAdapter(this::onInformeClick);
        recyclerInformes.setLayoutManager(new LinearLayoutManager(this));
        recyclerInformes.setAdapter(informeAdapter);
        recyclerInformes.setVisibility(View.GONE);

        String[] tipos = {"DOCENTE", "GRUPO"};
        tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, tipos);
        autoTipo.setAdapter(tipoAdapter);
        autoTipo.setInputType(0);
        autoTipo.setThreshold(0);
        autoTipo.setOnClickListener(v ->
                autoTipo.showDropDown());
        autoTipo.setOnItemClickListener((p,v,pos,id)->{
            idDocenteSel = idGrupoSel = -1;
            autoValor.setText("");
            if ("DOCENTE".equals(tipos[pos])) cargarDocentesEnDropdown();
            else                               cargarGruposEnDropdown();
        });

        etRango.setInputType(InputType.TYPE_NULL);
        etRango.setFocusable(false);

//picker de fecha
        MaterialDatePicker<Pair<Long, Long>> rangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTheme(R.style.ThemeOverlay_CustomDatePicker)

                        .setTitleText("Selecciona rango")
                        .build();
//mostrar picker y tal al tocar eso
        etRango.setOnClickListener(v ->
                rangePicker.show(getSupportFragmentManager(), "RANGO"));
        rangePicker.addOnPositiveButtonClickListener(selection -> {
            String desde = Utils.format(selection.first);
            String hasta = Utils.format(selection.second);
            etRango.setText(desde + " - " + hasta);
        });


        chipGroup.setOnCheckedChangeListener((g,checkedId)->{
            if      (checkedId==R.id.chipSemana)     periodoSeleccionado="SEMANA";
            else if (checkedId==R.id.chipMes)        periodoSeleccionado="MES";
            else if (checkedId==R.id.chipTrimestre)  periodoSeleccionado="TRIMESTRE";
            else if (checkedId==R.id.chipCurso)      periodoSeleccionado="CURSO";
            else if (checkedId==R.id.chipPersonalizado) periodoSeleccionado="PERSONALIZADO";
            else periodoSeleccionado="SEMANA";

            if ("PERSONALIZADO".equals(periodoSeleccionado)) {
                customRow.setVisibility(View.VISIBLE);
                recyclerInformes.setVisibility(View.GONE);
                autoTipo.setText(""); autoValor.setText(""); etRango.setText("");
                idDocenteSel = idGrupoSel = -1;
            } else {
                customRow.setVisibility(View.GONE);
                recyclerInformes.setVisibility(View.VISIBLE);
                recargarListaJson();
            }
        });
        chipGroup.check(R.id.chipSemana);


        btnVerPDF.setOnClickListener(v -> {
            if ("PERSONALIZADO".equals(periodoSeleccionado)) {
                if (autoTipo.getText().toString().trim().isEmpty() ||
                        autoValor.getText().toString().trim().isEmpty()||
                        etRango.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this,
                            "Completa tipo, valor y rango", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(buildPdfUrl())));
        });
    }

 /////////////////////// UTILIDADES Y TAL ///////////////////////////

    private String buildPdfUrl() {
        StringBuilder u = new StringBuilder(BASE_URL).append("?formato=PDF");
        if (!"PERSONALIZADO".equals(periodoSeleccionado)) {
            u.append("&periodo=").append(periodoSeleccionado)
                    .append("&ref=").append(LocalDate.now());
        } else if (!etRango.getText().toString().isEmpty()
                && etRango.getText().toString().contains(" - ")) {
            String[] p = etRango.getText().toString().split(" - ");
            u.append("&desde=").append(p[0].trim())
                    .append("&hasta=").append(p[1].trim());
        }
        if (idDocenteSel!=-1) u.append("&idDocente=").append(idDocenteSel);
        if (idGrupoSel  !=-1) u.append("&idGrupo=").append(idGrupoSel);
        return u.toString();
    }

    private void recargarListaJson() {
        StringBuilder u = new StringBuilder(BASE_URL)
                .append("?formato=JSON")
                .append("&periodo=").append(periodoSeleccionado)
                .append("&ref=").append(LocalDate.now());
        if (idDocenteSel!=-1) u.append("&idDocente=").append(idDocenteSel);
        if (idGrupoSel  !=-1) u.append("&idGrupo=").append(idGrupoSel);
        new FetchInformesTask().execute(u.toString());
    }

    private class FetchInformesTask extends AsyncTask<String,Void,List<Informe>> {
        private String error;
        @Override protected List<Informe> doInBackground(String...p) {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(new URL(p[0]).openStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb=new StringBuilder(); String line;
                while((line=r.readLine())!=null) sb.append(line);
                JSONArray arr=new JSONArray(sb.toString());
                List<Informe> l=new ArrayList<>();
                for(int i=0;i<arr.length();i++){
                    JSONObject o=arr.getJSONObject(i);
                    Informe inf=new Informe();
                    inf.setId(o.getInt("idAusencia"));
                    inf.setDocente(o.getString("docente"));
                    inf.setGrupo(o.getString("grupo"));
                    inf.setFecha(o.getString("fecha"));
                    l.add(inf);
                }
                return l;
            } catch(Exception e){ error=e.getMessage();Log.e(TAG,"JSON",e);return null;}
        }
        @Override protected void onPostExecute(List<Informe> l){
            if(l!=null) informeAdapter.submitList(l);
            else Toast.makeText(InformesActivity.this,
                    "Error: "+(error==null?"desconocido":error),Toast.LENGTH_LONG).show();
        }
    }

    private void cargarDocentesEnDropdown() {
        new AsyncTask<Void,Void,Void>(){
            List<String> nombres=new ArrayList<>();
            @Override protected Void doInBackground(Void...v){
                try(BufferedReader r=new BufferedReader(
                        new InputStreamReader(new URL(DOC_URL).openStream(),StandardCharsets.UTF_8))){
                    JSONArray arr=new JSONArray(r.readLine());
                    listaDocentes.clear(); nombres.clear();
                    for(int i=0;i<arr.length();i++){
                        JSONObject o=arr.getJSONObject(i);
                        Docente d=new Docente(o.getInt("id"),o.getString("nombre"));
                        listaDocentes.add(d); nombres.add(d.toString());
                    }
                }catch(Exception e){Log.e(TAG,"docentes",e);}
                return null;
            }
            @Override protected void onPostExecute(Void v){
                adapterValores=new ArrayAdapter<>(InformesActivity.this,
                        android.R.layout.simple_list_item_1,nombres);
                autoValor.setAdapter(adapterValores);
                autoValor.setInputType(0);
                autoValor.setThreshold(0);
                autoValor.setOnClickListener(vw ->
                        autoValor.showDropDown());
                autoValor.setOnItemClickListener((p,view,pos,id)->{
                    idDocenteSel=listaDocentes.get(pos).getId();
                    idGrupoSel=-1;
                    if(!"PERSONALIZADO".equals(periodoSeleccionado)) recargarListaJson();
                });
            }
        }.execute();
    }

    private void cargarGruposEnDropdown() {
        new AsyncTask<Void,Void,Void>(){
            List<String> nombres=new ArrayList<>();
            @Override protected Void doInBackground(Void...v){
                try(BufferedReader r=new BufferedReader(
                        new InputStreamReader(new URL(GRUPOS_URL).openStream(),StandardCharsets.UTF_8))){
                    JSONArray arr=new JSONArray(r.readLine());
                    listaGrupos.clear(); nombres.clear();
                    for(int i=0;i<arr.length();i++){
                        JSONObject o=arr.getJSONObject(i);
                        Grupo g=new Grupo(o.getInt("idGrupo"),o.getString("nombre"));
                        listaGrupos.add(g); nombres.add(g.toString());
                    }
                }catch(Exception e){Log.e(TAG,"grupos",e);}
                return null;
            }
            @Override protected void onPostExecute(Void v){
                adapterValores=new ArrayAdapter<>(InformesActivity.this,
                        android.R.layout.simple_list_item_1,nombres);
                autoValor.setAdapter(adapterValores);
                autoValor.setInputType(0);
                autoValor.setThreshold(0);
                autoValor.setOnClickListener(vw ->
                        autoValor.showDropDown());
                autoValor.setOnItemClickListener((p,view,pos,id)->{
                    idGrupoSel=listaGrupos.get(pos).getId();
                    idDocenteSel=-1;
                    if(!"PERSONALIZADO".equals(periodoSeleccionado)) recargarListaJson();
                });
            }
        }.execute();
    }

    // MENU DESPLEGABLE
    private void showModulesMenu(View anchor) {
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popup.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem item = popup.getMenu().getItem(i);
            if (item.getIcon() != null) {
                item.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
            }
        }
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_fichajes) {
            Intent intent = new Intent(this, FichajeActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_guardias) {
            Intent intent = new Intent(this, GuardiasActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_informes) {
            Intent intent = new Intent(this, InformesActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cerrar sesión");
            builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
            builder.setPositiveButton("Sí, cerrar", (dialog, which) -> {
                Intent intent = new Intent(InformesActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            Button btnSi = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSi.setTextColor(ContextCompat.getColor(this, R.color.amarillo_magi));

            Button btnNo = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            btnNo.setTextColor(ContextCompat.getColor(this, R.color.gris_claro));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onInformeClick(Informe inf){
        Toast.makeText(this,"Informe "+inf.getFecha(),Toast.LENGTH_SHORT).show();
    }

    private String nombreMes(int mes){
        return new String[]{"","enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"}[mes];
    }

   //una clase chiquita pra el grupo y tal
    private static class Grupo{
        private final int id; private final String nombre;
        Grupo(int id,String n){this.id=id;this.nombre=n;}
        int getId(){return id;}
        @Override public String toString(){return nombre;}
    }

}
