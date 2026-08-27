package org.smartregister.reveal.interactor;

import static org.smartregister.repository.BaseRepository.TYPE_Synced;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_SYNC_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.SYNC_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TASK_SYNC_STATUS;
import static org.smartregister.reveal.util.Constants.Tables.CLIENT_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.EVENT_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.STRUCTURE_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.TASK_TABLE;

import java.util.Set;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.repository.PlanDefinitionRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseDrawerContract;
import org.smartregister.reveal.util.AppExecutors;
import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class BaseDrawerInteractor implements BaseDrawerContract.Interactor {

    private AppExecutors appExecutors;

    private BaseDrawerContract.Presenter presenter;

    private PlanDefinitionRepository planDefinitionRepository;

    private RevealApplication revealApplication;

    private SQLiteDatabase database;

    public BaseDrawerInteractor(BaseDrawerContract.Presenter presenter) {
        this.presenter = presenter;
        revealApplication = RevealApplication.getInstance();
        database = revealApplication.getRepository().getReadableDatabase();
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        planDefinitionRepository = RevealApplication.getInstance().getPlanDefinitionRepository();
    }

    @Override
    public void fetchPlans() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Set<PlanDefinition> planDefinitionSet = planDefinitionRepository.findAllPlanDefinitions();
                appExecutors.mainThread().execute(() -> presenter.onPlansFetched(planDefinitionSet));
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void checkSynced() {

        String syncQuery = getSyncQuery();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Cursor syncCursor = null;
                try
                {
                    syncCursor  = database.rawQuery(syncQuery, new String[]{TYPE_Synced, TYPE_Synced, TYPE_Synced, TYPE_Synced});
                    Integer count = syncCursor.getCount();

                    if(count == 0)
                    {
                        revealApplication.setSynced(true);
                    }
                    else
                    {
                        revealApplication.setSynced(false);
                    }
                }
                catch (Exception e)
                {
                    Timber.tag("Reveal Exception").w(e, "EXCEPTION %s", e.toString());
                }
                finally
                {
                    if (syncCursor != null)
                        syncCursor.close();
                }

                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean synced = revealApplication.getSynced();
                        (presenter).updateSyncStatusDisplay(synced);
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @NotNull
    private String getSyncQuery() {
        return String.format("SELECT %s FROM %s WHERE %s <> ?\n", SYNC_STATUS, CLIENT_TABLE, SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ?\n", SYNC_STATUS, EVENT_TABLE, SYNC_STATUS, SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ?\n", TASK_SYNC_STATUS, TASK_TABLE, TASK_SYNC_STATUS) +
                "UNION ALL\n" +
                String.format("SELECT %s FROM %s WHERE %s <> ?\n", STRUCTURE_SYNC_STATUS, STRUCTURE_TABLE, STRUCTURE_SYNC_STATUS);
    }
}
