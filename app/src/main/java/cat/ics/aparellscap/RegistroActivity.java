package cat.ics.aparellscap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int MAX_PHOTOS = 3;

    private EditText etNombre, etTipo, etCodigo, etMarca, etNumserie, etNotas, etUbicacion;
    private Switch swFuncional;
    private Button btnCrear, btnActualizar, btnEliminar, btnSalir, btnFoto;
    private ViewPager2 viewPagerFotos;
    private TabLayout tabLayoutFotos;
    private FotoPagerAdapter fotoAdapter;

    private DatabaseHelper dbHelper;
    private int currentEquipoId = -1;

    private File currentPhotoFile;
    private List<File> photoFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DatabaseHelper(this);

        // Vincular vistas con comprobación de nulidad
        etNombre = findViewById(R.id.etNombre);
        etTipo = findViewById(R.id.etTipo);
        etCodigo = findViewById(R.id.etCodigo);
        etMarca = findViewById(R.id.etMarca);
        etNumserie = findViewById(R.id.etNumserie);
        etNotas = findViewById(R.id.etNotas);
        etUbicacion = findViewById(R.id.etUbicacion);
        swFuncional = findViewById(R.id.swFuncional);
        btnCrear = findViewById(R.id.btnCrear);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir = findViewById(R.id.btnSalir);
        btnFoto = findViewById(R.id.btnFoto);
        viewPagerFotos = findViewById(R.id.viewPagerFotos);
        tabLayoutFotos = findViewById(R.id.tabLayoutFotos);

        // Inicializar lista de fotos vacía para evitar NullPointer
        for (int i = 0; i < MAX_PHOTOS; i++) {
            photoFiles.add(null);
        }

        // Configurar adaptador de fotos
        fotoAdapter = new FotoPagerAdapter(this, photoFiles);
        viewPagerFotos.setAdapter(fotoAdapter);
        new TabLayoutMediator(tabLayoutFotos, viewPagerFotos,
                (tab, position) -> tab.setText("Foto " + (position + 1))).attach();

        // Obtener ID si venimos de listado
        Intent intent = getIntent();
        if (intent.hasExtra("EQUIPO_ID")) {
            currentEquipoId = intent.getIntExtra("EQUIPO_ID", -1);
            cargarDatosEquipo(currentEquipoId);
            btnCrear.setEnabled(false);
        } else {
            btnActualizar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }

        // Listeners
        btnCrear.setOnClickListener(v -> crearEquipo());
        btnActualizar.setOnClickListener(v -> actualizarEquipo());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
        btnSalir.setOnClickListener(v -> finish());
        btnFoto.setOnClickListener(v -> verificarPermisosYTomarFoto());
    }

    private void cargarDatosEquipo(int id) {
        Cursor cursor = dbHelper.getEquipoById(id);
        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)));
            etTipo.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIPO)));
            etCodigo.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CODIGO)));
            etMarca.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MARCA)));
            etNumserie.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NUMSERIE)));
            etNotas.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTAS)));
            etUbicacion.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBICACION)));
            swFuncional.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FUNCIONAL)) == 1);
        }
        cursor.close();
        cargarFotosGuardadas(id);
    }

    private void cargarFotosGuardadas(int equipoId) {
        photoFiles.clear();
        File dir = new File(getFilesDir(), "images");
        if (!dir.exists()) dir.mkdirs();
        for (int i = 1; i <= MAX_PHOTOS; i++) {
            File photo = new File(dir, equipoId + "_" + i + ".jpg");
            if (photo.exists()) {
                photoFiles.add(photo);
            } else {
                photoFiles.add(null);
            }
        }
        fotoAdapter.notifyDataSetChanged();
    }

    private void verificarPermisosYTomarFoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                dispatchTakePictureIntent();
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "cat.ics.aparellscap.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getFilesDir(), "images");
        if (!storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoFile = image;
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                int freeIndex = -1;
                for (int i = 0; i < photoFiles.size(); i++) {
                    if (photoFiles.get(i) == null) {
                        freeIndex = i;
                        break;
                    }
                }
                if (freeIndex == -1) {
                    Toast.makeText(this, "Ya hay 3 fotos guardadas", Toast.LENGTH_SHORT).show();
                    currentPhotoFile.delete();
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath());
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 768, true);
                bitmap.recycle();

                String fileName;
                if (currentEquipoId != -1) {
                    fileName = currentEquipoId + "_" + (freeIndex + 1) + ".jpg";
                } else {
                    fileName = "temp_" + freeIndex + "_" + System.currentTimeMillis() + ".jpg";
                }

                File finalFile = new File(getFilesDir(), "images/" + fileName);
                try (FileOutputStream out = new FileOutputStream(finalFile)) {
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show();
                    currentPhotoFile.delete();
                    return;
                }

                currentPhotoFile.delete();
                photoFiles.set(freeIndex, finalFile);
                fotoAdapter.notifyDataSetChanged();
            }
        }
    }

    private void crearEquipo() {
        if (!validarCampos()) return;
        String nombre = etNombre.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String codigo = etCodigo.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String numserie = etNumserie.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        boolean funcional = swFuncional.isChecked();

        long newId = dbHelper.insertEquipo(nombre, tipo, codigo, marca, numserie, notas, ubicacion, funcional);
        if (newId != -1) {
            for (int i = 0; i < photoFiles.size(); i++) {
                File f = photoFiles.get(i);
                if (f != null && f.getName().startsWith("temp_")) {
                    File newFile = new File(getFilesDir(), "images/" + newId + "_" + (i + 1) + ".jpg");
                    if (f.renameTo(newFile)) {
                        photoFiles.set(i, newFile);
                    }
                }
            }
            Toast.makeText(this, "Equipo creado", Toast.LENGTH_SHORT).show();
            currentEquipoId = (int) newId;
            btnCrear.setEnabled(false);
            btnActualizar.setEnabled(true);
            btnEliminar.setEnabled(true);
        } else {
            Toast.makeText(this, "Error al crear", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEquipo() {
        if (currentEquipoId == -1) return;
        if (!validarCampos()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar actualización");
        builder.setMessage("¿Guardar los cambios?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String tipo = etTipo.getText().toString().trim();
            String codigo = etCodigo.getText().toString().trim();
            String marca = etMarca.getText().toString().trim();
            String numserie = etNumserie.getText().toString().trim();
            String notas = etNotas.getText().toString().trim();
            String ubicacion = etUbicacion.getText().toString().trim();
            boolean funcional = swFuncional.isChecked();
            boolean success = dbHelper.updateEquipo(currentEquipoId, nombre, tipo, codigo, marca, numserie, notas, ubicacion, funcional);
            if (success) {
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void confirmarEliminar() {
        if (currentEquipoId == -1) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar equipo");
        builder.setMessage("¿Está seguro de eliminar este equipo? También se borrarán sus fotos.");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            boolean success = dbHelper.deleteEquipo(currentEquipoId);
            if (success) {
                for (File f : photoFiles) {
                    if (f != null && f.exists()) f.delete();
                }
                Toast.makeText(this, "Equipo eliminado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("Requerido");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
