package org.smartregister.family;

import androidx.annotation.NonNull;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.AppProperties;

import id.zelory.compressor.Compressor;

/**
 * Created by keyman on 31/07/17.
 */
public class FamilyLibrary {
    private static FamilyLibrary instance;

    private final Context context;
    private FamilyMetadata metadata;

    private int applicationVersion;
    private int databaseVersion;

    private UniqueIdRepository uniqueIdRepository;
    private ECSyncHelper syncHelper;

    private ClientProcessorForJava clientProcessorForJava;
    private Compressor compressor;

    public static void init(Context context, FamilyMetadata familyMetadata, int applicationVersion, int databaseVersion) {
        if (instance == null) {
            instance = new FamilyLibrary(context, familyMetadata, applicationVersion, databaseVersion);
        }
    }

    public static FamilyLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException(" Instance does not exist!!! Call "
                    + CoreLibrary.class.getName()
                    + ".init method in the onCreate method of "
                    + "your Application class ");
        }
        return instance;
    }

    private FamilyLibrary(Context contextArg, FamilyMetadata metadataArg, int applicationVersion, int databaseVersion) {
        this.context = contextArg;
        this.metadata = metadataArg;
        this.applicationVersion = applicationVersion;
        this.databaseVersion = databaseVersion;
    }

    public void setMetadata(FamilyMetadata metadata) {
        this.metadata = metadata;
    }

    public Context context() {
        return context;
    }


    public FamilyMetadata metadata() {
        return metadata;
    }

    public int getApplicationVersion() {
        return applicationVersion;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (syncHelper == null) {
            syncHelper = ECSyncHelper.getInstance(context().applicationContext());
        }
        return syncHelper;
    }


    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = ClientProcessorForJava.getInstance(context().applicationContext());
        }
        return clientProcessorForJava;
    }

    public void setClientProcessorForJava(ClientProcessorForJava clientProcessorForJava) {
        this.clientProcessorForJava = clientProcessorForJava;
    }

    @NonNull
    public Compressor getCompressor() {
        if (compressor == null) {
            compressor = new Compressor(context().applicationContext());
        }
        return compressor;
    }

    /**
     * Use this method when testing.
     * It should replace org.smartregister.Context#setInstance(org.smartregister.Context) which has been removed
     *
     * @param context
     */
    public static void reset(Context context, FamilyMetadata metadata, int applicationVersion, int databaseVersion) {
        if (context != null) {
            instance = new FamilyLibrary(context, metadata, applicationVersion, databaseVersion);
        }
    }

    public AppProperties getProperties() {
        return CoreLibrary.getInstance().context().getAppProperties();
    }
}
