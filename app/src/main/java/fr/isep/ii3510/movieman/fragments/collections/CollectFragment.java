package fr.isep.ii3510.movieman.fragments.collections;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.isep.ii3510.movieman.R;
import fr.isep.ii3510.movieman.adapters.CollectViewPagerAdapter;
import fr.isep.ii3510.movieman.databinding.FragmentCollectBinding;

// Tab layout tutorial https://www.youtube.com/watch?v=uc5o7x0P2OU&ab_channel=larntech
public class CollectFragment extends Fragment {

    private FragmentCollectBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = FragmentCollectBinding.inflate(inflater,container,false);
        View view = mBinding.getRoot();

        getTabs();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
    }

    public void getTabs(){
        final CollectViewPagerAdapter collectViewPagerAdapter = new CollectViewPagerAdapter(requireActivity().getSupportFragmentManager());

        new Handler().post(() -> {
            collectViewPagerAdapter.addFragment(ToSeeFragment.getInstance(), getString(R.string.to_see));
            collectViewPagerAdapter.addFragment(HaveSeenFragment.getInstance(), getString(R.string.have_seen));

            mBinding.viewPager.setAdapter(collectViewPagerAdapter);
            mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
        });
    }
}
