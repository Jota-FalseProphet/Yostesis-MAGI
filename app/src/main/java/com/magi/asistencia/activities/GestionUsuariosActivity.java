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
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.magi.asistencia.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GestionUsuariosActivity extends AppCompatActivity {

    private static final String TAG      = "GestionUsuarios";
    private static final String BASE_URL = "https://magi.it.com/";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    private String dni;
    private boolean isAdmin;

    private TextInputEditText editDni;
    private TextInputEditText editNombreCompleto;
    private TextInputEditText editPassword;
    private TextInputEditText editRepeatPassword;
    private MaterialButton       buttonCreate;
    private OkHttpClient         httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);


        dni      = getIntent().getStringExtra("DNI");
        isAdmin  = getIntent().getBooleanExtra("IS_ADMIN", false);


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        View root = findViewById(R.id.activity_gestion_usuarios_constraint_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sys.top, 0, 0);
            return insets;
        });


        drawerLayout   = findViewById(R.id.activity_gestion_usuarios_drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);

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
                Intent logoutIntent = new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
                return true;
            }
            if (intent != null) {
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;
            }
            return false;
        });


        editDni            = findViewById(R.id.editDniCreate);
        editNombreCompleto = findViewById(R.id.editNombreCreate);
        editPassword       = findViewById(R.id.editPasswordCreate);
        editRepeatPassword = findViewById(R.id.editRepeatPasswordCreate);
        buttonCreate       = findViewById(R.id.buttonRegisterCreate);
        httpClient         = new OkHttpClient();


        buttonCreate.setOnClickListener(v -> {
            String nuevoDni       = editDni.getText().toString().trim();
            String nuevoNombre    = editNombreCompleto.getText().toString().trim();
            String nuevaPass      = editPassword.getText().toString().trim();
            String nuevaPassRep   = editRepeatPassword.getText().toString().trim();


            if (nuevoDni.isEmpty() || nuevoNombre.isEmpty() || nuevaPass.isEmpty() || nuevaPassRep.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!nuevoDni.matches("^[0-9]{8}[A-Za-z]$")) {
                Toast.makeText(this, "DNI inválido. Debe ser 8 dígitos y una letra", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nuevaPass.length() < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!nuevaPass.equals(nuevaPassRep)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            crearUsuarioEnBackend(nuevoDni, nuevoNombre, nuevaPass);
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
                Intent intent = new Intent(GestionUsuariosActivity.this, LoginActivity.class);
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

private void crearUsuarioEnBackend(String dni, String nombre, String pass) {
        String url = BASE_URL + "api/docentes";

        try {
            JSONObject json = new JSONObject();
            json.put("dni", dni);
            json.put("nombreCompleto", nombre);
            json.put("contrasena", pass);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error al conectar con el servidor", e);
                    runOnUiThread(() ->
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "Fallo al conectar con el servidor", Toast.LENGTH_LONG).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    int code = response.code();
                    String respBody = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        if (code == 201) {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "Usuario creado correctamente", Toast.LENGTH_LONG).show();
                            // Limpiar campos para poder crear otro:
                            editDni.setText("");
                            editNombreCompleto.setText("");
                            editPassword.setText("");
                            editRepeatPassword.setText("");
                            editDni.requestFocus();
                        } else if (code == 409) {
                            Toast.makeText(GestionUsuariosActivity.this,
                                    "Error: ya existe un usuario con ese DNI.", Toast.LENGTH_LONG).show();
                        } else {
                            String msg = "Error (" + code + ")";
                            if (!respBody.isEmpty()) {
                                msg += ": " + respBody;
                            }
                            Toast.makeText(GestionUsuariosActivity.this,
                                    msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error formando JSON de registro", e);
            Toast.makeText(this, "Error interno al preparar datos", Toast.LENGTH_SHORT).show();
        }
    }
}
