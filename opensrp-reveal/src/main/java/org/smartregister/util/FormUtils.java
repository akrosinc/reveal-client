package org.smartregister.util;

import android.content.Context;
/**
 * Created by koros on 9/28/15.
 */
public class FormUtils {

    public static final String TAG = "FormUtils";
    public static final String ecClientRelationships = "ec_client_relationships.json";
    private static FormUtils instance;
    private Context mContext;


    public FormUtils(Context context)  {
        mContext = context;
    }

    public static FormUtils getInstance(Context ctx) throws Exception {
        if (instance == null)
            instance = new FormUtils(ctx);

        if (ctx != null && instance.mContext != ctx)
            instance.mContext = ctx;

        return instance;
    }












}