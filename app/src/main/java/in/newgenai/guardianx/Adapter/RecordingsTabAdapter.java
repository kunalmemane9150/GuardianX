package in.newgenai.guardianx.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class RecordingsTabAdapter extends FragmentPagerAdapter {

    private static ArrayList<Fragment> fragmentArrayLists = new ArrayList<Fragment>();
    private static ArrayList<String> fragmentTitle = new ArrayList<String>();

    public RecordingsTabAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentArrayLists.get(position);
    }

    @Override
    public int getCount() {
        return fragmentArrayLists.size();
    }

    public void addFragment(Fragment fragment, String title){
        fragmentArrayLists.add(fragment);
        fragmentTitle.add(title);
    }

    public void clearList(){
        fragmentTitle.clear();
        fragmentArrayLists.clear();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitle.get(position);
    }
}
