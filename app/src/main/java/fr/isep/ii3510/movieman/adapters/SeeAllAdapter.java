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
import fr.isep.ii3510.movieman.databinding.ItemMovie2Binding;
import fr.isep.ii3510.movieman.models.Movie;
import fr.isep.ii3510.movieman.utils.Constants;

public class SeeAllAdapter extends RecyclerView.Adapter<SeeAllAdapter.ViewHolder> {

    private final List<Movie> mMovieList;

    public SeeAllAdapter(List<Movie> movieList) {mMovieList = movieList;}

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final ItemMovie2Binding mBinding;

        public ViewHolder(ItemMovie2Binding binding){
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.ivMovie2ItemImg.getLayoutParams().width = (int) (mBinding.getRoot().getContext().getResources().getDisplayMetrics().widthPixels * 0.3);
            mBinding.ivMovie2ItemImg.getLayoutParams().height = (int) ((mBinding.getRoot().getContext().getResources().getDisplayMetrics().widthPixels * 0.3) / 0.75);

            mBinding.movie2Item.setOnClickListener(view -> {

                Intent intent = new Intent(mBinding.getRoot().getContext(), MovieActivity.class);
                intent.putExtra(Constants.MOVIE_ID, mMovieList.get(getAdapterPosition()).getId());
                mBinding.getRoot().getContext().startActivity(intent);

            });
        }

    }

    @NonNull
    @Override
    public SeeAllAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeeAllAdapter.ViewHolder(ItemMovie2Binding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(holder.mBinding.getRoot().getContext().getApplicationContext()).load(Constants.URL_IMG_LOAD_342 + mMovieList.get(position).getPoster_path())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.mBinding.ivMovie2ItemImg);

        holder.mBinding.tvMovie2ItemTitle.setText(mMovieList.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }


}
