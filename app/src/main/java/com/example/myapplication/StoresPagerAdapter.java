package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StoresPagerAdapter extends FragmentStateAdapter {
    public StoresPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new MapsFragment();
        return new ProductsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
