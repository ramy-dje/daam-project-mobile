package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.apis.ProductApi;
import com.example.myapplication.apis.RetrofitProductClient;
import com.example.myapplication.models.Product;
import com.example.myapplication.session.SessionManager;
import com.example.myapplication.ReviewActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment implements ProductsAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        recyclerView = view.findViewById(R.id.products_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ProductsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Fetch products from API
        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        ProductApi api = RetrofitProductClient.getInstance().create(ProductApi.class);

        api.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Success - save to local storage and display
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.updateProducts(productList);

                    // Save products to local storage for offline access
                    new SessionManager(requireContext()).saveProducts(response.body());
                    
                    Toast.makeText(requireContext(), "Products loaded", Toast.LENGTH_SHORT).show();
                } else {
                    // ❌ Failed - try to load from local storage
                    loadProductsOffline();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                // ❌ Network error - load from local storage
                t.printStackTrace();
                loadProductsOffline();
            }
        });
    }

    private void loadProductsOffline() {
        // Load products from local storage
        List<Product> cachedProducts = new SessionManager(requireContext()).getProducts();
        
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            // Display cached products
            productList.clear();
            productList.addAll(cachedProducts);
            adapter.updateProducts(productList);
            Toast.makeText(requireContext(), "Showing offline products", Toast.LENGTH_SHORT).show();
        } else {
            // No cached products and no connection
            Toast.makeText(requireContext(), "No products available (offline)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail activity
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onReviewClick(Product product) {
        Intent intent = new Intent(requireContext(), ReviewActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        startActivity(intent);
    }
}


