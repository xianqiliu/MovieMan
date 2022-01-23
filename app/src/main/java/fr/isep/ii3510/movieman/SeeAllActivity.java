package fr.isep.ii3510.movieman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.isep.ii3510.movieman.adapters.SeeAllAdapter;
import fr.isep.ii3510.movieman.databinding.ActivitySeeAllBinding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieResponse;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import fr.isep.ii3510.movieman.utils.Constants;
import fr.isep.ii3510.movieman.utils.FastClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeAllActivity extends AppCompatActivity {

    private ActivitySeeAllBinding binding;

    private List<Movie> movieList;

    private SeeAllAdapter adapter;

    private Call<MovieResponse> call;

    private String category;
    private Integer page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySeeAllBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
        setSupportActionBar(binding.toolbarSeeAll);
        setTitle("");

        Intent intent = getIntent();
        category = intent.getStringExtra(Constants.TITLE_SEE_ALL);
        binding.tvSeeAllTitle.setText(category);

        movieList = new ArrayList<>();
        adapter = new SeeAllAdapter(movieList);
        binding.rvSeeAll.setAdapter(adapter);
        binding.rvSeeAll.setLayoutManager(new LinearLayoutManager(SeeAllActivity.this, LinearLayoutManager.VERTICAL, false));

        page = 1;
        binding.tvPage.setText(new StringBuilder().append("Page - ").append(page.toString()));

        binding.btnBackSeeAll.setOnClickListener(view1 -> onBackPressed());

        btnSetPageListener();

        displayContent(category,1);

    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(call != null) call.cancel();
    }

    private void btnSetPageListener(){

        binding.btnPre.setOnClickListener(view -> {
            if(FastClick.isFastClick()){
                page = page - 1;
                movieList.clear();
                if (page <= 1) binding.btnPre.setVisibility(View.INVISIBLE);
                displayContent(category,page);
            }else{
                Toast.makeText(SeeAllActivity.this,"Don't click so fast! Thank you! 🤬", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnNext.setOnClickListener(view -> {
            if(FastClick.isFastClick()){
                page = page + 1;
                movieList.clear();
                if (page > 1) binding.btnPre.setVisibility(View.VISIBLE);
                displayContent(category,page);
            }else{
                Toast.makeText(SeeAllActivity.this,"Don't click so fast! Thank you! 🤬", Toast.LENGTH_SHORT).show();
            }
        });

        binding.tvPage.setOnClickListener(view -> {
            if(FastClick.isFastClick()){
                binding.rvSeeAll.smoothScrollToPosition(0);
            }else{
                Toast.makeText(SeeAllActivity.this,"Don't click so fast! Thank you! 🤬", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayContent(String category, Integer page){

        //System.out.println(category);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        switch (category) {
            case Constants.NOW_PLAYING:
                call = apiService.getNowPlayingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), page, "US");
                break;
            case Constants.POPULAR:
                call = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), page, "US");
                break;
            case Constants.UPCOMING:
                call = apiService.getUpcomingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), page, "US");
                break;
            case Constants.TOP_RATED:
                call = apiService.getTopRatedMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), page, "US");
                break;
        }

        call.enqueue(new Callback<MovieResponse>(){

            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;

                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getPoster_path() != null)
                        movieList.add(movie);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) { }
        });

        binding.tvPage.setText(new StringBuilder().append("Page - ").append(page.toString()));

    }
}
