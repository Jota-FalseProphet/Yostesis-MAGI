package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.magi.asistencia.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG      = "ProfileActivity";
    private static final String DOC_URL  = "https://magi.it.com/api/docentes";

    private DrawerLayout    drawerLayout;
    private NavigationView  navigationView;
    private MaterialToolbar topAppBar;

    private String dni;
    private boolean isAdmin;

    private CardView       cardProfilePhoto;
    private ImageView      ivPhoto;
    private TextView       tvDniValue;
    private TextView       tvNameValue;
    private TextView       tvBirthValue;
    private TextView       tvRoleValue;
    private MaterialButton btnLogout;

    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dni     = extras.getString("DNI");
            isAdmin = extras.getBoolean("IS_ADMIN", false);
        }
        Log.d(TAG, "DNI=" + dni + "  isAdmin=" + isAdmin);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowManager.LayoutParams wParams = getWindow().getAttributes();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        View root = findViewById(R.id.activity_profile_drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return insets;
        });

        topAppBar      = findViewById(R.id.topAppBar);
        drawerLayout   = findViewById(R.id.activity_profile_drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        cardProfilePhoto = findViewById(R.id.card_profile_photo);
        ivPhoto          = findViewById(R.id.iv_profile_photo);
        tvDniValue       = findViewById(R.id.tv_dni_value);
        tvNameValue      = findViewById(R.id.tv_name_value);
        tvBirthValue     = findViewById(R.id.tv_birth_value);
        tvRoleValue      = findViewById(R.id.tv_role_value);

        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
        }

        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtras(getIntent().getExtras());
            startActivity(i);
        });

        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_fichajes) {
                intent = new Intent(this, FichajeActivity.class);
            } else if (id == R.id.nav_guardias) {
                intent = new Intent(this, GuardiasActivity.class);
            } else if (id == R.id.nav_informes) {
                intent = new Intent(this, InformesActivity.class);
            } else if (id == R.id.nav_logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Cerrar sesión");
                builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
                builder.setPositiveButton("Sí, cerrar", (dialog, which) -> {
                    Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
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

            if (intent != null) {
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });

        httpClient = new OkHttpClient();
        fetchDocenteFromBackend();

        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(this, MenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        }
    }

    private void fetchDocenteFromBackend() {
        String url = DOC_URL + "/" + dni;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error de red al obtener datos de perfil: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Error de red al cargar perfil", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bodyString = response.body().string();

                if (!bodyString.trim().startsWith("{")) {
                    Log.e(TAG, "Respuesta inesperada (no JSON): " + bodyString);
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this,
                                "Error al cargar datos del perfil", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(bodyString);
                    final String nombre      = obj.optString("nombre", "");
                    final String fechaNac    = obj.optString("fecha_nacimiento", "");
                    final String rolJson     = obj.optString("rol", "");
                    final String fotoUrl     = obj.optString("url_foto", null);

                    final String rolFinal;
                    if (isAdmin) {
                        rolFinal = "Admin";
                    } else {
                        rolFinal = rolJson.isEmpty() ? "Docente" : rolJson;
                    }

                    runOnUiThread(() -> {
                        tvDniValue.setText(dni);
                        tvNameValue.setText(!nombre.isEmpty() ? nombre : "-");
                        tvBirthValue.setText(!fechaNac.isEmpty() ? fechaNac : "-");
                        tvRoleValue.setText(rolFinal);
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Error parseando JSON: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this,
                                "Error al procesar datos del servidor", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            }
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
            if (isAdmin) {
                Intent intent = new Intent(this, InformesActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Solo accesible por el administrador", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cerrar sesión");
            builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
            builder.setPositiveButton("Sí, cerrar", (dialog, which) -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
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
