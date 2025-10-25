package com.example.recomendaciones_app.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.recomendaciones_app.data.model.CarritoItem;
import com.example.recomendaciones_app.data.model.Producto;
import java.util.ArrayList;
import java.util.List;

public class CarritoService {

    private DBHelper dbHelper;

    public CarritoService(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public void anadirAlCarrito(Producto producto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DBHelper.TABLE_CARRITO,
                    new String[]{DBHelper.COLUMN_CARRITO_ID, DBHelper.COLUMN_CANTIDAD},
                    DBHelper.COLUMN_FK_PRODUCTO_ID + " = ?",
                    new String[]{String.valueOf(producto.getLocalId())}, 
                    null, null, null);

            if (cursor.moveToFirst()) {
                int idCarrito = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CARRITO_ID));
                int cantidadActual = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CANTIDAD));
                
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_CANTIDAD, cantidadActual + 1);
                db.update(DBHelper.TABLE_CARRITO, values, DBHelper.COLUMN_CARRITO_ID + " = ?", new String[]{String.valueOf(idCarrito)});
            } else {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_FK_PRODUCTO_ID, producto.getLocalId());
                values.put(DBHelper.COLUMN_CANTIDAD, 1);
                db.insert(DBHelper.TABLE_CARRITO, null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public List<CarritoItem> obtenerItemsCarrito() {
        List<CarritoItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT * FROM " + DBHelper.TABLE_CARRITO + " c " +
                       "INNER JOIN " + DBHelper.TABLE_PRODUCTOS + " p ON c." + DBHelper.COLUMN_FK_PRODUCTO_ID + " = p." + DBHelper.COLUMN_LOCAL_ID;

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    Producto p = new Producto();
                    p.setLocalId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_LOCAL_ID)));
                    p.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE)));
                    p.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRICE)));
                    // Anotación: CORRECCIÓN. Usar el método setThumbnail en lugar de setImageUrl.
                    p.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGE_URL)));

                    int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CANTIDAD));

                    items.add(new CarritoItem(p, cantidad));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return items;
    }

    public void vaciarCarrito() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete(DBHelper.TABLE_CARRITO, null, null);
        } finally {
            db.close();
        }
    }
}
