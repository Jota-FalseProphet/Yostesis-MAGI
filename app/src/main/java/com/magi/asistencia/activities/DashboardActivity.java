package com.magi.asistencia.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.magi.asistencia.R;

public class DashboardActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blanco));
        }
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        // 1) Configuramos el logo como "home" y lo dejamos clicable
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);       // ocultamos el título si no lo quieres
            ab.setDisplayUseLogoEnabled(true);          // hacemos visible el logo
        }
        topAppBar.setOnClickListener(v -> {
            // Si pinchan en cualquier parte del toolbar (incluido el logo), recargamos Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
        });

        // 2) Cuando pinchen la hamburguesa, les abrimos un PopupMenu con los módulos
        //    (podrías usar un DrawerLayout / NavigationView si quieres algo más "oficial")
        topAppBar.setNavigationOnClickListener(v -> {
            showModulesMenu(v);
        });
    }

    // Inflamos el menú vacío para que reserve espacio para los iconos
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Nota: no añadimos aquí items, reservamos el menú para el PopupMenu
        return true;
    }

    // Este método muestra tu menú de módulos como un PopupMenu
    private void showModulesMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.menu_dashboard);
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    // Aquí manejas los clicks sobre los items del menú_dashboard.xml
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_fichajes) {
            startActivity(new Intent(this, FichajeActivity.class));
            return true;
        } else if (id == R.id.nav_guardias) {
            // lanzar GuardiasActivity
            return true;
        } else if (id == R.id.nav_informes) {
            // lanzar InformesActivity
            return true;
        } else if (id == R.id.nav_logout) {
            // cerrar sesión
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
