package fr.isep.ii3510.movieman.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.isep.ii3510.movieman.MovieActivity;
import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.SeeAllActivity;
import fr.isep.ii3510.movieman.databinding.FragmentExploreBinding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieResponse;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import fr.isep.ii3510.movieman.utils.Constants;
import fr.isep.ii3510.movieman.utils.FastClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding mBinding;

    private Movie movieRandom;

    private List<Movie> mMovieList;

    private Call<MovieResponse> mMovieResponseCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = FragmentExploreBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();

        mMovieList = new ArrayList<>();

        mBinding.itemRandom.ivMovieItemImg.getLayoutParams().width = (int) (view.getContext().getResources().getDisplayMetrics().widthPixels * 0.75);
        mBinding.itemRandom.ivMovieItemImg.getLayoutParams().height = (int) ((view.getContext().getResources().getDisplayMetrics().widthPixels * 0.75) / 0.75);

        loadView();

        btnJumpListener();

        btnChangeListener();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
    }

    private void loadView() {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mMovieResponseCall = apiService.getRandomMovie(getString(R.string.MOVIE_DB_API_KEY),new Random().nextInt(500));
        System.out.println(mMovieResponseCall.request().url());

        mMovieResponseCall.enqueue(new Callback<MovieResponse>()  {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.body() != null) {
                    System.out.println(response.body());
                    for (Movie movie : response.body().getResults()) {
                        if (movie != null && movie.getPoster_path() != null)
                            mMovieList.add(movie);
                    }

                    movieRandom = mMovieList.get(new Random().nextInt(mMovieList.size()-1));

                    Glide.with(requireContext()).load(Constants.URL_IMG_LOAD_1280 + movieRandom.getPoster_path())
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mBinding.itemRandom.ivMovieItemImg);

                    mBinding.itemRandom.tvMovieItemTitle.setText(movieRandom.getTitle());

                    System.out.println(movieRandom.getPoster_path());

                }else{
                    System.out.println("response.body() == null");
                }
            }

            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) { }
        });
    }

    private void btnChangeListener(){

        final Button btn = mBinding.btnChange;
        btn.setOnClickListener(view -> {
            if(FastClick.isFastClick()){
                loadView();
            }else {
                Toast.makeText(view.getContext(),"Don't click so fast! Thank you! 🤬", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void btnJumpListener(){

        final CardView card = mBinding.itemRandom.cardViewMovieItem;
        card.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), MovieActivity.class);
            intent.putExtra(Constants.MOVIE_ID, movieRandom.getId());
            view.getContext().startActivity(intent);
        });

    }

}
