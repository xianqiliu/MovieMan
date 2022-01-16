package fr.isep.ii3510.movieman.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import fr.isep.ii3510.movieman.MovieActivity;
import fr.isep.ii3510.movieman.databinding.ItemMovieBinding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.utils.Constants;

// ViewBinding in RecycleView adapter https://stackoverflow.com/questions/60664346/what-is-the-right-way-of-android-view-binding-in-the-recyclerview-adapter-class
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> mMovieList;

    public MovieAdapter(List<Movie> movieList) {
        mMovieList = movieList;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        private ItemMovieBinding itemBinding;

        public MovieViewHolder(ItemMovieBinding binding){
            super(binding.getRoot());
            this.itemBinding = binding;

            // poster -> 3:4
            itemBinding.ivMovieItemImg.getLayoutParams().width = (int) (itemBinding.getRoot().getContext().getResources().getDisplayMetrics().widthPixels * 0.3);
            itemBinding.ivMovieItemImg.getLayoutParams().height = (int) ((itemBinding.getRoot().getContext().getResources().getDisplayMetrics().widthPixels * 0.3) / 0.75);

            itemBinding.cardViewMovieItem.setOnClickListener(view -> {
                Intent intent = new Intent(itemBinding.getRoot().getContext(), MovieActivity.class);
                intent.putExtra(Constants.MOVIE_ID, mMovieList.get(getAdapterPosition()).getId());
                itemBinding.getRoot().getContext().startActivity(intent);
            });

        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       return new MovieViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Glide.with(holder.itemBinding.getRoot().getContext().getApplicationContext()).load(Constants.URL_IMG_LOAD_342 + mMovieList.get(position).getPoster_path())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.itemBinding.ivMovieItemImg);

        if (mMovieList.get(position).getTitle() != null)
            holder.itemBinding.tvMovieItemTitle.setText(mMovieList.get(position).getTitle());
        else
            holder.itemBinding.tvMovieItemTitle.setText("");

    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

}
