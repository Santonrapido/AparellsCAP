package cat.ics.aparellscap;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListadoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EquipoAdapter adapter;
    private List<EquipoItem> listaEquipos = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private Button btnExportar, btnExportarTxt, btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        btnExportar = findViewById(R.id.btnExportar);
        btnExportarTxt = findViewById(R.id.btnExportarTxt);
        btnSalir = findViewById(R.id.btnSalir);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EquipoAdapter(this, listaEquipos, equipo -> {
            Intent intent = new Intent(ListadoActivity.this, RegistroActivity.class);
            intent.putExtra("EQUIPO_ID", equipo.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnSalir.setOnClickListener(v -> finish());
        btnExportar.setOnClickListener(v -> exportarRegistroActual());
        btnExportarTxt.setOnClickListener(v -> exportarTodosTxt());

        cargarLista();
    }

    private void cargarLista() {
        listaEquipos.clear();
        Cursor cursor = dbHelper.getAllEquipos();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIPO));
                listaEquipos.add(new EquipoItem(id, nombre, tipo));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void exportarRegistroActual() {
        // Exportar el primer elemento seleccionado (simplificado: pide selección)
        // Para esta demo, exportaremos un diálogo con el primer equipo
        if (listaEquipos.isEmpty()) {
            Toast.makeText(this, "No hay equipos", Toast.LENGTH_SHORT).show();
            return;
        }
        // Seleccionar el primer equipo como ejemplo (en una app real se seleccionaría de la lista)
        EquipoItem item = listaEquipos.get(0);
        exportarEquipoIndividual(item.id);
    }

    private void exportarEquipoIndividual(int equipoId) {
        Cursor cursor = dbHelper.getEquipoById(equipoId);
        if (cursor.moveToFirst()) {
            StringBuilder sb = new StringBuilder();
            sb.append("DETALLE DE EQUIPO\n");
            sb.append("------------------\n");
            sb.append("ID: ").append(equipoId).append("\n");
            sb.append("Nombre: ").append(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE))).append("\n");
            // ... resto de campos
            cursor.close();
            compartirTexto(sb.toString());
        }
    }

    private void exportarTodosTxt() {
        String texto = dbHelper.exportarEquiposATexto();
        compartirTexto(texto);
    }

    private void compartirTexto(String texto) {
        try {
            File file = new File(getCacheDir(), "export_equipos.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(texto);
            writer.close();

            Uri uri = FileProvider.getUriForFile(this, "cat.ics.aparellscap.provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartir vía"));
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    // Clase interna para el adaptador
    static class EquipoItem {
        int id;
        String nombre, tipo;
        EquipoItem(int id, String nombre, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
        }
    }
}
