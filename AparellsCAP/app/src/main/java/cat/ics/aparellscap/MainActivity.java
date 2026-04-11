package cat.ics.aparellscap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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