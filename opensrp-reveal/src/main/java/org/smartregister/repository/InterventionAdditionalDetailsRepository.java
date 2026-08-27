package org.smartregister.repository;

import android.content.ContentValues;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseCorruptException;
import org.smartregister.clientandeventmodel.InterventionAdditionalDetail;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class InterventionAdditionalDetailsRepository extends BaseRepository {

    public static final String INTERVENTION_ADDITIONAL_DETAILS = "intervention_additional_details";

    public static final String PLAN_IDENTIFIER = "plan_identifier";
    public static final String EVENT_TYPE = "event_type";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String VALUE_TYPE = "value_type";

    private static final String ID = "id";
    private static final String TASK_KEY_ID = "task_key_id";

    private static final String TASK_ID = "task_id";
    private static final String EVENT_DATE_TIME = "event_date_time";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + INTERVENTION_ADDITIONAL_DETAILS + " (" +
                    TASK_KEY_ID + " TEXT NOT NULL , " +
                    TASK_ID + " TEXT NOT NULL , " +
                    EVENT_DATE_TIME+" TEXT NOT NULL, " +
                    PLAN_IDENTIFIER + " TEXT NOT NULL, " +
                    EVENT_TYPE + " TEXT NOT NULL, " +
                    KEY + " TEXT NOT NULL, " +
                    VALUE + " TEXT NOT NULL, " +
                    VALUE_TYPE + " value_type TEXT NOT NULL, " +
                    "PRIMARY KEY(" + TASK_KEY_ID + ") " +
                    "); ";

    private static final String CREATE_INDEX =
            "CREATE INDEX "+INTERVENTION_ADDITIONAL_DETAILS+"_index ON "+ INTERVENTION_ADDITIONAL_DETAILS +" (" +
                    TASK_KEY_ID +","+PLAN_IDENTIFIER+","+EVENT_TYPE+","+KEY+
                    ");";

    public static void createTable(SQLiteDatabase database) {
        try{
            database.execSQL(CREATE_TABLE);
            database.execSQL(CREATE_INDEX);
        } catch (SQLiteDatabaseCorruptException e){
            Timber.e("Unable to create %s" , INTERVENTION_ADDITIONAL_DETAILS);
        }
    }

    public void addDetailsToTable(List<InterventionAdditionalDetail> details) {

        details.stream().forEach(detail ->{
            ContentValues contentValues = getContentValues(detail);
            getWritableDatabase()
                    .replace(
                            INTERVENTION_ADDITIONAL_DETAILS, null, contentValues);

        });

    }

    public List<String> getStringValuePerFieldCode(String fieldCode, String planIdentifier) {

        String query = "SELECT iad.value from intervention_additional_details iad "
                + " WHERE iad."+PLAN_IDENTIFIER+" = ? "
                + " AND iad." + KEY + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{planIdentifier,fieldCode});
        List<String> values = new ArrayList<>();
        if (cursor != null ) {
            if ( cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndex("value"));
                    values.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;
    }

    public int getSumPerFieldCode(String fieldCode, String planIdentifier, String groupId) {

        String query = "SELECT coalesce(sum("+VALUE+"),0) as sumValue from "+INTERVENTION_ADDITIONAL_DETAILS + " iad"
                + " LEFT JOIN task t on t._id = iad.task_id"
                + " WHERE "
                + " iad." + KEY + " = ?"
                + " AND iad." + VALUE_TYPE + " = 'int' "
                + " AND iad." + PLAN_IDENTIFIER + "=?"
                + " AND t.group_id = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{fieldCode, planIdentifier, groupId});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int value = cursor.getInt(cursor.getColumnIndex("sumValue"));
                cursor.close();
                return value;
            } while (cursor.moveToNext());
        }
        return 0;
    }

    private ContentValues getContentValues(InterventionAdditionalDetail detail) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(PLAN_IDENTIFIER,detail.getPlanIdentifier());
        contentValues.put(TASK_KEY_ID,detail.getTaskKeyId());
        contentValues.put(TASK_ID,detail.getTaskId());
        contentValues.put(EVENT_DATE_TIME,detail.getEventDateTime());
        contentValues.put(EVENT_TYPE,detail.getEventType());
        contentValues.put(KEY,detail.getKey());
        contentValues.put(VALUE,detail.getValue());
        contentValues.put(VALUE_TYPE,detail.getValueType());

        return contentValues;
    }



}
