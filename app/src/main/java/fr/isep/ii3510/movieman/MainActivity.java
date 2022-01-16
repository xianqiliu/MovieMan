package fr.isep.ii3510.movieman;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import fr.isep.ii3510.movieman.databinding.ActivityMainBinding;
import fr.isep.ii3510.movieman.fragments.MoviesFragment;
import fr.isep.ii3510.movieman.ui.login.LoginActivity;

// ViewBinding in Activity https://developer.android.com/topic/libraries/view-binding
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
        setSupportActionBar(binding.mainBar.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.mainBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(MainActivity.this);
        binding.navView.setCheckedItem(R.id.item_movies);

        binding.mainBar.toolbar.setTitleTextColor(getColor(R.color.white));
        binding.mainBar.toolbar.setTitle(R.string.movies);

        setFragment(new MoviesFragment());
    }

    @Override
    public void onStart() {
        super.onStart();
//        mAuth = FirebaseAuth.getInstance();
//
//        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//        startActivity(intent);

//        if(mAuth.getCurrentUser() == null){
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        binding.drawerLayout.closeDrawer(GravityCompat.START);

        if(id == R.id.item_movies){
            binding.mainBar.toolbar.setTitle(R.string.movies);
            setFragment(new MoviesFragment());
            return true;
        }else if(id == R.id.item_to_see){
            binding.mainBar.toolbar.setTitle(R.string.to_see);
            return true;
        }else if(id == R.id.item_have_seen){
            binding.mainBar.toolbar.setTitle(R.string.have_seen);
            return true;
        }

        return false;
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_main_fragment_container, fragment);
        fragmentTransaction.commit();
    }

}