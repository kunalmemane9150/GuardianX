package in.newgenai.guardianx.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import in.newgenai.guardianx.Adapter.FileViewerAdapter;
import in.newgenai.guardianx.Database.DBRecordHelper;
import in.newgenai.guardianx.Model.RecordingItemModel;
import in.newgenai.guardianx.R;
import in.newgenai.guardianx.databinding.FragmentCallBinding;
import in.newgenai.guardianx.databinding.FragmentRecordingsBinding;

public class RecordingsFragment extends Fragment {

    private FragmentRecordingsBinding binding;

    DBRecordHelper dbRecordHelper;
    private FileViewerAdapter fileViewerAdapter;

    ArrayList<RecordingItemModel> arrayListAudio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecordingsBinding.inflate(inflater, container, false);

        init();
        Log.d("AudioArrayList: ", String.valueOf(arrayListAudio));

        return binding.getRoot();
    }

    private void init() {
        dbRecordHelper = new DBRecordHelper(getContext());

        binding.recordingsRv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.recordingsRv.setLayoutManager(linearLayoutManager);

        arrayListAudio = dbRecordHelper.getAllAudios();

        if (arrayListAudio == null){
            Toast.makeText(getContext(), "No File Found.", Toast.LENGTH_SHORT).show();
        }else {
            fileViewerAdapter = new FileViewerAdapter(getActivity(), arrayListAudio, linearLayoutManager);
            binding.recordingsRv.setAdapter(fileViewerAdapter);
        }


    }
}