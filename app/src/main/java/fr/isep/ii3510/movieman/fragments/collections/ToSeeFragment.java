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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.databinding.FragmentToSeeBinding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieCollections;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToSeeFragment extends Fragment {

    private FragmentToSeeBinding mBinding;

    private List<Movie> mMovieList;
    private MovieAdapter mAdapter;

    private HashMap<String, Object> mp;

    private Boolean firstLoad = true;

    public static ToSeeFragment getInstance(){
        return new ToSeeFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentToSeeBinding.inflate(inflater,container,false);
        View view = mBinding.getRoot();

        mMovieList = new ArrayList<>();
        mAdapter = new MovieAdapter(mMovieList);
        mBinding.rvToSee.setAdapter(mAdapter);
        mBinding.rvToSee.setLayoutManager(new GridLayoutManager(getContext(),3));

        mp = MovieCollections.toSeeMap;

        getToSeeList();

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
            getToSeeList();
        }else{
            firstLoad = false;
        }

        mAdapter.notifyDataSetChanged();
    }

    private void getToSeeList(){

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        List<Integer> seeList = new ArrayList<>();
        for(String collectOrderStr : mp.keySet()) {
            if (!collectOrderStr.equals("num")) {
                seeList.add(Integer.parseInt(collectOrderStr));
            }
        }

        seeList.sort(Collections.reverseOrder());
        System.out.println(seeList);

        for(Integer seeOrder : seeList){

                String mIdString = Objects.requireNonNull(mp.get(seeOrder.toString())).toString();
                System.out.println(seeOrder + ": "+ mIdString);

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


        // Test data -> Movie testData = new Movie(646380,"Don't Look Up","/th4E1yqsE8DGpAseLiUrI60Hf8V.jpg");

    }
}