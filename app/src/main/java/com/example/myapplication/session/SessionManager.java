package com.example.myapplication.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.models.Client;
import com.example.myapplication.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_CLIENT_JSON = "client_json";
    private static final String KEY_PRODUCTS_JSON = "products_json";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveClient(Client client) {
        if (client == null) return;
        String json = gson.toJson(client);
        prefs.edit().putString(KEY_CLIENT_JSON, json).apply();
    }

    public Client getClient() {
        String json = prefs.getString(KEY_CLIENT_JSON, null);
        if (json == null) return null;
        return gson.fromJson(json, Client.class);
    }

    public boolean isLoggedIn() {
        return getClient() != null;
    }

    // Product storage for offline mode
    public void saveProducts(List<Product> products) {
        if (products == null) return;
        String json = gson.toJson(products);
        prefs.edit().putString(KEY_PRODUCTS_JSON, json).apply();
    }

    public List<Product> getProducts() {
        String json = prefs.getString(KEY_PRODUCTS_JSON, null);
        if (json == null) return null;
        Type listType = new TypeToken<List<Product>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

