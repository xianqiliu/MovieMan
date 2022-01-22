package fr.isep.ii3510.movieman;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import fr.isep.ii3510.movieman.adapters.CastAdapter;
import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.adapters.VideoAdapter;
import fr.isep.ii3510.movieman.databinding.ActivityMovieBinding;
import fr.isep.ii3510.movieman.models.Cast;
import fr.isep.ii3510.movieman.models.CastResponse;
import fr.isep.ii3510.movieman.models.Genre;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.models.MovieCollections;
import fr.isep.ii3510.movieman.models.MovieResponse;
import fr.isep.ii3510.movieman.models.Video;
import fr.isep.ii3510.movieman.models.VideoResponse;
import fr.isep.ii3510.movieman.services.ApiClient;
import fr.isep.ii3510.movieman.services.ApiService;
import fr.isep.ii3510.movieman.utils.ConnBroadcastReceiver;
import fr.isep.ii3510.movieman.utils.Constants;
import fr.isep.ii3510.movieman.utils.NetworkConn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {

    private ActivityMovieBinding binding;

    private int mMovieId;

    private List<Video> mVideoList;
    private List<Cast> mCastList;
    private List<Movie> mSimilarList;

    private VideoAdapter mVideoAdapter;
    private CastAdapter mCastAdapter;
    private MovieAdapter mSimilarAdapter;

    private Snackbar mConnectivitySnackbar;
    private ConnBroadcastReceiver mConnBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;
    private boolean isActivityLoaded;

    private Call<Movie> mMovieCall;
    private Call<VideoResponse> mVideoCall;
    private Call<CastResponse> mCastCall;
    private Call<MovieResponse> mSimilarCall;

    // for Collection data in Firebase
    int flagToSee;
    int flagHaveSeen;
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMovieBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
        setSupportActionBar(binding.toolbar);
        setTitle("");

        Intent intent = getIntent();
        mMovieId = intent.getIntExtra(Constants.MOVIE_ID, -1);
        if (mMovieId == -1) finish();

        int pW = (int) (getResources().getDisplayMetrics().widthPixels * 0.25);
        int pH = (int) (pW / 0.75);
        int bgW = getResources().getDisplayMetrics().widthPixels;
        int bgH = bgW / 2;

        binding.layoutToolbarMovie.getLayoutParams().height = bgH + (int) (pH * 0.9);
        binding.ivPoster.getLayoutParams().width = pW;
        binding.ivPoster.getLayoutParams().height = pH;
        binding.ivBackdrop.getLayoutParams().height = bgH;

        binding.btnBack.setOnClickListener(view1 -> onBackPressed());

        // LinearSnapHelper https://developer.android.com/reference/androidx/recyclerview/widget/LinearSnapHelper
        (new LinearSnapHelper()).attachToRecyclerView(binding.containerMovie.rvTrailer);

        mVideoList = new ArrayList<>();
        mCastList = new ArrayList<>();
        mSimilarList = new ArrayList<>();

        mVideoAdapter = new VideoAdapter(mVideoList);
        binding.containerMovie.rvTrailer.setAdapter(mVideoAdapter);
        binding.containerMovie.rvTrailer.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mCastAdapter = new CastAdapter(mCastList);
        binding.containerMovie.rvCast.setAdapter(mCastAdapter);
        binding.containerMovie.rvCast.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mSimilarAdapter = new MovieAdapter(mSimilarList);
        binding.containerMovie.rvSimilarMovie.setAdapter(mSimilarAdapter);
        binding.containerMovie.rvSimilarMovie.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.HORIZONTAL, false));

        if (NetworkConn.isConnected(MovieActivity.this)) {
            isActivityLoaded = true;
            displayContent();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mSimilarAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isActivityLoaded && !NetworkConn.isConnected(MovieActivity.this)) {
            // TODO
            mConnectivitySnackbar = Snackbar.make(binding.tvGenre, R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnBroadcastReceiver = new ConnBroadcastReceiver(() -> {
                mConnectivitySnackbar.dismiss();
                isActivityLoaded = true;
                displayContent();
                isBroadcastReceiverRegistered = false;
                unregisterReceiver(mConnBroadcastReceiver);
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            registerReceiver(mConnBroadcastReceiver, intentFilter);
        } else if (!isActivityLoaded && NetworkConn.isConnected(MovieActivity.this)) {
            isActivityLoaded = true;
            displayContent();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            isBroadcastReceiverRegistered = false;
            unregisterReceiver(mConnBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMovieCall != null) mMovieCall.cancel();
        if (mVideoCall != null) mVideoCall.cancel();
        if (mSimilarCall != null) mSimilarCall.cancel();
    }

    private void displayContent() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        mMovieCall = apiService.getMovieDetails(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mMovieCall.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull final Response<Movie> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;

                // for Collection Data in firebase
                mMovie = new Movie(mMovieId, response.body().getTitle(), response.body().getPoster_path());

                // bar
                binding.movieBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                    if (appBarLayout.getTotalScrollRange() + verticalOffset == 0) {
                        if (response.body().getTitle() != null) {
                            binding.layoutToolbar.setCollapsedTitleTextColor(getColor(R.color.white));
                            binding.layoutToolbar.setTitle(response.body().getTitle());
                        } else {
                            binding.layoutToolbar.setTitle("");
                        }
                    } else {
                        binding.layoutToolbar.setTitle("");
                        binding.toolbar.setVisibility(View.INVISIBLE);
                    }
                });

                // poster
                assert response.body() != null;
                Glide.with(getApplicationContext()).load(Constants.URL_IMG_LOAD_1280 + response.body().getPoster_path())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivPoster);

                // backdrop
                Glide.with(getApplicationContext()).load(Constants.URL_IMG_LOAD_1280 + response.body().getBackdrop_path())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivBackdrop);

                // bar detail: title, released date, runtime,
                binding.tvTitle.setText(response.body().getTitle());
                getTypeList(response.body().getGenres());
                getDateTime(response.body().getRelease_date(), response.body().getRuntime());

                // other details: btnToSee, btnHaveSeen, rating, overview
                btnCollectListener();

                if (response.body().getVote_average() != null && response.body().getVote_average() != 0) {
                    binding.containerMovie.layoutRating.setVisibility(View.VISIBLE);
                    binding.containerMovie.tvRating.setText(String.format(Locale.ENGLISH, "%.1f", response.body().getVote_average()));
                }

                if (response.body().getOverview() != null && !response.body().getOverview().trim().isEmpty()) {
                    binding.containerMovie.tvOverview.setText(response.body().getOverview());
                    binding.containerMovie.tvReadMore.setVisibility(View.VISIBLE);
                    binding.containerMovie.tvReadMore.setOnClickListener(view -> {
                        binding.containerMovie.tvOverview.setMaxLines(Integer.MAX_VALUE);
                        binding.containerMovie.tvReadMore.setVisibility(View.GONE);
                    });
                }

                displayTrailer();
                displayCast();
                displaySimilar();

            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
            }
        });
    }

    // TODO
    private void btnCollectListener() {

        // Don' forget
        // 1. check if the user's toSeeList contains the id of this movie
        // 2. the set flagToSee as 0 (not contained) or 1 (contain)

        final Button btnToSee = binding.containerMovie.btnToSee;
        final Button btnHaveSeen = binding.containerMovie.btnHaveSeen;

        // check if it's in toSee or haveSeen list
        if(MovieCollections.toSeeMapRe.containsKey(String.valueOf(mMovie.getId()))){
            btnToSee.setBackgroundColor(getColor(R.color.teal_200));
            flagToSee = 1;
        }
        if(MovieCollections.haveSeenMapRe.containsKey(String.valueOf(mMovie.getId()))){
            btnHaveSeen.setBackgroundColor(getColor(R.color.teal_200));
            flagHaveSeen = 1;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        btnToSee.setOnClickListener(view -> {
            if (flagToSee == 0) {
                int index = ((Long) Objects.requireNonNull(MovieCollections.toSeeMap.get("num"))).intValue() + 1;
                HashMap<String, Object> hm = new HashMap<>();
                hm.put(String.valueOf(index), mMovie.getId());
                hm.put("num", index);

                if (user != null) {
                    db.collection(user.getUid()).document("toSee").update(hm).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            btnToSee.setBackgroundColor(getColor(R.color.teal_200));
                            flagToSee = 1;
                            Toast.makeText(getApplicationContext(), "added to your toSee list", Toast.LENGTH_SHORT).show();
                            MovieCollections.toSeeMap.put("num", (long) index);
                            MovieCollections.toSeeMap.put(String.valueOf(index), mMovie.getId());
                            MovieCollections.toSeeMapRe.put(String.valueOf(mMovie.getId()), String.valueOf(index));

                        } else {
                            Toast.makeText(getApplicationContext(), "failed to add, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            } else {
                if (user != null) {
                    db.collection(user.getUid()).document("toSee").update(Objects.requireNonNull(MovieCollections.toSeeMapRe.get(String.valueOf(mMovie.getId()))), FieldValue.delete())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            btnToSee.setBackgroundColor(getColor(R.color.purple_500));
                            flagToSee = 0;
                            MovieCollections.toSeeMap.remove(MovieCollections.toSeeMapRe.get(String.valueOf(mMovie.getId())));
                            MovieCollections.toSeeMapRe.remove(String.valueOf(mMovie.getId()));
                            Toast.makeText(getApplicationContext(), "removed from your toSee list", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "failed to remove, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

//            Toast.makeText(MovieActivity.this, mMovie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        btnHaveSeen.setOnClickListener(view -> {
            if (flagHaveSeen == 0) {
                int index = ((Long) Objects.requireNonNull(MovieCollections.haveSeenMap.get("num"))).intValue() + 1;
                HashMap<String, Object> hm = new HashMap<>();
                hm.put(String.valueOf(index), mMovie.getId());
                hm.put("num", index);

                assert user != null;
                db.collection(user.getUid()).document("haveSeen").update(hm).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        btnHaveSeen.setBackgroundColor(getColor(R.color.teal_200));
                        flagHaveSeen = 1;
                        Toast.makeText(getApplicationContext(), "added to your haveSeen list", Toast.LENGTH_SHORT).show();
                        MovieCollections.haveSeenMap.put("num", (long) index);
                        MovieCollections.haveSeenMap.put(String.valueOf(index), mMovie.getId());
                        MovieCollections.haveSeenMapRe.put(String.valueOf(mMovie.getId()), String.valueOf(index));

                    } else {
                        Toast.makeText(getApplicationContext(), "failed to add, please try again", Toast.LENGTH_SHORT).show();
                    }
                });


            } else {
                if (user != null) {
                    db.collection(user.getUid()).document("haveSeen").update(Objects.requireNonNull(MovieCollections.haveSeenMapRe.get(String.valueOf(mMovie.getId()))), FieldValue.delete())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    btnHaveSeen.setBackgroundColor(getColor(R.color.purple_500));
                                    flagHaveSeen = 0;
                                    MovieCollections.haveSeenMap.remove(MovieCollections.haveSeenMapRe.get(String.valueOf(mMovie.getId())));
                                    MovieCollections.haveSeenMapRe.remove(String.valueOf(mMovie.getId()));
                                    Toast.makeText(getApplicationContext(), "removed from your haveSeen list", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(getApplicationContext(), "failed to remove, please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
//            Toast.makeText(MovieActivity.this, mMovie.getTitle(), Toast.LENGTH_SHORT).show();
        });


    }

    private void getTypeList(List<Genre> genresList) {
        String genres = "Type: ";
        if (genresList != null) {
            for (int i = 0; i < genresList.size(); i++) {
                if (genresList.get(i) == null) continue;
                if (i == genresList.size() - 1) genres = genres.concat(genresList.get(i).getName());
                else genres = genres.concat(genresList.get(i).getName() + ", ");
            }
        }
        binding.tvGenre.setText(genres);
    }

    private void getDateTime(String date, Integer runtime) {
        String res = "";

        if (date != null && !date.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            try {
                Date releaseDate = sdf1.parse(date);
                if (releaseDate != null) res += "Release Date: " + sdf2.format(releaseDate) + "\n";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            res = "-\n";
        }

        if (runtime != null && runtime != 0) res += "Runtime: " + runtime + " min";
        else res += "-";

        binding.tvDetail.setText(res);
    }

    private void displayTrailer() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mVideoCall = apiService.getMovieVideos(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mVideoCall.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoResponse> call, @NonNull Response<VideoResponse> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                for (Video video : response.body().getResults()) {
                    if (video != null && video.getSite() != null && video.getSite().equals("YouTube") && video.getType() != null && video.getType().equals("Trailer"))
                        mVideoList.add(video);
                }
                binding.containerMovie.tvTrailer.setVisibility(View.VISIBLE);

                mVideoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void displayCast() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mCastCall = apiService.getMovieCredits(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mCastCall.enqueue(new Callback<CastResponse>() {
            @Override
            public void onResponse(@NonNull Call<CastResponse> call, @NonNull Response<CastResponse> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;
                if (response.body().getCast() == null) return;

                mCastList.addAll(response.body().getCast());
                binding.containerMovie.tvCast.setVisibility(View.VISIBLE);

                mCastAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<CastResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void displaySimilar() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        mSimilarCall = apiService.getSimilarMovies(mMovieId, getResources().getString(R.string.MOVIE_DB_API_KEY), 1);
        mSimilarCall.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                mSimilarList.addAll(response.body().getResults());
                binding.containerMovie.tvSimilarMovie.setVisibility(View.VISIBLE);

                mSimilarAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
            }
        });
    }

}
