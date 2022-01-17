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
import java.util.List;

import fr.isep.ii3510.movieman.adapters.MovieAdapter;
import fr.isep.ii3510.movieman.databinding.FragmentHaveSeenBinding;
import fr.isep.ii3510.movieman.models.Movie;

public class HaveSeenFragment extends Fragment {

    private FragmentHaveSeenBinding mBinding;

    private List<Movie> mMovieList;
    private MovieAdapter mAdapter;

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

        getHaveSeenList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter.notifyDataSetChanged();
    }

    private void getHaveSeenList(){
        List<Movie> haveSeenList = new ArrayList<>();

        // Test Data
        Movie testData = new Movie(372058,"Your Name.","/q719jXXEzOoYaps6babgKnONONX.jpg");

        haveSeenList.add(testData);

        mMovieList.addAll(haveSeenList);

        mAdapter.notifyDataSetChanged();
    }

}
