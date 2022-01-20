package fr.isep.ii3510.movieman.fragments.collections;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.databinding.FragmentHaveSeenBinding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieCollections;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaveSeenFragment extends Fragment {

    private FragmentHaveSeenBinding mBinding;

    private List<Movie> mMovieList;
    private MovieAdapter mAdapter;

    private HashMap<String, Object> mp;

    private Boolean firstLoad = true;

    public static HaveSeenFragment getInstance(){
        return new HaveSeenFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHaveSeenBinding.inflate(inflater,container,false);
        View view = mBinding.getRoot();

        mMovieList = new ArrayList<>();
        mAdapter = new MovieAdapter(mMovieList);
        mBinding.rvHaveSeen.setAdapter(mAdapter);
        mBinding.rvHaveSeen.setLayoutManager(new GridLayoutManager(getContext(),3));

        mp = MovieCollections.haveSeenMap;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!firstLoad) {
            mMovieList.clear();
        }
        getHaveSeenList();

        firstLoad = false;

        mAdapter.notifyDataSetChanged();
    }

    private void getHaveSeenList(){
        //List<Movie> haveSeenList = new ArrayList<>();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        for(String collectOrder : mp.keySet()){
            if(!collectOrder.equals("num")){
                String mIdString = Objects.requireNonNull(mp.get(collectOrder)).toString();
                System.out.println(collectOrder + ": "+ mIdString);

                Call<Movie> movieCall;
                int mId = Integer.parseInt(mIdString);
                movieCall = apiService.getMovieDetails(mId, getResources().getString(R.string.MOVIE_DB_API_KEY));
                movieCall.enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                        if (!response.isSuccessful()) return;
                        if (response.body() == null) return;

                        mMovieList.add(new Movie(mId,response.body().getTitle(),response.body().getPoster_path()));
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) { }
                });

            }

        }

        // Test Data
/*
        Movie testData = new Movie(372058,"Your Name.","/q719jXXEzOoYaps6babgKnONONX.jpg");
        haveSeenList.add(testData);
        mMovieList.addAll(haveSeenList);
        mAdapter.notifyDataSetChanged();
*/
    }

}
