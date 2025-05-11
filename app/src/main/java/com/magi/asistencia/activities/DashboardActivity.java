package com.magi.asistencia.activities;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.magi.asistencia.R;

public class DashboardActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //La toolBar y la StatusBar se Hace desde Aquí
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false); // FULL edge-to-edge

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        insetsController.setAppearanceLightStatusBars(true); // dark icons
        View root = findViewById(R.id.drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0); // Push content down
            return insets;
        });

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
    //ToolBar y Status Bar hasta aquí

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

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

}
