package fr.isep.ii3510.movieman.fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.helpers.ConnectivityBroadcastReceiver;
import fr.isep.ii3510.movieman.databinding.FragmentMoviesBinding;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.MovieService;
import fr.isep.ii3510.movieman.models.GenresList;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.NowShowingMoviesResponse;
import fr.isep.ii3510.movieman.models.PopularMoviesResponse;
import fr.isep.ii3510.movieman.models.TopRatedMoviesResponse;
import fr.isep.ii3510.movieman.models.UpcomingMoviesResponse;
import fr.isep.ii3510.movieman.utils.GenreMap;
import fr.isep.ii3510.movieman.utils.NetworkConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ViewBinding in Fragment https://developer.android.com/topic/libraries/view-binding
public class MoviesFragment extends Fragment {

    // 1 -> Now Playing, 2 -> Popular, 3 -> Upcoming, 4 -> Top Rated

    private FragmentMoviesBinding mBinding;

    private boolean isPart1Loaded, isPart2Loaded, isPart3Loaded, isPart4Loaded;

    private List<Movie> mMovieList1, mMovieList2, mMovieList3, mMovieList4;
    private MovieAdapter mAdapter1, mAdapter2, mAdapter3, mAdapter4;

    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;

    private boolean isBroadcastReceiverRegistered;
    private boolean isFragmentLoaded;

    private Call<GenresList> mGenresListCall;

    private Call<NowShowingMoviesResponse> mCall1;
    private Call<PopularMoviesResponse> mCall2;
    private Call<UpcomingMoviesResponse> mCall3;
    private Call<TopRatedMoviesResponse> mCall4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentMoviesBinding.inflate(inflater,container,false);
        View view = mBinding.getRoot();

        mBinding.progressBar.setVisibility(View.GONE);

        isPart1Loaded = false;
        isPart2Loaded = false;
        isPart3Loaded = false;
        isPart4Loaded = false;

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

        mBinding.rvPopular.setAdapter(mAdapter2);
        mBinding.rvPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mBinding.rvUpcoming.setAdapter(mAdapter3);
        mBinding.rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mBinding.rvTopRated.setAdapter(mAdapter4);
        mBinding.rvTopRated.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
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

        if (!isFragmentLoaded && !NetworkConnection.isConnected(getContext())) {
            mConnectivitySnackbar = Snackbar.make(getActivity().findViewById(R.id.activity_main_fragment_container), R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(new ConnectivityBroadcastReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionConnected() {
                    mConnectivitySnackbar.dismiss();
                    isFragmentLoaded = true;
                    loadFragment();
                    isBroadcastReceiverRegistered = false;
                    getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
                }
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            getActivity().registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isFragmentLoaded && NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            mConnectivitySnackbar.dismiss();
            isBroadcastReceiverRegistered = false;
            getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
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

    private void loadFragment() {

        if (GenreMap.isGenresListLoaded()) {
            loadPart1();
            loadPart2();
            loadPart3();
            loadPart4();
        } else {
            MovieService apiService = ApiClient.getClient().create(MovieService.class);
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mGenresListCall = apiService.getMovieGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY));
            mGenresListCall.enqueue(new Callback<GenresList>() {
                @Override
                public void onResponse(@NonNull Call<GenresList> call, @NonNull Response<GenresList> response) {
                    if (!response.isSuccessful()) {
                        mGenresListCall = call.clone();
                        mGenresListCall.enqueue(this);
                        return;
                    }

                    if (response.body() == null) return;
                    if (response.body().getGenres() == null) return;

                    GenreMap.getGenresList(response.body().getGenres());
                    loadPart1();
                    loadPart2();
                    loadPart3();
                    loadPart4();
                }

                @Override
                public void onFailure(Call<GenresList> call, Throwable t) { }
            });
        }

    }

    private void loadPart1() {
        MovieService apiService = ApiClient.getClient().create(MovieService.class);
        mBinding.progressBar.setVisibility(View.VISIBLE);
        mCall1 = apiService.getNowShowingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall1.enqueue(new Callback<NowShowingMoviesResponse>() {
            @Override
            public void onResponse(Call<NowShowingMoviesResponse> call, Response<NowShowingMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mCall1 = call.clone();
                    mCall1.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                isPart1Loaded = true;
                checkAllDataLoaded();
                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getBackdrop_path() != null)
                        mMovieList1.add(movie);
                }
                mAdapter1.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<NowShowingMoviesResponse> call, Throwable t) { }
        });
    }

    private void loadPart2() {
        MovieService apiService = ApiClient.getClient().create(MovieService.class);
        mBinding.progressBar.setVisibility(View.VISIBLE);
        mCall2 = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall2.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mCall2 = call.clone();
                    mCall2.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                isPart2Loaded = true;
                checkAllDataLoaded();
                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getBackdrop_path() != null)
                        mMovieList2.add(movie);
                }
                mAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) { }
        });
    }

    private void loadPart3() {
        MovieService apiService = ApiClient.getClient().create(MovieService.class);
        mBinding.progressBar.setVisibility(View.VISIBLE);
        mCall3 = apiService.getUpcomingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall3.enqueue(new Callback<UpcomingMoviesResponse>() {
            @Override
            public void onResponse(Call<UpcomingMoviesResponse> call, Response<UpcomingMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mCall3 = call.clone();
                    mCall3.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                isPart3Loaded = true;
                checkAllDataLoaded();
                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getBackdrop_path() != null)
                        mMovieList3.add(movie);
                }
                mAdapter3.notifyDataSetChanged();
            }

            @Override public void onFailure(Call<UpcomingMoviesResponse> call, Throwable t) { }
        });
    }

    private void loadPart4() {
        MovieService apiService = ApiClient.getClient().create(MovieService.class);
        mBinding.progressBar.setVisibility(View.VISIBLE);
        mCall4 = apiService.getTopRatedMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mCall4.enqueue(new Callback<TopRatedMoviesResponse>() {
            @Override
            public void onResponse(Call<TopRatedMoviesResponse> call, Response<TopRatedMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mCall4 = call.clone();
                    mCall4.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                isPart4Loaded = true;
                checkAllDataLoaded();
                for (Movie movie : response.body().getResults()) {
                    if (movie != null && movie.getBackdrop_path() != null)
                        mMovieList4.add(movie);
                }
                mAdapter4.notifyDataSetChanged();
            }

            @Override public void onFailure(Call<TopRatedMoviesResponse> call, Throwable t) { }
        });
    }

    private void checkAllDataLoaded() {
        if (isPart1Loaded && isPart2Loaded && isPart3Loaded && isPart4Loaded) {
            mBinding.progressBar.setVisibility(View.GONE);
            mBinding.layoutNowShowing.setVisibility(View.VISIBLE);
            mBinding.rvNowPlaying.setVisibility(View.VISIBLE);
            mBinding.layoutPopular.setVisibility(View.VISIBLE);
            mBinding.rvPopular.setVisibility(View.VISIBLE);
            mBinding.layoutUpcoming.setVisibility(View.VISIBLE);
            mBinding.rvUpcoming.setVisibility(View.VISIBLE);
            mBinding.layoutTopRated.setVisibility(View.VISIBLE);
            mBinding.rvTopRated.setVisibility(View.VISIBLE);
        }
    }
}
