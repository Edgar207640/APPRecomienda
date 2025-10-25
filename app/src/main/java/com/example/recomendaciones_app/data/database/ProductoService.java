package com.example.recomendaciones_app.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.recomendaciones_app.data.model.Producto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductoService {

    private DBHelper dbHelper;
    private static final String TAG = "ProductoService";

    public ProductoService(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public void insertarProductos(List<Producto> productos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Producto producto : productos) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_API_ID, producto.getApiId());
                values.put(DBHelper.COLUMN_NOMBRE, producto.getNombre());
                values.put(DBHelper.COLUMN_DESCRIPCION, producto.getDescripcion());
                values.put(DBHelper.COLUMN_PRICE, producto.getPrice());
                values.put(DBHelper.COLUMN_RATING, producto.getRating());
                values.put(DBHelper.COLUMN_BRAND, producto.getBrand());
                values.put(DBHelper.COLUMN_CATEGORIA, producto.getCategoria());
                values.put(DBHelper.COLUMN_IMAGE_URL, producto.getThumbnail());
                values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

                db.insertWithOnConflict(DBHelper.TABLE_PRODUCTOS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error al insertar productos", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Anotación: CORRECCIÓN. El método ahora usa apiId, que es un identificador fiable y siempre disponible.
    public void actualizarProductoEstado(int apiId, boolean visto, boolean meGusta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_VISTO, visto ? 1 : 0);
            values.put(DBHelper.COLUMN_ME_GUSTA, meGusta ? 1 : 0);
            values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

            // Anotación: CORRECCIÓN. La condición WHERE ahora usa el apiId.
            db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_API_ID + " = ?", new String[]{String.valueOf(apiId)});
        } finally {
            db.close();
        }
    }

    /**
     * Método defensivo: actualiza el estado (visto/meGusta) usando el objeto Producto.
     * Intenta usar apiId si está disponible, luego localId, luego nombre; si no encuentra
     * nada hace una inserción mínima para que exista la fila.
     */
    public void actualizarProductoEstado(Producto producto) {
        if (producto == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_VISTO, producto.getVisto() == 1 ? 1 : 0);
            values.put(DBHelper.COLUMN_ME_GUSTA, producto.getMeGusta() == 1 ? 1 : 0);
            values.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

            int apiId = producto.getApiId();
            int localId = producto.getLocalId();
            String nombre = producto.getNombre();

            int rowsUpdated = 0;
            if (apiId > 0) {
                rowsUpdated = db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_API_ID + " = ?", new String[]{String.valueOf(apiId)});
            } else if (localId > 0) {
                rowsUpdated = db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_LOCAL_ID + " = ?", new String[]{String.valueOf(localId)});
            } else if (nombre != null && !nombre.isEmpty()) {
                rowsUpdated = db.update(DBHelper.TABLE_PRODUCTOS, values, DBHelper.COLUMN_NOMBRE + " = ?", new String[]{nombre});
            }

            // Si no actualizó ninguna fila, intentamos insertar una fila mínima para mantener consistencia
            if (rowsUpdated == 0) {
                ContentValues insertValues = new ContentValues();
                if (apiId > 0) insertValues.put(DBHelper.COLUMN_API_ID, apiId);
                insertValues.put(DBHelper.COLUMN_NOMBRE, nombre != null ? nombre : "");
                insertValues.put(DBHelper.COLUMN_DESCRIPCION, producto.getDescripcion());
                insertValues.put(DBHelper.COLUMN_PRICE, producto.getPrice());
                insertValues.put(DBHelper.COLUMN_RATING, producto.getRating());
                insertValues.put(DBHelper.COLUMN_BRAND, producto.getBrand());
                insertValues.put(DBHelper.COLUMN_CATEGORIA, producto.getCategoria());
                insertValues.put(DBHelper.COLUMN_IMAGE_URL, producto.getThumbnail());
                insertValues.put(DBHelper.COLUMN_VISTO, producto.getVisto() == 1 ? 1 : 0);
                insertValues.put(DBHelper.COLUMN_ME_GUSTA, producto.getMeGusta() == 1 ? 1 : 0);
                insertValues.put(DBHelper.COLUMN_TIMESTAMP, getCurrentTimestamp());

                db.insertWithOnConflict(DBHelper.TABLE_PRODUCTOS, null, insertValues, SQLiteDatabase.CONFLICT_IGNORE);
            }

        } catch (SQLiteException e) {
            Log.e(TAG, "Error al actualizar producto", e);
        } finally {
            db.close();
        }
    }

    public List<Producto> obtenerTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DBHelper.TABLE_PRODUCTOS, null, null, null, null, null, DBHelper.COLUMN_LOCAL_ID + " DESC");
            return cursorToList(cursor);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public List<Producto> obtenerPorFavoritos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection = DBHelper.COLUMN_ME_GUSTA + " = ?";
            String[] selectionArgs = {"1"};
            cursor = db.query(DBHelper.TABLE_PRODUCTOS, null, selection, selectionArgs, null, null, DBHelper.COLUMN_TIMESTAMP + " DESC");
            return cursorToList(cursor);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private List<Producto> cursorToList(Cursor cursor) {
        List<Producto> productos = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int localIdIndex = cursor.getColumnIndex(DBHelper.COLUMN_LOCAL_ID);
                int apiIdIndex = cursor.getColumnIndex(DBHelper.COLUMN_API_ID);
                int nombreIndex = cursor.getColumnIndex(DBHelper.COLUMN_NOMBRE);
                int descIndex = cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPCION);
                int priceIndex = cursor.getColumnIndex(DBHelper.COLUMN_PRICE);
                int ratingIndex = cursor.getColumnIndex(DBHelper.COLUMN_RATING);
                int brandIndex = cursor.getColumnIndex(DBHelper.COLUMN_BRAND);
                int catIndex = cursor.getColumnIndex(DBHelper.COLUMN_CATEGORIA);
                int imageUrlIndex = cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_URL);
                int vistoIndex = cursor.getColumnIndex(DBHelper.COLUMN_VISTO);
                int meGustaIndex = cursor.getColumnIndex(DBHelper.COLUMN_ME_GUSTA);
                int timestampIndex = cursor.getColumnIndex(DBHelper.COLUMN_TIMESTAMP);

                Producto p = new Producto();
                if (localIdIndex != -1) p.setLocalId(cursor.getInt(localIdIndex));
                if (apiIdIndex != -1) p.setApiId(cursor.getInt(apiIdIndex));
                if (nombreIndex != -1) p.setNombre(cursor.getString(nombreIndex));
                if (descIndex != -1) p.setDescripcion(cursor.getString(descIndex));
                if (priceIndex != -1) p.setPrice(cursor.getDouble(priceIndex));
                if (ratingIndex != -1) p.setRating(cursor.getDouble(ratingIndex));
                if (brandIndex != -1) p.setBrand(cursor.getString(brandIndex));
                if (catIndex != -1) p.setCategoria(cursor.getString(catIndex));
                if (imageUrlIndex != -1) p.setThumbnail(cursor.getString(imageUrlIndex));
                if (vistoIndex != -1) p.setVisto(cursor.getInt(vistoIndex));
                if (meGustaIndex != -1) p.setMeGusta(cursor.getInt(meGustaIndex));
                if (timestampIndex != -1) p.setTimestamp(cursor.getString(timestampIndex));
                productos.add(p);
            } while (cursor.moveToNext());
        }
        return productos;
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
    }
}
