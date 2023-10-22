package in.newgenai.guardianx.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.newgenai.guardianx.R;
import in.newgenai.guardianx.databinding.FragmentCallBinding;
import in.newgenai.guardianx.databinding.FragmentContactUsBinding;

public class ContactUsFragment extends Fragment {


    private FragmentContactUsBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactUsBinding.inflate(inflater, container, false);

        binding.sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();

            }
        });

        return binding.getRoot();
    }
    String subject, description;
    private void sendEmail() {

        subject = binding.subjectEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (subject ==" " || subject.isEmpty()){
            binding.subjectEt.setError("Please enter subject");
            binding.subjectEt.requestFocus();
            return;
        }
        if (description ==" " || description.isEmpty()){
            binding.subjectEt.setError("Please enter description");
            binding.subjectEt.requestFocus();
            return;
        }

        String[] TO_EMAIL = {"kunal.memane20@vit.edu"};

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, TO_EMAIL);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, description);

        startActivity(Intent.createChooser(intent, "Choose email application:"));

    }

    @Override
    public void onStop() {
        binding.subjectEt.setText("");
        binding.descriptionEt.setText("");
        super.onStop();
    }
}