package fr.isep.ii3510.movieman.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.SeeAllActivity;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.databinding.FragmentMoviesBinding;
import fr.isep.ii3510.movieman.utils.ConnBroadcastReceiver;
import fr.isep.ii3510.movieman.models.GenresList;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieResponse;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import fr.isep.ii3510.movieman.utils.Constants;
import fr.isep.ii3510.movieman.utils.GenreMap;
import fr.isep.ii3510.movieman.utils.NetworkConn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ViewBinding in Fragment https://developer.android.com/topic/libraries/view-binding
public class MoviesFragment extends Fragment {

    // 1 -> Now Playing, 2 -> Popular, 3 -> Upcoming, 4 -> Top Rated

    private FragmentMoviesBinding mBinding;

    private List<Movie> mMovieList1, mMovieList2, mMovieList3, mMovieList4;
    private MovieAdapter mAdapter1, mAdapter2, mAdapter3, mAdapter4;

    private Snackbar mConnectivitySnackbar;
    private ConnBroadcastReceiver mConnBroadcastReceiver;

    private boolean isBroadcastReceiverRegistered;
    private boolean isFragmentLoaded;

    private Call<GenresList> mGenresListCall;

    private Call<MovieResponse> mCall1, mCall2, mCall3, mCall4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentMoviesBinding.inflate(inflater,container,false);
        View view = mBinding.getRoot();

        mMovieList1 = new ArrayList<>();
        mMovieList2 = new ArrayList<>();
        mMovieList3 = new ArrayList<>();
        mMovieList4 = new ArrayList<>();

        mAdapter1 = new MovieAdapter(mMovieList1);
        mAdapter2 = new MovieAdapter(mMovieList2);
        mAdapter3 = new MovieAdapter(mMovieList3);
        mAdapter4 = new MovieAdapter(mMovieList4);

        mBinding.rvNowPlaying.setAdapter(mAdapter1);
        mBinding.rvNowPlaying.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        jumpToAll(mBinding.tvNowAll, Constants.NOW_PLAYING);

        mBinding.rvPopular.setAdapter(mAdapter2);
        mBinding.rvPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        jumpToAll(mBinding.tvPopularAll, Constants.POPULAR);

        mBinding.rvUpcoming.setAdapter(mAdapter3);
        mBinding.rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        jumpToAll(mBinding.tvUpcomingAll, Constants.UPCOMING);

        mBinding.rvTopRated.setAdapter(mAdapter4);
        mBinding.rvTopRated.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        jumpToAll(mBinding.tvTopAll, Constants.TOP_RATED);

        if (NetworkConn.isConnected(requireContext())) { //https://stackoverflow.com/questions/60402490/difference-between-getcontext-and-requirecontext-when-using-fragments
            isFragmentLoaded = true;
            loadView();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter1.notifyDataSetChanged();
        mAdapter2.notifyDataSetChanged();
        mAdapter3.notifyDataSetChanged();
        mAdapter4.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isFragmentLoaded && !NetworkConn.isConnected(requireContext())) {

            mConnectivitySnackbar = Snackbar.make(requireActivity().findViewById(R.id.activity_main_fragment_container), R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnBroadcastReceiver = new ConnBroadcastReceiver(() -> {
                mConnectivitySnackbar.dismiss();
                isFragmentLoaded = true;
                loadView();
                isBroadcastReceiverRegistered = false;
                requireActivity().unregisterReceiver(mConnBroadcastReceiver);
            });

            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            requireActivity().registerReceiver(mConnBroadcastReceiver, intentFilter);

        } else if (!isFragmentLoaded && NetworkConn.isConnected(requireContext())) {
            isFragmentLoaded = true;
            loadView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            mConnectivitySnackbar.dismiss();
            isBroadcastReceiverRegistered = false;
            requireActivity().unregisterReceiver(mConnBroadcastReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGenresListCall != null) mGenresListCall.cancel();
        if (mCall1 != null) mCall1.cancel();
        if (mCall2 != null) mCall2.cancel();
        if (mCall3 != null) mCall3.cancel();
        if (mCall4 != null) mCall4.cancel();

        mBinding = null;
    }

    private void loadView() {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mBinding.progressBar.setVisibility(View.VISIBLE);

        mGenresListCall = apiService.getMovieGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY));
        mCall1 = apiService.getNowPlayingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall2 = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall3 = apiService.getUpcomingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall4 = apiService.getTopRatedMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");

        mGenresListCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(@NonNull Call<GenresList> call, @NonNull Response<GenresList> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;
                if (response.body().getGenres() == null) return;

                GenreMap.getGenresList(response.body().getGenres());

                loadSubView(mCall1,mMovieList1,mAdapter1);
                loadSubView(mCall2,mMovieList2,mAdapter2);
                loadSubView(mCall3,mMovieList3,mAdapter3);
                loadSubView(mCall4,mMovieList4,mAdapter4);

                mBinding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<GenresList> call, @NonNull Throwable t) { }
        });

    }

    // Retrofit+RecyclerView https://velmurugan-murugesan.medium.com/retrofit-android-example-with-recyclerview-870e74e5b2ff
    private void loadSubView(Call<MovieResponse> mCall, List<Movie> mList, MovieAdapter mAdapter) {

        mCall.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if(!response.isSuccessful()) {Log.d("MovieFragment", "No success"); return;}
                if(response.body() == null) {Log.d("MovieFragment", "No response"); return;}
                if(response.body().getResults() == null) {Log.d("MovieFragment", "No result"); return;}

                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getPoster_path() != null)
                        mList.add(movie);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) { }
        });

    }

    private void jumpToAll(TextView tv, String content){

        String extra = Constants.TITLE_SEE_ALL;

        tv.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(),SeeAllActivity.class);
            intent.putExtra(extra,content);
            view.getContext().startActivity(intent);
        });

    }

}
