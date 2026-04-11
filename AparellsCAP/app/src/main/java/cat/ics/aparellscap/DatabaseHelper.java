package cat.ics.aparellscap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AparellsCAP.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla Equipo
    public static final String TABLE_EQUIPO = "tblEquipo";
    public static final String COL_ID = "Id";
    public static final String COL_NOMBRE = "Nombre";
    public static final String COL_TIPO = "Tipo";
    public static final String COL_CODIGO = "Codigo";
    public static final String COL_MARCA = "Marca";
    public static final String COL_NUMSERIE = "Numserie";
    public static final String COL_NOTAS = "Notas";
    public static final String COL_UBICACION = "Ubicacion";
    public static final String COL_FUNCIONAL = "Funcional"; // 0/1

    // Tabla Incidencias
    public static final String TABLE_INCIDENCIAS = "tblIncidencias";
    public static final String COL_INC_IDEQUIPO = "IdEquipo";
    public static final String COL_INC_FECHA = "Fecha";
    public static final String COL_INC_INCIDENCIA = "Incidencia";
    public static final String COL_INC_FECHARESUELTO = "FechaResuelto";

    // Tabla Revisiones
    public static final String TABLE_REVISIONES = "tblRevisiones";
    public static final String COL_REV_IDEQUIPO = "IdEquipo";
    public static final String COL_REV_FECHA = "Fecha";
    public static final String COL_REV_EMPRESA = "Empresa";
    public static final String COL_REV_NOTAS = "Notas";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EQUIPO = "CREATE TABLE " + TABLE_EQUIPO + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NOMBRE + " TEXT,"
                + COL_TIPO + " TEXT,"
                + COL_CODIGO + " TEXT,"
                + COL_MARCA + " TEXT,"
                + COL_NUMSERIE + " TEXT,"
                + COL_NOTAS + " TEXT,"
                + COL_UBICACION + " TEXT,"
                + COL_FUNCIONAL + " INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_EQUIPO);

        String CREATE_INCIDENCIAS = "CREATE TABLE " + TABLE_INCIDENCIAS + "("
                + COL_INC_IDEQUIPO + " INTEGER,"
                + COL_INC_FECHA + " TEXT,"
                + COL_INC_INCIDENCIA + " TEXT,"
                + COL_INC_FECHARESUELTO + " TEXT,"
                + "FOREIGN KEY(" + COL_INC_IDEQUIPO + ") REFERENCES " + TABLE_EQUIPO + "(" + COL_ID + ")"
                + ")";
        db.execSQL(CREATE_INCIDENCIAS);

        String CREATE_REVISIONES = "CREATE TABLE " + TABLE_REVISIONES + "("
                + COL_REV_IDEQUIPO + " INTEGER,"
                + COL_REV_FECHA + " TEXT,"
                + COL_REV_EMPRESA + " TEXT,"
                + COL_REV_NOTAS + " TEXT,"
                + "FOREIGN KEY(" + COL_REV_IDEQUIPO + ") REFERENCES " + TABLE_EQUIPO + "(" + COL_ID + ")"
                + ")";
        db.execSQL(CREATE_REVISIONES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVISIONES);
        onCreate(db);
    }

    // Métodos CRUD para Equipo

    public long insertEquipo(String nombre, String tipo, String codigo, String marca,
                             String numserie, String notas, String ubicacion, boolean funcional) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, nombre);
        values.put(COL_TIPO, tipo);
        values.put(COL_CODIGO, codigo);
        values.put(COL_MARCA, marca);
        values.put(COL_NUMSERIE, numserie);
        values.put(COL_NOTAS, notas);
        values.put(COL_UBICACION, ubicacion);
        values.put(COL_FUNCIONAL, funcional ? 1 : 0);
        long id = db.insert(TABLE_EQUIPO, null, values);
        db.close();
        return id;
    }

    public boolean updateEquipo(int id, String nombre, String tipo, String codigo, String marca,
                                String numserie, String notas, String ubicacion, boolean funcional) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, nombre);
        values.put(COL_TIPO, tipo);
        values.put(COL_CODIGO, codigo);
        values.put(COL_MARCA, marca);
        values.put(COL_NUMSERIE, numserie);
        values.put(COL_NOTAS, notas);
        values.put(COL_UBICACION, ubicacion);
        values.put(COL_FUNCIONAL, funcional ? 1 : 0);
        int rows = db.update(TABLE_EQUIPO, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public boolean deleteEquipo(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EQUIPO, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public Cursor getEquipoById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EQUIPO, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public Cursor getAllEquipos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EQUIPO, null, null, null, null, null, COL_NOMBRE + " ASC");
    }

    // Métodos para exportar datos (texto formateado)
    public String exportarEquiposATexto() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = getAllEquipos();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO));
                String codigo = cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO));
                String marca = cursor.getString(cursor.getColumnIndexOrThrow(COL_MARCA));
                String numserie = cursor.getString(cursor.getColumnIndexOrThrow(COL_NUMSERIE));
                String notas = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTAS));
                String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_UBICACION));
                boolean funcional = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FUNCIONAL)) == 1;

                sb.append("----------------------------------------\n");
                sb.append("ID: ").append(id).append("\n");
                sb.append("Nombre: ").append(nombre).append("\n");
                sb.append("Tipo: ").append(tipo).append("\n");
                sb.append("Código: ").append(codigo).append("\n");
                sb.append("Marca: ").append(marca).append("\n");
                sb.append("Nº Serie: ").append(numserie).append("\n");
                sb.append("Notas: ").append(notas).append("\n");
                sb.append("Ubicación: ").append(ubicacion).append("\n");
                sb.append("Funcional: ").append(funcional ? "Sí" : "No").append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        sb.append("----------------------------------------\n");
        return sb.toString();
    }
}