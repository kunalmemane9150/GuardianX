package in.newgenai.guardianx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

import in.newgenai.guardianx.Database.DBRecordHelper;
import in.newgenai.guardianx.Model.SnapModel;
import in.newgenai.guardianx.OpenSnapActivity;
import in.newgenai.guardianx.R;

public class SnapAdapter extends RecyclerView.Adapter<SnapAdapter.ViewHolder> {

    private ArrayList<SnapModel> snapModelArrayList;
    private Context context;

    OnItemClickListener onItemClickListener;


    public SnapAdapter(ArrayList<SnapModel> arrayList, Context context) {
        this.snapModelArrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SnapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_snap_view, parent, false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SnapAdapter.ViewHolder holder, int position) {

        holder.fileName.setText(snapModelArrayList.get(position).getTitle());
        holder.fileSize.setText(getSize(snapModelArrayList.get(position).getSize()));

        Glide.with(context).load(snapModelArrayList.get(position).getPath())
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.snapImageView);

        holder.itemView.setOnClickListener(view ->
                onItemClickListener.onClick(view, snapModelArrayList.get(position).getPath()));


    }

    public static String getSize(long size) {
        if (size <= 0) {
            return "0";
        }

        double d = (double) size;
        int log10 = (int) (Math.log10(d) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double power = Math.pow(1024.0d, log10);
        stringBuilder.append(decimalFormat.format(d / power));
        stringBuilder.append(" ");
        stringBuilder.append(new String[]{"B", "KB", "MB", "GB", "TB"}[log10]);
        return stringBuilder.toString();
    }


    @Override
    public int getItemCount() {
        return snapModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView snapImageView;
        private TextView fileName, fileSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            snapImageView = itemView.findViewById(R.id.snapImageView);
            fileName = itemView.findViewById(R.id.fileNameTv);
            fileSize = itemView.findViewById(R.id.fileSizeTv);

        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(View view, String path);
   }

}
