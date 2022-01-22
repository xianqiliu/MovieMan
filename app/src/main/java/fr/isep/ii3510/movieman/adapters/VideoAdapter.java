package fr.isep.ii3510.movieman.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import fr.isep.ii3510.movieman.databinding.ItemVideoBinding;
import fr.isep.ii3510.movieman.models.Video;
import fr.isep.ii3510.movieman.utils.Constants;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<Video> mVideoList;

    public VideoAdapter(List<Video> videoList) { mVideoList = videoList; }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private final ItemVideoBinding itemVideoBinding;

        public VideoViewHolder(ItemVideoBinding binding){
            super(binding.getRoot());
            this.itemVideoBinding = binding;

            itemVideoBinding.cardViewVideo.setOnClickListener(view -> {
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_YOUTUBE_WATCH + mVideoList.get(getAdapterPosition()).getKey()));
                itemVideoBinding.getRoot().getContext().startActivity(youtubeIntent);
            });
        }

    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        Glide.with(holder.itemVideoBinding.getRoot().getContext().getApplicationContext())
                .load(Constants.URL_YOUTUBE_THUMBNAIL + mVideoList.get(position).getKey() + Constants.URL_YOUTUBE_THUMBNAIL_QUALITY)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.itemVideoBinding.ivVideoImg);

        if (mVideoList.get(position).getName() != null){
            holder.itemVideoBinding.tvVideoName.setText(mVideoList.get(position).getName());
        } else {
            holder.itemVideoBinding.tvVideoName.setText("");
        }
    }

    @Override
    public int getItemCount() { return mVideoList.size(); }

}
