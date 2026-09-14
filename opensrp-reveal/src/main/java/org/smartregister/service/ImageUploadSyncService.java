package org.smartregister.service;

import android.app.IntentService;
import android.content.Intent;

import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.ResponseStatus;
import org.smartregister.repository.ImageRepository;

import java.util.List;

import timber.log.Timber;

import static org.smartregister.util.Log.logError;


public class ImageUploadSyncService extends IntentService {
    private static final String TAG = ImageUploadSyncService.class.getCanonicalName();
    private ImageRepository imageRepo;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * name Used to name the worker thread, important only for debugging.
     */
    public ImageUploadSyncService() {
        super("ImageUploadSyncService");
        imageRepo = CoreLibrary.getInstance().context().imageRepository();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            List<ProfileImage> profileImages = imageRepo.findAllUnSynced();
            for (int i = 0; i < profileImages.size(); i++) {
                String response = CoreLibrary.getInstance().context().getHttpAgent().httpImagePost(
                        getImageUploadEndpoint(), profileImages.get(i));
                if (response.contains(ResponseStatus.success.displayValue())) {
                    imageRepo.close(profileImages.get(i).getImageid());
                } else {
                    Timber.tag("Reveal Exception").w("Image Upload: could NOT upload image ID: %s %s %s ", profileImages.get(i).getImageid(), " PATH: ", profileImages.get(i).getFilepath());

                }
            }
        } catch (Exception e) {
            logError(TAG, e.getMessage());
        }
    }

    private String getImageUploadEndpoint() {
        return CoreLibrary.getInstance().context().configuration().dristhiBaseURL()
                + AllConstants.PROFILE_IMAGES_UPLOAD_PATH;
    }

    /**
     * Entry point for WorkManager workers. Uploads the unsynced profile images
     * synchronously on the caller's (worker) thread without the
     * {@link android.app.IntentService} lifecycle or a background {@code startService()}.
     * This class extends the plain {@link android.app.IntentService} (not
     * {@code BaseSyncIntentService}); its {@link ImageRepository} is initialized in the
     * constructor, so no additional lifecycle initialization needs replicating here.
     *
     * @param appContext the application context provided by the Worker
     */
    public void runImageUpload(@androidx.annotation.NonNull android.content.Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        onHandleIntent(new Intent());
    }
}