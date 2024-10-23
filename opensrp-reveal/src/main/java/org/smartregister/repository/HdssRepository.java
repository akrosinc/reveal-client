package org.smartregister.repository;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.domain.HdssCompound;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.HdssIndividualHouseHoldCompound;
import org.smartregister.domain.Period;
import org.smartregister.domain.Task;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;


public class HdssRepository extends BaseRepository {

    public static final String COMPOUND_ID = "compound_id";
    public static final String HOUSEHOLD_ID = "household_id";

    public static final String IDENTIFIER = "identifier";

    public static final String STRUCTURE_ID = "structure_id";

    public static final String INDIVIDUAL_ID = "individual_id";
    public static final String DOB = "dob";
    public static final String GENDER = "gender";

    public static String HDSS_COMPOUND = "hdss_compound";

    public static final String HDSS_COMPOUND_HOUSEHOLD = "hdss_compound_household";
    public static final String HDSS_HOUSEHOLD_INDIVIDUAL = "hdss_household_individual";
    public static final String HDSS_HOUSEHOLD_STRUCTURE = "hdss_household_structure";
    public static final String SERVER_VERSION = "server_version";

    public static final String HDSS_INDIVIDUAL = "hdss_individual";
    private static final String CREATE_HDSS_COMPOUND =
            "CREATE TABLE IF NOT EXISTS " + HDSS_COMPOUND + " ( " +
                    " " + COMPOUND_ID + " TEXT NOT NULL, " +
                    "  " + SERVER_VERSION + " INTEGER NOT NULL, " +
                    " PRIMARY KEY(" + COMPOUND_ID + ") " +
                    ");";

    private static final String CREATE_HDSS_COMPOUND_HOUSEHOLD =
            "CREATE TABLE IF NOT EXISTS " + HDSS_COMPOUND_HOUSEHOLD + " ( " +
                    " " + COMPOUND_ID + " TEXT NOT NULL, " +
                    " " + HOUSEHOLD_ID + "  TEXT NOT NULL, " +
                    "  " + SERVER_VERSION + " INTEGER NOT NULL, " +
                    " PRIMARY KEY(" + HOUSEHOLD_ID + ") " +
                    ");";
    private static final String CREATE_HDSS_HOUSEHOLD_STRUCTURE =
            "CREATE TABLE IF NOT EXISTS " + HDSS_HOUSEHOLD_STRUCTURE + " ( " +
                    " " + HOUSEHOLD_ID + " TEXT NOT NULL, " +
                    " " + STRUCTURE_ID + " TEXT NOT NULL, " +
                    "  " + SERVER_VERSION + " INTEGER NOT NULL, " +
                    " PRIMARY KEY(" + HOUSEHOLD_ID + ") " +
                    ");";
    private static final String CREATE_HDSS_HOUSEHOLD_INDIVIDUAL =
            "CREATE TABLE IF NOT EXISTS " + HDSS_HOUSEHOLD_INDIVIDUAL + "  ( " +
                    "  " + HOUSEHOLD_ID + "  TEXT NOT NULL, " +
                    "  " + INDIVIDUAL_ID + "  TEXT NOT NULL, " +
                    "  " + SERVER_VERSION + " INTEGER NOT NULL, " +
                    " PRIMARY KEY( " + INDIVIDUAL_ID + " ) " +
                    ");";


    private static final String CREATE_HDSS_INDIVIDUAL =
            "CREATE TABLE IF NOT EXISTS " + HDSS_INDIVIDUAL + "  ( " +
                    "  " + IDENTIFIER + "  TEXT NOT NULL, " +
                    "  " + INDIVIDUAL_ID + "  TEXT NOT NULL, " +
                    "  " + DOB + "  TEXT NOT NULL, " +
                    "  " + GENDER + "  TEXT NOT NULL, " +
                    "  " + SERVER_VERSION + " INTEGER NOT NULL, " +
                    " PRIMARY KEY( " + INDIVIDUAL_ID + " ) " +
                    ");";

    public static void createCompoundTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HDSS_COMPOUND);
    }

    public static void createCompoundHouseholdTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HDSS_COMPOUND_HOUSEHOLD);
    }

    public static void createHouseholdStructureTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HDSS_HOUSEHOLD_STRUCTURE);
    }

    public static void createHouseholdIndividualTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HDSS_HOUSEHOLD_INDIVIDUAL);
    }

    public static void createIndividualTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HDSS_INDIVIDUAL);
    }

    public static void deleteData(SQLiteDatabase database) {
        database.execSQL("DELETE FROM " + HDSS_INDIVIDUAL);
        database.execSQL("DELETE FROM " + HDSS_HOUSEHOLD_INDIVIDUAL);
        database.execSQL("DELETE FROM " + HDSS_HOUSEHOLD_STRUCTURE);
        database.execSQL("DELETE FROM " + HDSS_COMPOUND_HOUSEHOLD);
        database.execSQL("DELETE FROM " + HDSS_COMPOUND);
    }

    public void moveIndividualFromHouseholdToHousehold(String fromHoushold, String toHoushold, String individual) {
        getReadableDatabase().delete(HDSS_HOUSEHOLD_INDIVIDUAL, HOUSEHOLD_ID + " = ? AND " + INDIVIDUAL_ID + " = ?", new String[]{
                fromHoushold, individual
        });
        long hdssMaxServerVersion = getMaxServerVersion();
        hdssMaxServerVersion++;
        addOrUpdateHouseholdIndividual(List.of(new HdssHouseholdIndividual(toHoushold, individual, hdssMaxServerVersion)));

    }

    public void addOrUpdateCompounds(List<HdssCompound> compounds) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        compounds.forEach(compound -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COMPOUND_ID, compound.getCompoundId());
            contentValues.put(SERVER_VERSION, compound.getServerVersion());
            writableDatabase.replace(HDSS_COMPOUND, null, contentValues);
        });
    }


    public void addOrUpdateCompoundHouseholds(List<HdssCompoundHousehold> hdssCompoundHousehold) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        hdssCompoundHousehold.forEach(household -> {

            ContentValues contentValues = new ContentValues();
            contentValues.put(HOUSEHOLD_ID, household.getHouseholdId());
            contentValues.put(COMPOUND_ID, household.getCompoundId());
            contentValues.put(SERVER_VERSION, household.getServerVersion());
            writableDatabase.replace(HDSS_COMPOUND_HOUSEHOLD, null, contentValues);

        });
    }

    public void addOrUpdateHouseholdStructure(List<HdssHouseholdStructure> householdStructures) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        householdStructures.forEach(household -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(HOUSEHOLD_ID, household.getHouseholdId());
            contentValues.put(STRUCTURE_ID, household.getStructureId());
            contentValues.put(SERVER_VERSION, household.getServerVersion());
            writableDatabase.replace(HDSS_HOUSEHOLD_STRUCTURE, null, contentValues);

        });
    }

    public void addOrUpdateHouseholdIndividual(List<HdssHouseholdIndividual> householdIndividuals) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        householdIndividuals.forEach(householdIndividual -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(HOUSEHOLD_ID, householdIndividual.getHouseholdId());
            contentValues.put(INDIVIDUAL_ID, householdIndividual.getIndividualId());
            contentValues.put(SERVER_VERSION, householdIndividual.getServerVersion());
            writableDatabase.replace(HDSS_HOUSEHOLD_INDIVIDUAL, null, contentValues);
        });
    }

    public int getMaxServerVersion() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String query = "SELECT MAX(hdss.server_version) as server_version from (\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_individual hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_household_individual hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_household_structure hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_compound hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_compound_household hdss\n" +
                ") as hdss ";

        Cursor cursor;
        try {
            cursor = readableDatabase.rawQuery(query, new String[]{});
        } catch (Exception e) {
            return 0;
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int anInt = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));
                cursor.close();
                return anInt;
            }
        }
        return 0;
    }

    public int getMinServerVersionFromMaxOfAllHdssTables() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String query = "SELECT MIN(hdss.server_version) as server_version from (\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_individual hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_household_individual hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_household_structure hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_compound hdss\n" +
                "UNION ALL\n" +
                "SELECT MAX(hdss.server_version) as server_version from hdss_compound_household hdss\n" +
                ") as hdss ";

        Cursor cursor;
        try {
            cursor = readableDatabase.rawQuery(query, new String[]{});
        } catch (Exception e) {
            return 0;
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int anInt = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));
                cursor.close();
                return anInt;
            }
        }
        return 0;
    }

    public List<HdssIndividualHouseHoldCompound> getItemsGreateThanServerVersion(long serverVersion) {

        SQLiteDatabase readableDatabase = getReadableDatabase();

        List<HdssIndividualHouseHoldCompound> values = new ArrayList<>();
        String query;
        Cursor cursor;

        query = "SELECT \n" +
                "hi.identifier,\n" +
                "hi.individual_id,\n" +
                "hi.dob,\n" +
                "hi.gender,\n" +
                "hi.server_version,\n" +
                "hhi.household_id,\n" +
                "hhs.structure_id,\n" +
                "hc.compound_id\n" +
                "\n" +
                " from hdss_individual hi\n" +
                "left join hdss_household_individual hhi on hhi.individual_id = hi.individual_id\n" +
                "left join hdss_compound_household hch on hch.household_id = hhi.household_id\n" +
                "left join hdss_household_structure hhs on hhs.household_id = hhi.household_id\n" +
                "left join hdss_compound hc on hc.compound_id = hch.compound_id \n" +
                "WHERE hi.server_version > ?";
        try {
            cursor = readableDatabase.rawQuery(query, new String[]{String.valueOf(serverVersion)});


            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

                        String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

                        String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                        String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

                        String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

                        String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

                        String structureId = cursor.getString(cursor.getColumnIndexOrThrow(STRUCTURE_ID));

                        int serverVersionFromDB = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                        HdssIndividualHouseHoldCompound hdssIndividual = new HdssIndividualHouseHoldCompound(identifier, individualId, dob, gender, householdId, compoundId);
                        hdssIndividual.setServerVersion(serverVersionFromDB);
                        hdssIndividual.setStructureId(structureId);

                        values.add(hdssIndividual);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Timber.tag("Reveal Exception").e("Cannot get records by max server version");
        }
        return values;

    }

    public List<HdssIndividualHouseHoldCompound> searchHouseholdIndividual(String searchtext, String genderSearch) {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String query;
        Cursor cursor;
        if (genderSearch != null) {
            query = "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender from hdss_individual hi  \n" +
                    "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n" +
                    "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n" +
                    "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n" +
                    " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)" +
                    " and lower(hi.gender) = lower(?)\n";
            cursor = readableDatabase.rawQuery(query, new String[]{"%" + searchtext + "%", "%" + searchtext + "%", "%" + searchtext + "%", genderSearch});
        } else {
            query = "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender from hdss_individual hi  \n" +
                    "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n" +
                    "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n" +
                    "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n" +
                    " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)\n";
            cursor = readableDatabase.rawQuery(query, new String[]{"%" + searchtext + "%", "%" + searchtext + "%", "%" + searchtext + "%"});
        }


        List<HdssIndividualHouseHoldCompound> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

                    String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

                    String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

                    String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

                    String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

                    HdssIndividualHouseHoldCompound hdssIndividual = new HdssIndividualHouseHoldCompound(identifier, individualId, dob, gender, householdId, compoundId);


                    values.add(hdssIndividual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;
    }

    public void addOrUpdateIndividual(List<HdssIndividual> householdIndividuals) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        householdIndividuals.forEach(individual -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IDENTIFIER, individual.getIdentifier());
            contentValues.put(INDIVIDUAL_ID, individual.getIndividualId());
            contentValues.put(DOB, individual.getDob());
            contentValues.put(GENDER, individual.getGender());
            contentValues.put(SERVER_VERSION, individual.getServerVersion());
            writableDatabase.replace(HDSS_INDIVIDUAL, null, contentValues);
        });
    }

    public List<String> getCompounds() {

        String query = "SELECT hc." + COMPOUND_ID + " from " + HDSS_COMPOUND + " hc";

        SQLiteDatabase db = getReadableDatabase();
        RevealApplication.getInstance().getRepository().getReadableDatabase();


        Cursor cursor = db.rawQuery(query, null);
        List<String> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));
                    values.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<String> getHouseholdsByCompoundId(String compoundId) {

        String query = "SELECT hc." + HOUSEHOLD_ID + " FROM " + HDSS_COMPOUND_HOUSEHOLD + " hc WHERE hc." + COMPOUND_ID + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{compoundId});
        List<String> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
                    values.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<String> getHouseholds() {

        String query = "SELECT hc." + HOUSEHOLD_ID + " FROM " + HDSS_COMPOUND_HOUSEHOLD + " hc";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<String> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
                    values.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<HdssIndividual> getIndividualsByHouseholdId(String householdId) {

        String query = "SELECT " + " i." + IDENTIFIER + "," +
                "i." + INDIVIDUAL_ID + ", i." + DOB + ", i." + GENDER + " , i.server_version From " + HDSS_HOUSEHOLD_INDIVIDUAL + " hc \n" +
                "left join " + HDSS_INDIVIDUAL + " i on i." + INDIVIDUAL_ID + " = hc." + INDIVIDUAL_ID + "\n" +
                "where hc." + HOUSEHOLD_ID + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{householdId});
        List<HdssIndividual> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
                    String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

                    String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

                    long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                    HdssIndividual hdssIndividual = new HdssIndividual(identifier, individualId, dob, gender, serverVersion);

                    values.add(hdssIndividual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<HdssIndividual> getIndividuals() {

        String query = "SELECT " + " i." + IDENTIFIER + "," +
                "i." + INDIVIDUAL_ID + ", i." + DOB + ", i." + GENDER + ", i.server_version"
                + " From " + HDSS_INDIVIDUAL + " i";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<HdssIndividual> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

                    String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

                    String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

                    long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                    HdssIndividual hdssIndividual = new HdssIndividual(identifier, individualId, dob, gender, serverVersion);


                    values.add(hdssIndividual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<HdssIndividualHouseHoldCompound> getIndividualHouseholdCompound() {

        String query = "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender from hdss_individual hi  " +
                "left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id " +
                "left join hdss_compound_household hch on hch.household_id = hhi.household_id " +
                "left join hdss_compound hc on hc.compound_id = hch.compound_id";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<HdssIndividualHouseHoldCompound> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

                    String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

                    String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

                    String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

                    String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

                    HdssIndividualHouseHoldCompound hdssIndividual = new HdssIndividualHouseHoldCompound(identifier, individualId, dob, gender, householdId, compoundId);


                    values.add(hdssIndividual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public List<HdssCompoundHousehold> getHouseholdCompound() {

        String query = "SELECT compound_id, household_id, server_version  from hdss_compound_household";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<HdssCompoundHousehold> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

                    String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

                    long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                    HdssCompoundHousehold compoundHousehold = new HdssCompoundHousehold(compoundId, householdId, serverVersion);

                    values.add(compoundHousehold);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;

    }

    public Set<Task> getTasksByStructure(String structureId, String planIdentifier) {

        String query = "SELECT * from task t where t.for in (\n" +
                "SELECT i.identifier\n" +
                "--,hi.*, hs.*\n" +
                " From hdss_individual i \n" +
                "inner join hdss_household_individual hi on hi.individual_id = i.individual_id\n" +
                "inner join hdss_household_structure hs on hi.household_id = hs.household_id\n" +
                "where hs.structure_id = ? and t.plan_id = ?\n" +
                ")";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{structureId, planIdentifier});

        Set<Task> tasks = new HashSet<>();
        while (cursor.moveToNext()) {
            Task task = readTaskCursor(cursor);
            task.setStructureId(structureId);
            tasks.add(task);
        }

        return tasks;
    }


    public List<String> getHouseholdIdByStructureId(String structureId) {
        String query = "SELECT hs.household_id from hdss_household_structure hs\n" +
                "        WHERE hs.structure_id = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{structureId});
        List<String> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
                    values.add(householdId);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;
    }

    public List<HdssCompoundHousehold> getCompoundAndHouseholdByStructureId(String structureId) {
        String query = "    SELECT hc.compound_id,hch.household_id, hhs.structure_id, hc.server_version from hdss_compound hc " +
                "    left join hdss_compound_household hch on hch.compound_id = hc.compound_id " +
                "    left join hdss_household_structure hhs on hhs.household_id = hch.household_id " +
                "    where structure_id = ? LIMIT 1 ";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{structureId});
        List<HdssCompoundHousehold> values = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

                    String compound = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

                    long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                    HdssCompoundHousehold hdssCompoundHousehold = new HdssCompoundHousehold(compound, householdId, serverVersion);
                    values.add(hdssCompoundHousehold);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;
    }


    public Map<String, HdssIndividual> getIndividualsByStructureId(String structureId) {

        String query = "SELECT " + " i." + IDENTIFIER + "," +
                "i." + INDIVIDUAL_ID + ", i." + DOB + ", i." + GENDER + ", i." + SERVER_VERSION
                + " From " + HDSS_INDIVIDUAL + " i " +
                " inner join hdss_household_individual hi on hi.individual_id = i.individual_id\n" +
                " inner join hdss_household_structure hs on hs.household_id = hi.household_id\n" +
                " WHERE hs.structure_id = ?";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{structureId});
        Map<String, HdssIndividual> values = new HashMap<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
                    String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));
                    String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
                    String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
                    long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

                    HdssIndividual hdssIndividual = new HdssIndividual(identifier, individualId, dob, gender, serverVersion);
                    values.put(identifier, hdssIndividual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return values;
    }

    public Task readTaskCursor(Cursor cursor) {
        Task task = new Task();
        task.setIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.ID)));
        task.setPlanIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.PLAN_ID)));
        task.setGroupIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.GROUP_ID)));
        if (cursor.getString(cursor.getColumnIndex(TaskRepository.STATUS)) != null) {
            task.setStatus(Task.TaskStatus.valueOf(cursor.getString(cursor.getColumnIndex(TaskRepository.STATUS))));
        }
        task.setBusinessStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.BUSINESS_STATUS)));
        task.setPriority(Task.TaskPriority.valueOf(cursor.getString(cursor.getColumnIndex(TaskRepository.PRIORITY))));
        task.setCode(cursor.getString(cursor.getColumnIndex(TaskRepository.CODE)));
        task.setDescription(cursor.getString(cursor.getColumnIndex(TaskRepository.DESCRIPTION)));
        task.setFocus(cursor.getString(cursor.getColumnIndex(TaskRepository.FOCUS)));
        task.setForEntity(cursor.getString(cursor.getColumnIndex(TaskRepository.FOR)));
        Period period = new Period();
        period.setStart(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.START))));
        period.setEnd(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.END))));
        task.setExecutionPeriod(period);
        task.setAuthoredOn(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.AUTHORED_ON))));
        task.setLastModified(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.LAST_MODIFIED))));
        task.setOwner(cursor.getString(cursor.getColumnIndex(TaskRepository.OWNER)));
        task.setSyncStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.SYNC_STATUS)));
        task.setServerVersion(cursor.getLong(cursor.getColumnIndex(TaskRepository.SERVER_VERSION)));

        task.setReasonReference(cursor.getString(cursor.getColumnIndex(TaskRepository.REASON_REFERENCE)));
        task.setLocation(cursor.getString(cursor.getColumnIndex(TaskRepository.LOCATION)));
        task.setRequester(cursor.getString(cursor.getColumnIndex(TaskRepository.REQUESTER)));
        Period restrictionPeriod = new Period();
        restrictionPeriod.setStart(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_START))));
        restrictionPeriod.setEnd(DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_END))));
        Task.Restriction restriction = new Task.Restriction(cursor.getInt(cursor.getColumnIndex(TaskRepository.RESTRICTION_REPEAT)), restrictionPeriod);
        if (restriction.getRepetitions() != 0 || restrictionPeriod.getStart() != null && restrictionPeriod.getEnd() != null) {
            task.setRestriction(restriction);
        }
        return task;
    }
}
