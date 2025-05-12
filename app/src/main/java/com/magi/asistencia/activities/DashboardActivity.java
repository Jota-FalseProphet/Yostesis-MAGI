package com.magi.asistencia.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

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
        // Siempre en modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Edge-to-edge + status bar blanca + iconos oscuros
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        // Ajuste de padding top igual al alto de la status bar
        View root = findViewById(R.id.activity_login_drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sb.top, 0, 0);
            return insets;
        });

        // Configuramos toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false); // oculta el texto de título automático (es que pone MAGI en gigante)
        }

        //LOGO TEXTO REDIRIGE AL DASHBOARD
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        //MENU ICON ABRE EL MENU DESPLEGABLE
        topAppBar.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });
        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

    }

    // MENU DESPLEGABLE
    private void showModulesMenu(View anchor) {
        // 1. Contexto con estilo personalizado (ThemeOverlay, no Widget)
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);

        // 2. Crear el PopupMenu con ese wrapper
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(wrapper, anchor);

        // 3. Inflar el menú
        popup.inflate(R.menu.menu_dashboard);

        // 4. Aplicar tinte a los íconos manualmente (por si tu estilo no lo aplica)
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem item = popup.getMenu().getItem(i);
            if (item.getIcon() != null) {
                item.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
            }
        }

        // 5. Listener de clicks
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // 6. Mostrar
        popup.show();
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // aquí tus rutas
        int id = item.getItemId();
        if (id == R.id.nav_fichajes) {
            startActivity(new Intent(this, FichajeActivity.class));
            return true;
        }
        else if (id == R.id.nav_guardias) {
            // …
            return true;
        }
        else if (id == R.id.nav_informes) {
            // …
            return true;
        }
        else if (id == R.id.nav_logout) {
            // …
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
