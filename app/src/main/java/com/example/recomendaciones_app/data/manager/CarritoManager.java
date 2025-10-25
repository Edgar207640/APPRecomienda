package com.example.recomendaciones_app.data.manager;

import com.example.recomendaciones_app.data.model.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase Singleton para gestionar el estado del carrito de compras de forma centralizada.
 */
public class CarritoManager {

    private static CarritoManager instance;
    private final List<Producto> cartItems = new ArrayList<>();

    // Constructor privado para evitar instanciación directa.
    private CarritoManager() {}

    /**
     * Obtiene la instancia única de CarritoManager.
     *
     * @return La instancia de CarritoManager.
     */
    public static synchronized CarritoManager getInstance() {
        if (instance == null) {
            instance = new CarritoManager();
        }
        return instance;
    }

    /**
     * Añade un producto a la lista del carrito.
     *
     * @param producto El producto a añadir.
     */
    public void addProduct(Producto producto) {
        cartItems.add(producto);
    }

    /**
     * Devuelve la lista de productos en el carrito.
     *
     * @return La lista de productos.
     */
    public List<Producto> getCartItems() {
        return cartItems;
    }

    /**
     * Limpia todos los productos del carrito.
     */
    public void clearCart() {
        cartItems.clear();
    }
}
