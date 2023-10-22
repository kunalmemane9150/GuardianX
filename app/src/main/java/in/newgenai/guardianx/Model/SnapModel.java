package in.newgenai.guardianx.Model;

import android.net.Uri;

public class SnapModel {

    String title;
    String path;
    long size;

    public SnapModel(String title, String path, long size) {
        this.title = title;
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public long getSize() {
        return size;
    }

}
