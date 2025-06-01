package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.material.card.MaterialCardView;
import com.magi.asistencia.R;

public class DashboardActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Bundle extras = getIntent().getExtras();
        String dni      = extras.getString("DNI");
        boolean isAdmin = extras.getBoolean("IS_ADMIN", false);

        Log.d("HISTORICO", "DNI="+dni+"  isAdmin="+isAdmin);

        // Siempre en modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Edge-to-edge + status bar blanca + iconos oscuros
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        // Ajuste de padding top al DrawerLayout
        View root = findViewById(R.id.activity_login_drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return insets;
        });

        // Configuramos toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);

        // ——— LOGO: REDIRIGE AL DASHBOARD ———
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        // ——— MENÚ ICON: ABRE DESPLEGABLE ———
        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        // —————————————————————
        // LÓGICA DE LAS CARDS
        // —————————————————————

        // Card Fichajes
        MaterialCardView cardFichajes = findViewById(R.id.cardFichajes);
        cardFichajes.setOnClickListener(v -> {
            Intent intent = new Intent(this, FichajeActivity.class);
            intent.putExtras(getIntent().getExtras());  // ← copiamos DNI e IS_ADMIN juntos
            startActivity(intent);
        });

        // Card Guardias
        MaterialCardView cardGuardias = findViewById(R.id.cardGuardias);
        cardGuardias.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuardiasActivity.class);
            intent.putExtras(getIntent().getExtras());  // ← copiamos DNI e IS_ADMIN juntos
            startActivity(intent);
        });

        // Card Informes
        MaterialCardView cardInformes = findViewById(R.id.cardInformes);
        cardInformes.setOnClickListener(v -> {
            Intent intent = new Intent(this, InformesActivity.class);
            intent.putExtras(getIntent().getExtras());  // ← copiamos DNI e IS_ADMIN juntos
            startActivity(intent);
        });

        // Card Otros
        MaterialCardView cardOtros = findViewById(R.id.cardOtros);
        cardOtros.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtras(getIntent().getExtras());  // ← copiamos DNI e IS_ADMIN juntos
            startActivity(intent);
        });
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
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
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
}

