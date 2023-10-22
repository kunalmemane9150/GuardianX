package in.newgenai.guardianx.Interfaces;

import in.newgenai.guardianx.Model.RecordingItemModel;

public interface OnDatabaseChangeListener {
    void onNewDatabaseEntryAdded(RecordingItemModel recordingItemModel);
}
