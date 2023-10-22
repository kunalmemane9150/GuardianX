package in.newgenai.guardianx.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import in.newgenai.guardianx.Database.DBRecordHelper;
import in.newgenai.guardianx.Fragment.AudioPlayerFragment;
import in.newgenai.guardianx.Interfaces.OnDatabaseChangeListener;
import in.newgenai.guardianx.Model.RecordingItemModel;
import in.newgenai.guardianx.R;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.FileViewerViewHolder>
        implements OnDatabaseChangeListener {


    Context context;
    ArrayList<RecordingItemModel> arrayList;
    LinearLayoutManager linearLayoutManager;

    DBRecordHelper dbRecordHelper;

    public FileViewerAdapter(Context context, ArrayList<RecordingItemModel> arrayList, LinearLayoutManager linearLayoutManager){
        this.context = context;
        this.arrayList = arrayList;
        this.linearLayoutManager = linearLayoutManager;
        dbRecordHelper = new DBRecordHelper(context);
        dbRecordHelper.setOnDatabaseChangeListener(this);
    }

    @NonNull
    @Override
    public FileViewerAdapter.FileViewerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recordings_view, parent, false);
        return new FileViewerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewerAdapter.FileViewerViewHolder holder, int position) {
        RecordingItemModel recordingItemModel = arrayList.get(position);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItemModel.getLength());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recordingItemModel.getLength()) -
                TimeUnit.MINUTES.toSeconds(minutes);

        holder.fileName.setText(recordingItemModel.getName());
        holder.fileLength.setText(String.format("%02d:%02d", minutes, seconds));

        holder.fileTimeAdded.setText(DateUtils.formatDateTime(context,recordingItemModel.getTime_added(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE |
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onNewDatabaseEntryAdded(RecordingItemModel recordingItemModel) {
        arrayList.add(recordingItemModel);
        notifyItemInserted(arrayList.size()-1);
    }

    public class FileViewerViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName, fileLength, fileTimeAdded;
        private CardView cardView;

        public FileViewerViewHolder(@NonNull View itemView) {
            super(itemView);

            fileName = itemView.findViewById(R.id.fileNameTv);
            fileLength = itemView.findViewById(R.id.fileLengthTv);
            fileTimeAdded = itemView.findViewById(R.id.fileTimeAddedTv);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioPlayerFragment playerFragment = new AudioPlayerFragment();
                    Bundle b = new Bundle();

                    b.putSerializable("item", arrayList.get(getAdapterPosition()));
                    playerFragment.setArguments(b);

                    FragmentTransaction fragmentTransaction = ((FragmentActivity)context)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playerFragment.show(fragmentTransaction, "Player");
                }
            });

        }
    }
}
