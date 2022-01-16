package fr.isep.ii3510.movieman;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.isep.ii3510.movieman.adapters.CastAdapter;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.adapters.TrailerAdapter;
import fr.isep.ii3510.movieman.databinding.ActivityMovieBinding;
import fr.isep.ii3510.movieman.helpers.ConnectivityBroadcastReceiver;
import fr.isep.ii3510.movieman.models.Cast;
import fr.isep.ii3510.movieman.models.CastResponse;
import fr.isep.ii3510.movieman.models.Genre;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieResponse;
import fr.isep.ii3510.movieman.models.Video;
import fr.isep.ii3510.movieman.models.VideoResponse;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import fr.isep.ii3510.movieman.utils.Constants;
import fr.isep.ii3510.movieman.utils.NetworkConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {

    private ActivityMovieBinding binding;

    private int mMovieId;

    private int mPosterHeight;
    private int mPosterWidth;
    private int mBackdropHeight;
    private int mBackdropWidth;

    private List<Video> mTrailerList;
    private List<Cast> mCastList;
    private List<Movie> mMovieList;

    private TrailerAdapter mAdapter1;
    private CastAdapter mAdapter2;
    private MovieAdapter mAdapter3;

    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;
    private boolean isActivityLoaded;

    private Call<Movie> mMovieDetailsCall;
    private Call<VideoResponse> mMovieTrailersCall;
    private Call<CastResponse> mMovieCreditsCall;
    private Call<MovieResponse> mSimilarMoviesCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMovieBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
        setSupportActionBar(binding.toolbar);
        setTitle("");

        Intent intent = getIntent();
        mMovieId = intent.getIntExtra(Constants.MOVIE_ID,-1);
        if(mMovieId == -1) finish();

        mPosterWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.25);
        mPosterHeight = (int) (mPosterWidth / 0.66);
        mBackdropWidth = getResources().getDisplayMetrics().widthPixels;
        mBackdropHeight = (int) (mBackdropWidth / 1.77);

        binding.layoutToolbarMovie.getLayoutParams().height = mBackdropHeight + (int) (mPosterHeight * 0.9);
        binding.ivPoster.getLayoutParams().width = mPosterWidth;
        binding.ivPoster.getLayoutParams().height = mPosterHeight;
        binding.ivBackdrop.getLayoutParams().height = mBackdropHeight;

        binding.btnBack.setOnClickListener(view1 -> onBackPressed());

        // LinearSnapHelper https://developer.android.com/reference/androidx/recyclerview/widget/LinearSnapHelper
        (new LinearSnapHelper()).attachToRecyclerView(binding.containerMovie.rvTrailer);
        mTrailerList = new ArrayList<>();
        mAdapter1 = new TrailerAdapter(mTrailerList);
        binding.containerMovie.rvTrailer.setAdapter(mAdapter1);
        binding.containerMovie.rvTrailer.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mCastList = new ArrayList<>();
        mAdapter2 = new CastAdapter(mCastList);
        binding.containerMovie.rvCast.setAdapter(mAdapter2);
        binding.containerMovie.rvCast.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mMovieList = new ArrayList<>();
        mAdapter3 = new MovieAdapter(mMovieList);
        binding.containerMovie.rvSimilarMovie.setAdapter(mAdapter3);
        binding.containerMovie.rvSimilarMovie.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        if (NetworkConnection.isConnected(MovieActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAdapter3.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isActivityLoaded && !NetworkConnection.isConnected(MovieActivity.this)) {
            // TODO
            mConnectivitySnackbar = Snackbar.make(binding.tvGenre, R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(() -> {
                mConnectivitySnackbar.dismiss();
                isActivityLoaded = true;
                loadActivity();
                isBroadcastReceiverRegistered = false;
                unregisterReceiver(mConnectivityBroadcastReceiver);
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isActivityLoaded && NetworkConnection.isConnected(MovieActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(isBroadcastReceiverRegistered) {
            isBroadcastReceiverRegistered = false;
            unregisterReceiver(mConnectivityBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMovieDetailsCall != null) mMovieDetailsCall.cancel();
        if (mMovieTrailersCall != null) mMovieTrailersCall.cancel();
        if (mSimilarMoviesCall != null) mSimilarMoviesCall.cancel();
    }

    private void loadActivity() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        mMovieDetailsCall = apiService.getMovieDetails(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mMovieDetailsCall.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, final Response<Movie> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;

                binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                   if(appBarLayout.getTotalScrollRange() + verticalOffset == 0){
                       if (response.body().getTitle() != null){
                           binding.toolbarLayout.setTitle(response.body().getTitle());
                       }else{
                           binding.toolbarLayout.setTitle("");
                       }
                   }else{
                       binding.toolbarLayout.setTitle("");
                       binding.toolbar.setVisibility(View.INVISIBLE);
                   }
                });

                Glide.with(getApplicationContext()).load(Constants.URL_IMG_LOAD_1280 + response.body().getPoster_path())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivPoster);

                Glide.with(getApplicationContext()).load(Constants.URL_IMG_LOAD_1280 + response.body().getBackdrop_path())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivBackdrop);

                binding.tvTitle.setText(response.body().getTitle());

                setDetails(response.body().getRelease_date(), response.body().getRuntime());

                setTrailers();

                binding.containerMovie.viewHorizontalLine.setVisibility(View.VISIBLE);

                setCasts();

                setSimilarMovies();

            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {

            }
        });
    }

    private void setGenres(List<Genre> genresList) {
        String genres = "";
        if (genresList != null) {
            for (int i = 0; i < genresList.size(); i++) {
                if (genresList.get(i) == null) continue;
                if (i == genresList.size() - 1) {
                    genres = genres.concat(genresList.get(i).getName());
                } else {
                    genres = genres.concat(genresList.get(i).getName() + ", ");
                }
            }
        }
        binding.tvGenre.setText(genres);
    }

    private void setYear(String releaseDateString) {
        if (releaseDateString != null && !releaseDateString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            try {
                Date releaseDate = sdf1.parse(releaseDateString);
                binding.tvYear.setText(sdf2.format(releaseDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            binding.tvYear.setText("");
        }
    }

    private void setDetails(String releaseString, Integer runtime) {
        String detailsString = "";

        if (releaseString != null && !releaseString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy");
            try {
                Date releaseDate = sdf1.parse(releaseString);
                detailsString += sdf2.format(releaseDate) + "\n";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            detailsString = "-\n";
        }

        if (runtime != null && runtime != 0) {
            if (runtime < 60) {
                detailsString += runtime + " min(s)";
            } else {
                detailsString += runtime / 60 + " hr " + runtime % 60 + " mins";
            }
        } else {
            detailsString += "-";
        }

        binding.containerMovie.tvDetail.setText(detailsString);
    }

    private void setTrailers() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mMovieTrailersCall = apiService.getMovieVideos(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mMovieTrailersCall.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (!response.isSuccessful()) {
                    mMovieTrailersCall = call.clone();
                    mMovieTrailersCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                for (Video video : response.body().getResults()) {
                    if (video != null && video.getSite() != null && video.getSite().equals("YouTube") && video.getType() != null && video.getType().equals("Trailer"))
                        mTrailerList.add(video);
                }
                if (!mTrailerList.isEmpty())
                    binding.containerMovie.tvTrailer.setVisibility(View.VISIBLE);
                mAdapter1.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {

            }
        });
    }

    private void setCasts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mMovieCreditsCall = apiService.getMovieCredits(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mMovieCreditsCall.enqueue(new Callback<CastResponse>() {
            @Override
            public void onResponse(Call<CastResponse> call, Response<CastResponse> response) {
                if (!response.isSuccessful()) {
                    mMovieCreditsCall = call.clone();
                    mMovieCreditsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getCast() == null) return;

                for (Cast castBrief : response.body().getCast()) {
                    if (castBrief != null && castBrief.getName() != null)
                        mCastList.add(castBrief);
                }

                if (!mCastList.isEmpty())
                    binding.containerMovie.tvCast.setVisibility(View.VISIBLE);
                mAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<CastResponse> call, Throwable t) {

            }
        });
    }

    private void setSimilarMovies() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mSimilarMoviesCall = apiService.getSimilarMovies(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY), 1);
        mSimilarMoviesCall.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (!response.isSuccessful()) {
                    mSimilarMoviesCall = call.clone();
                    mSimilarMoviesCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                for (Movie movieBrief : response.body().getResults()) {
                    if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPoster_path() != null)
                        mMovieList.add(movieBrief);
                }

                if (!mMovieList.isEmpty())
                    binding.containerMovie.tvSimilarMovie.setVisibility(View.VISIBLE);
                mAdapter3.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {

            }
        });
    }

}
