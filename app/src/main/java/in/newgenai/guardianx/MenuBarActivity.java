package in.newgenai.guardianx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.os.Bundle;

import in.newgenai.guardianx.Adapter.ContactUsTabAdapter;
import in.newgenai.guardianx.Fragment.ContactUsFragment;
import in.newgenai.guardianx.Fragment.PrivacyPolicyFragment;
import in.newgenai.guardianx.Fragment.TermsFragment;
import in.newgenai.guardianx.databinding.ActivityMenuBarBinding;

public class MenuBarActivity extends AppCompatActivity {

    private ActivityMenuBarBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabLayout.setupWithViewPager(binding.viewPager);

        ContactUsTabAdapter contactTabAdapter = new ContactUsTabAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        contactTabAdapter.clearList();
        contactTabAdapter.addFragment(new ContactUsFragment(),"Contact");
        contactTabAdapter.addFragment(new PrivacyPolicyFragment(),"Privacy");
        contactTabAdapter.addFragment(new TermsFragment(),"Terms");
        binding.viewPager.setAdapter(contactTabAdapter);
    }
}