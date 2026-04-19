package cat.ics.aparellscap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvBuildDate = findViewById(R.id.tvBuildDate);

        String version = BuildConfig.BUILD_VERSION;
        tvVersion.setText("Versión: " + version);

        try {
            long timestamp = Long.parseLong(BuildConfig.BUILD_DATE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fecha = sdf.format(new Date(timestamp));
            tvBuildDate.setText("Compilado: " + fecha);
        } catch (NumberFormatException e) {
            tvBuildDate.setText("Compilado: fecha desconocida");
        }

        Button btnRegistro = findViewById(R.id.btnRegistro);
        Button btnListado = findViewById(R.id.btnListado);

        btnRegistro.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                mostrarError("Error al abrir Registro", e);
            }
        });

        btnListado.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, ListadoActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                mostrarError("Error al abrir Listado", e);
            }
        });
    }

    private void mostrarError(String titulo, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(stackTrace)
                .setPositiveButton("OK", null)
                .show();

        Toast.makeText(this, titulo + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
