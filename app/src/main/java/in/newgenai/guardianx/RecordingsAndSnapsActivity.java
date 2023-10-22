package in.newgenai.guardianx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.os.Bundle;

import in.newgenai.guardianx.Adapter.ContactUsTabAdapter;
import in.newgenai.guardianx.Fragment.RecordingsFragment;
import in.newgenai.guardianx.Fragment.SnapsFragment;
import in.newgenai.guardianx.databinding.ActivityRecordingsAndSnapsBinding;

public class RecordingsAndSnapsActivity extends AppCompatActivity {

    private ActivityRecordingsAndSnapsBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordingsAndSnapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

    }
    private void init(){

        binding.tabLayout.setupWithViewPager(binding.viewPager);

        ContactUsTabAdapter contactTabAdapter = new ContactUsTabAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        contactTabAdapter.clearList();
        contactTabAdapter.addFragment(new RecordingsFragment(),"Recordings");
        contactTabAdapter.addFragment(new SnapsFragment(),"Snaps");
        binding.viewPager.setAdapter(contactTabAdapter);

    }
}