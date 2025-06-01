package com.magi.asistencia.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.material.button.MaterialButton;
import com.magi.asistencia.R;

public class MenuActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private String dni;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        /* -------- EXTRAÍMOS DNI + IS_ADMIN DESDE EL INTENT -------- */
        dni      = getIntent().getStringExtra("DNI");
        isAdmin  = getIntent().getBooleanExtra("IS_ADMIN", false);
        Log.d("MENU", "DNI="+dni+"  isAdmin="+isAdmin);

        /* -------- Apariencia general idéntica a Dashboard -------- */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        View root = findViewById(R.id.activity_menu_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sb.top, 0, 0);
            return insets;
        });

        /* ------------------ Toolbar ------------------ */
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);

        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtras(getIntent().getExtras());     // conserva DNI + IS_ADMIN
            startActivity(i);
        });

        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

      //botones
        MaterialButton btnUserAdmin = findViewById(R.id.btnUserAdmin);
        MaterialButton btnPerfil    = findViewById(R.id.btnPerfil);
        MaterialButton btnAjustes   = findViewById(R.id.btnAjustes);
        MaterialButton btnAddFalta  = findViewById(R.id.btnAddFalta);

       //gestion de usuarios
        if (isAdmin) {
            btnUserAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(this, GestionUsuariosActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            });
        } else {
            btnUserAdmin.setEnabled(false);
            btnUserAdmin.setAlpha(0.5f);

        }

        //mi perfil
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        });

        if (isAdmin) {
            btnAddFalta.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddFaltaActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            });
        } else {
            btnAddFalta.setEnabled(false);
            btnAddFalta.setAlpha(0.5f);
        }

        //ajustes
        btnAjustes.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        });
    }

    //menudesplegablemismalogicadeldashboard jejeje
    //menudesplegablemismalogicadeldashboard jejeje
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.nav_fichajes) {
            intent = new Intent(this, FichajeActivity.class);
        } else if (id == R.id.nav_guardias) {
            intent = new Intent(this, GuardiasActivity.class);
        } else if (id == R.id.nav_informes) {
            intent = new Intent(this, InformesActivity.class);
        } else if (id == R.id.nav_logout) {
            // Lógica de logout aquí
            return true;
        }
        if (intent != null) {
            intent.putExtras(getIntent().getExtras());   // DNI + IS_ADMIN
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
