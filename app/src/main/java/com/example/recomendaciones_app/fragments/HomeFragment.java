package com.example.recomendaciones_app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.OnboardingActivity;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.database.ProductoService;
import com.example.recomendaciones_app.data.model.Category;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.data.model.ProductoResponse;
import com.example.recomendaciones_app.data.network.ApiClient;
import com.example.recomendaciones_app.data.network.ApiService;
import com.example.recomendaciones_app.data.network.N8nService;
import com.example.recomendaciones_app.ui.activities.ProductoDetalleActivity;
import com.example.recomendaciones_app.ui.adapters.OfertaAdapter;
import com.example.recomendaciones_app.ui.adapters.ProductoAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductoAdapter.OnProductoActionListener, OfertaAdapter.OnOfertaClickListener {

    private static final String TAG = "HomeFragment";

    private static final int PAGE_SIZE = 30;
    private static final long DEBOUNCE_DELAY = 300;
    private static final double MIN_DISCOUNT_FOR_DEAL = 15.0;
    private static final String FOR_YOU_SLUG = "for-you-slug";

    private RecyclerView recyclerViewProductos, recyclerViewOfertas;
    private ProductoAdapter productoAdapter;
    private OfertaAdapter ofertaAdapter;
    private List<Producto> listaProductos = new ArrayList<>();
    private List<Producto> listaOfertas = new ArrayList<>();
    private EditText searchInput;
    private ChipGroup chipGroupCategories;
    private ProgressBar progressBar, progressBarBottom;

    private ApiService apiService;
    private ProductoService productoService;
    private N8nService n8nService; // nueva variable para el servicio n8n

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    private boolean isLoading = false;
    private int skip = 0;
    private int totalProducts = 0;
    private String currentSearchQuery = null;
    private String currentCategory = null; 

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: start");
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);

            apiService = ApiClient.getClient().create(ApiService.class);
            n8nService = new N8nService(); // inicializar el servicio N8n justo después de apiService
            productoService = new ProductoService(getContext());

            recyclerViewProductos = view.findViewById(R.id.recyclerViewProductos);
            recyclerViewOfertas = view.findViewById(R.id.recyclerViewOfertas);
            searchInput = view.findViewById(R.id.searchInput);
            chipGroupCategories = view.findViewById(R.id.chipGroupHomeCategories);
            progressBar = view.findViewById(R.id.progressBar);
            progressBarBottom = view.findViewById(R.id.progressBarBottom);

            setupRecyclerView();
            setupOfertasRecyclerView();
            setupSearch();
            setupCategoryChips();
            loadInitialData();

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView", e);
            try {
                java.io.File outFile = new java.io.File(getContext().getFilesDir(), "crash_log.txt");
                java.io.FileWriter fw = new java.io.FileWriter(outFile, true);
                java.io.PrintWriter pw = new java.io.PrintWriter(fw);
                pw.println("--- HomeFragment onCreateView exception: " + new java.util.Date() + " ---");
                e.printStackTrace(pw);
                pw.println();
                pw.close();
                fw.close();
            } catch (Exception ex) {
                Log.e(TAG, "Error escribiendo crash_log.txt desde HomeFragment", ex);
            }
            // Devolver una vista vacía segura para evitar que la app se cierre por la excepción
            View safeView = new View(getContext());
            return safeView;
        }
    }
    
    private void setupOfertasRecyclerView() {
        recyclerViewOfertas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        ofertaAdapter = new OfertaAdapter(getContext(), listaOfertas, this);
        recyclerViewOfertas.setAdapter(ofertaAdapter);
    }

    private void setupRecyclerView() {
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        productoAdapter = new ProductoAdapter(getContext(), listaProductos, this);
        recyclerViewProductos.setAdapter(productoAdapter);

        recyclerViewProductos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (currentCategory != null && currentCategory.equals(FOR_YOU_SLUG)) return; 
                
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0 && totalItemCount < totalProducts) {
                    loadMoreProducts();
                }
            }
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { searchHandler.removeCallbacks(searchRunnable); }
            @Override public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString();
                searchRunnable = () -> resetAndFetch();
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });
    }

    private void setupCategoryChips() {
        if (!isNetworkAvailable()) return;
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                // Anotación: CORRECCIÓN. Comprueba si el fragmento sigue adjunto antes de actuar.
                if (getContext() == null || !isAdded()) {
                    return; // Evita el crash si el fragmento se ha destruido.
                }
                if (response.isSuccessful() && response.body() != null) {
                    chipGroupCategories.removeAllViews();
                    addChip(getString(R.string.category_todos), "all-slug", true);
                    addChip(getString(R.string.category_para_ti), FOR_YOU_SLUG, false);
                    for (Category category : response.body()) {
                        addChip(category.getName(), category.getSlug(), false);
                    }
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {
                // Anotación: También es buena práctica comprobarlo en el onFailure.
                 if (getContext() == null || !isAdded()) {
                    return;
                }
            }
        });

        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null && chip.getTag() != null) {
                currentCategory = chip.getTag().toString();
                resetAndFetch();
            }
        });
    }

    private void addChip(String name, String slug, boolean isChecked) {
        Chip chip = new Chip(getContext());
        chip.setText(name);
        chip.setTag(slug);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chip.setChipMinHeight(96f);
        chipGroupCategories.addView(chip);
    }

    private void loadInitialData() {
        if (isNetworkAvailable()) {
            resetAndFetch();
        } else {
            Toast.makeText(getContext(), R.string.no_hay_conexion, Toast.LENGTH_LONG).show();
            loadFromDatabase();
        }
    }

    private void resetAndFetch() {
        skip = 0;
        listaProductos.clear();
        if(productoAdapter != null) productoAdapter.notifyDataSetChanged();
        fetchProducts();
    }

    private void loadMoreProducts() {
        skip += PAGE_SIZE;
        fetchProducts();
    }

    private void fetchProducts() {
        if (isLoading) return;
        isLoading = true;
        showProgress(true, skip > 0);

        if (FOR_YOU_SLUG.equals(currentCategory)) {
            fetchForYouProducts();
        } else {
            fetchNormalProducts();
        }
    }

    private void fetchNormalProducts() {
        Call<ProductoResponse> call;
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            call = apiService.searchProducts(currentSearchQuery);
        } else if (currentCategory != null && !currentCategory.equals("all-slug")) {
            call = apiService.getProductsByCategory(currentCategory);
        } else {
            call = apiService.getProducts(PAGE_SIZE, skip);
        }

        call.enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    ProductoResponse pr = response.body();
                    totalProducts = pr.getTotal();
                    
                    // Enviar datos al webhook si la lista no es nula ni vacía
                    if (pr.getProducts() != null && !pr.getProducts().isEmpty() && n8nService != null) {
                        n8nService.enviarDatos(pr.getProducts());
                    }

                    if (skip == 0) {
                        productoAdapter.setProductos(pr.getProducts());
                        extractAndSetOfertas(pr.getProducts());
                    } else {
                        productoAdapter.addProductos(pr.getProducts());
                    }
                    
                    databaseExecutor.execute(() -> productoService.insertarProductos(pr.getProducts()));
                } 
                showProgress(false, skip > 0);
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ProductoResponse> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                showProgress(false, skip > 0);
                isLoading = false;
            }
        });
    }

    private void extractAndSetOfertas(List<Producto> productos) {
        listaOfertas.clear();
        for (Producto p : productos) {
            if (p.getDiscountPercentage() > MIN_DISCOUNT_FOR_DEAL) {
                listaOfertas.add(p);
            }
        }
        ofertaAdapter.notifyDataSetChanged();
    }

    private void fetchForYouProducts() {
        SharedPreferences prefs = getContext().getSharedPreferences(OnboardingActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> userCategories = prefs.getStringSet(OnboardingActivity.KEY_USER_CATEGORIES, null);

        if (userCategories == null || userCategories.isEmpty()) {
            Toast.makeText(getContext(), "Aún no has seleccionado tus intereses.", Toast.LENGTH_LONG).show();
            showProgress(false, false);
            isLoading = false;
            return;
        }

        List<Producto> forYouProducts = new ArrayList<>();
        AtomicInteger callsToMake = new AtomicInteger(userCategories.size());

        for (String categorySlug : userCategories) {
            apiService.getProductsByCategory(categorySlug).enqueue(new Callback<ProductoResponse>() {
                @Override
                public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        forYouProducts.addAll(response.body().getProducts());
                    }
                    if (callsToMake.decrementAndGet() == 0) {
                        if (getContext() == null || !isAdded()) return;
                        finalizeForYouFetch(forYouProducts);
                    }
                }

                @Override
                public void onFailure(Call<ProductoResponse> call, Throwable t) {
                    if (callsToMake.decrementAndGet() == 0) {
                         if (getContext() == null || !isAdded()) return;
                        finalizeForYouFetch(forYouProducts);
                    }
                }
            });
        }
    }

    private void finalizeForYouFetch(List<Producto> products) {
        // Enviar datos al webhook si la lista no es nula ni vacía
        if (products != null && !products.isEmpty() && n8nService != null) {
            n8nService.enviarDatos(products);
        }

        Collections.shuffle(products);
        productoAdapter.setProductos(products);
        totalProducts = products.size(); 
        showProgress(false, false);
        isLoading = false;
    }

    private void loadFromDatabase() {
        databaseExecutor.execute(() -> {
            List<Producto> productosDB = productoService.obtenerTodos();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> productoAdapter.setProductos(productosDB));
            }
        });
    }

    private void showProgress(boolean show, boolean isBottom) {
        if (isBottom) {
            progressBarBottom.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    @Override
    public void onMeGustaClick(Producto producto, int position) {
        if (producto == null) {
            Log.w("HomeFragment", "onMeGustaClick: producto es null, ignorando");
            return;
        }

        boolean nuevoEstado = producto.getMeGusta() == 0;
        producto.setMeGusta(nuevoEstado ? 1 : 0);
        // Asegurarse de que la posición es válida antes de notificar el cambio
        if (productoAdapter != null) {
            if (position >= 0 && position < productoAdapter.getItemCount()) {
                productoAdapter.notifyItemChanged(position);
            } else {
                productoAdapter.notifyDataSetChanged();
            }
        }

        // Actualizar en la base de datos de forma defensiva usando el nuevo método
        Producto productoParaActualizar = producto;
        databaseExecutor.execute(() -> {
            try {
                if (productoService != null) {
                    productoService.actualizarProductoEstado(productoParaActualizar);
                } else {
                    Log.w("HomeFragment", "productoService es null, no se puede actualizar la BD");
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error actualizando estado de producto", e);
            }
        });
    }

    @Override
    public void onVerMasClick(Producto producto) {
        Intent intent = new Intent(getActivity(), ProductoDetalleActivity.class);
        intent.putExtra(ProductoDetalleActivity.EXTRA_PRODUCTO, producto);
        startActivity(intent);
    }

    @Override
    public void onOfertaClick(Producto producto) {
        onVerMasClick(producto); 
    }
}
