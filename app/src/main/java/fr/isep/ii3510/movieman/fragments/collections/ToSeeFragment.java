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
import fr.isep.ii3510.movieman.databinding.FragmentToSeeBinding;
import fr.isep.ii3510.movieman.models.Movie;

public class ToSeeFragment extends Fragment {

    private FragmentToSeeBinding mBinding;

    private List<Movie> mMovieList;
    private MovieAdapter mAdapter;

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

        getToSeeList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter.notifyDataSetChanged();
    }

    private void getToSeeList(){
        List<Movie> toSeeList = new ArrayList<>();

        // Test Data
        Movie testData = new Movie(646380,"Don't Look Up","/th4E1yqsE8DGpAseLiUrI60Hf8V.jpg");

        toSeeList.add(testData);

        mMovieList.addAll(toSeeList);

        mAdapter.notifyDataSetChanged();
    }
}