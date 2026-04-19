package cat.ics.aparellscap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a los nuevos TextView
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvBuildDate = findViewById(R.id.tvBuildDate);

        // Mostrar versión desde BuildConfig
        String version = BuildConfig.BUILD_VERSION;
        tvVersion.setText("Versión: " + version);

        // Formatear la fecha de compilación
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
            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        btnListado.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListadoActivity.class);
            startActivity(intent);
        });
    }
}
