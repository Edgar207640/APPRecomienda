package com.example.recomendaciones_app.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recomendaciones.db";
    // Anotación: MIGRACIÓN. Versión incrementada para añadir nuevos campos de DummyJSON.
    private static final int DATABASE_VERSION = 6;

    // --- TABLA PRODUCTOS ---
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String COLUMN_LOCAL_ID = "localId";
    public static final String COLUMN_API_ID = "apiId";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_IMAGE_URL = "imageUrl";
    public static final String COLUMN_VISTO = "visto";
    public static final String COLUMN_ME_GUSTA = "meGusta";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // --- TABLA CARRITO ---
    public static final String TABLE_CARRITO = "carrito";
    public static final String COLUMN_CARRITO_ID = "idCarrito";
    public static final String COLUMN_FK_PRODUCTO_ID = "fk_id";
    public static final String COLUMN_CANTIDAD = "cantidad";

    private static final String SQL_CREATE_TABLE_PRODUCTOS = "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
            COLUMN_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_API_ID + " INTEGER UNIQUE, " +
            COLUMN_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPCION + " TEXT, " +
            COLUMN_PRICE + " REAL, " +
            COLUMN_RATING + " REAL, " +
            COLUMN_BRAND + " TEXT, " +
            COLUMN_CATEGORIA + " TEXT, " +
            COLUMN_IMAGE_URL + " TEXT, " +
            COLUMN_VISTO + " INTEGER DEFAULT 0, " +
            COLUMN_ME_GUSTA + " INTEGER DEFAULT 0, " +
            COLUMN_TIMESTAMP + " TEXT);";

    private static final String SQL_CREATE_TABLE_CARRITO = "CREATE TABLE " + TABLE_CARRITO + " (" +
            COLUMN_CARRITO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_FK_PRODUCTO_ID + " INTEGER, " +
            COLUMN_CANTIDAD + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_FK_PRODUCTO_ID + ") REFERENCES " + TABLE_PRODUCTOS + "(" + COLUMN_LOCAL_ID + "));";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_PRODUCTOS);
        db.execSQL(SQL_CREATE_TABLE_CARRITO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARRITO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS);
        onCreate(db);
    }
}
