package fr.isep.ii3510.movieman.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import fr.isep.ii3510.movieman.databinding.ItemCastBinding;
import fr.isep.ii3510.movieman.models.Cast;
import fr.isep.ii3510.movieman.utils.Constants;
import lombok.NonNull;

public class MovieCastAdapter extends RecyclerView.Adapter<MovieCastAdapter.CastViewHolder> {

    private List<Cast> mCastList;

    public MovieCastAdapter(List<Cast> castList) { mCastList = castList; }

    public static class CastViewHolder extends RecyclerView.ViewHolder {

        private ItemCastBinding itemBinding;

        public CastViewHolder(ItemCastBinding binding) {
            super(binding.getRoot());
            this.itemBinding = binding;

            itemBinding.ivCast.setOnClickListener(view-> Toast.makeText(itemBinding.getRoot().getContext(), "test", Toast.LENGTH_LONG).show());

        }

    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CastViewHolder(ItemCastBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(CastViewHolder holder, int position){

        Glide.with(holder.itemBinding.getRoot().getContext().getApplicationContext())
                .load(Constants.URL_IMG_LOAD + mCastList.get(position).getProfile_path())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.itemBinding.ivCast);

        if (mCastList.get(position).getName() != null){
            holder.itemBinding.tvCastName.setText(mCastList.get(position).getName());
        } else {
            holder.itemBinding.tvCastName.setText("");
        }

        if (mCastList.get(position).getName() != null){
            holder.itemBinding.tvCastCharacter.setText(mCastList.get(position).getName());
        } else {
            holder.itemBinding.tvCastCharacter.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return mCastList.size();
    }

}
