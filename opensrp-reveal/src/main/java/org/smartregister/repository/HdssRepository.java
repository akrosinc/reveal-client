package org.smartregister.repository;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.domain.HdssCompound;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.HdssIndividualHouseHoldCompound;
import org.smartregister.domain.IndividualsAndTasksForCompound;
import org.smartregister.domain.Period;
import org.smartregister.domain.StructureTaskForCompound;
import org.smartregister.domain.Task;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.searchbox.HdssSearchBoxFactory;
import org.smartregister.reveal.test.HdssTask;
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
  public static final String _ID = "_id";

  public static final String IDENTIFIER = "identifier";

  public static final String STRUCTURE_ID = "structure_id";

  public static final String INDIVIDUAL_ID = "individual_id";
  public static final String DOB = "dob";
  public static final String GENDER = "gender";
  public static final String CLUSTER = "cluster";

  public static String HDSS_COMPOUND = "hdss_compound";

  public static String HDSS_HOUSEHOLD = "hdss_household";

  public static final String HDSS_COMPOUND_HOUSEHOLD = "hdss_compound_household";
  public static final String HDSS_HOUSEHOLD_INDIVIDUAL = "hdss_household_individual";
  public static final String HDSS_HOUSEHOLD_STRUCTURE = "hdss_household_structure";
  public static final String SERVER_VERSION = "server_version";

  public static final String NAME = "name";

  public static final String FLOATING_LOCATION_ID = "floating_location_id";
  public static final String FLOATING_LOCATION_NAME = "floating_location_name";
  public static final String FLOATING_LOCATION_GEOGRAPHIC_LEVEL =
      "floating_location_geographic_level";
  public static final String FLOATING_HOUSEHOLD_LOCATION_NAME = "floating_household_location_name";

  public static final String HDSS_INDIVIDUAL = "hdss_individual";
  public static final String HDSS_SEARCH_RESULTS = "hdss_search_results";
  private static final String CREATE_HDSS_COMPOUND =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_COMPOUND
          + " ( "
          + " "
          + COMPOUND_ID
          + " TEXT NOT NULL, "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY("
          + COMPOUND_ID
          + ") "
          + ");";

  private static final String CREATE_HDSS_HOUSEHOLD =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_HOUSEHOLD
          + " ( "
          + " "
          + HOUSEHOLD_ID
          + " TEXT NOT NULL, "
          + "  "
          + FLOATING_HOUSEHOLD_LOCATION_NAME
          + "  TEXT , "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY("
          + HOUSEHOLD_ID
          + ") "
          + ");";

  private static final String CREATE_HDSS_COMPOUND_HOUSEHOLD =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_COMPOUND_HOUSEHOLD
          + " ( "
          + " "
          + COMPOUND_ID
          + " TEXT NOT NULL, "
          + " "
          + HOUSEHOLD_ID
          + "  TEXT NOT NULL, "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY("
          + HOUSEHOLD_ID
          + ") "
          + ");";

  private static final String CREATE_HDSS_COMPOUND_HOUSEHOLD_INDEX =
      "CREATE INDEX IF NOT EXISTS hdss_compound_household_household_id_idx ON hdss_compound_household (household_id);";

  private static final String CREATE_HDSS_HOUSEHOLD_STRUCTURE =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_HOUSEHOLD_STRUCTURE
          + " ( "
          + " "
          + HOUSEHOLD_ID
          + " TEXT NOT NULL, "
          + " "
          + STRUCTURE_ID
          + " TEXT NOT NULL, "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY("
          + HOUSEHOLD_ID
          + ") "
          + ");";

  private static final String CREATE_HDSS_HOUSEHOLD_STRUCTURE_INDEX =
      "CREATE INDEX IF NOT EXISTS hdss_household_structure_structure_id_idx ON hdss_household_structure (structure_id);";

  private static final String CREATE_HDSS_HOUSEHOLD_INDIVIDUAL =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_HOUSEHOLD_INDIVIDUAL
          + "  ( "
          + "  "
          + HOUSEHOLD_ID
          + "  TEXT NOT NULL, "
          + "  "
          + INDIVIDUAL_ID
          + "  TEXT NOT NULL, "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY( "
          + INDIVIDUAL_ID
          + " ) "
          + ");";

  private static final String CREATE_HDSS_HOUSEHOLD_INDIVIDUAL_INDEX =
      "CREATE INDEX IF NOT EXISTS hdss_household_individual_individual_id_idx ON hdss_household_individual (individual_id);";


  private static final String CREATE_HDSS_INDIVIDUAL =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_INDIVIDUAL
          + "  ( "
          + "  "
          + IDENTIFIER
          + "  TEXT NOT NULL, "
          + "  "
          + INDIVIDUAL_ID
          + "  TEXT NOT NULL, "
          + "  "
          + DOB
          + "  TEXT NOT NULL, "
          + "  "
          + GENDER
          + "  TEXT NOT NULL, "
          + "  "
          + NAME
          + "  TEXT NOT NULL, "
          + "  "
          + FLOATING_LOCATION_ID
          + "  TEXT , "
          + "  "
          + FLOATING_LOCATION_NAME
          + "  TEXT , "
          + "  "
          + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
          + "  TEXT , "
          + "  "
          + CLUSTER
          + "  TEXT , "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY( "
          + INDIVIDUAL_ID
          + " ) "
          + ");";

  private static final String CREATE_HDSS_INDIVIDUAL_INDEX =
      "CREATE INDEX IF NOT EXISTS  hdss_individual_identifier_idx ON hdss_individual ( "
          + " identifier "
          + ");";

  public static final String DROP_HDSS_SEARCH_RESULTS = "DROP TABLE IF EXISTS "+HDSS_SEARCH_RESULTS+";";

  private static final String CREATE_HDSS_SEARCH_RESULTS =
      "CREATE TABLE IF NOT EXISTS "
          + HDSS_SEARCH_RESULTS
          + "  ( "
          + "  "
          + IDENTIFIER
          + "  TEXT NOT NULL, "
          + "  "
          + INDIVIDUAL_ID
          + "  TEXT NOT NULL, "
          + " "
          + COMPOUND_ID
          + " TEXT, "
          + " "
          + HOUSEHOLD_ID
          + "  TEXT, "
          + "  "
          + DOB
          + "  TEXT NOT NULL, "
          + "   "
          + NAME
          + "   TEXT NOT NULL, "
          + "  "
          + GENDER
          + "  TEXT NOT NULL, "
          + "  "
          + CLUSTER
          + "  TEXT, "
          + "  "
          + SERVER_VERSION
          + " INTEGER NOT NULL, "
          + " PRIMARY KEY( "
          + INDIVIDUAL_ID
          + " ) "
          + ");";

  public static void createCompoundTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_COMPOUND);
  }

  public static void createHouseholdTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_HOUSEHOLD);
  }

  public static void createCompoundHouseholdTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_COMPOUND_HOUSEHOLD);
  }

  public static void createCompoundHouseholdTableIndex(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_COMPOUND_HOUSEHOLD_INDEX);
  }

  public static void createHouseholdStructureTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_HOUSEHOLD_STRUCTURE);
  }

  public static void createHouseholdStructureTableIndex(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_HOUSEHOLD_STRUCTURE_INDEX);
  }

  public static void createHouseholdIndividualTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_HOUSEHOLD_INDIVIDUAL);
  }

  public static void createHouseholdIndividualTableIndex(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_HOUSEHOLD_INDIVIDUAL_INDEX);
  }

  public static void createIndividualTable(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_INDIVIDUAL);
  }

  public static void createIndividualTableIndex(SQLiteDatabase database) {
    database.execSQL(CREATE_HDSS_INDIVIDUAL_INDEX);
  }

  public static void createSearchResultsTable(SQLiteDatabase database) {
    database.execSQL(DROP_HDSS_SEARCH_RESULTS);
    database.execSQL(CREATE_HDSS_SEARCH_RESULTS);
  }

  public static void deleteData(SQLiteDatabase database) {
    database.execSQL("DELETE FROM " + HDSS_INDIVIDUAL);
    database.execSQL("DELETE FROM " + HDSS_HOUSEHOLD_INDIVIDUAL);
    database.execSQL("DELETE FROM " + HDSS_HOUSEHOLD_STRUCTURE);
    database.execSQL("DELETE FROM " + HDSS_COMPOUND_HOUSEHOLD);
    database.execSQL("DELETE FROM " + HDSS_COMPOUND);
  }

  public void moveIndividualFromHouseholdToHousehold(
      String fromHoushold, String toHoushold, String individual) {
    getReadableDatabase()
        .delete(
            HDSS_HOUSEHOLD_INDIVIDUAL,
            HOUSEHOLD_ID + " = ? AND " + INDIVIDUAL_ID + " = ?",
            new String[] {fromHoushold, individual});
    long hdssMaxServerVersion = getMaxServerVersion();
    hdssMaxServerVersion++;
    addOrUpdateHouseholdIndividual(
        List.of(new HdssHouseholdIndividual(toHoushold, individual, hdssMaxServerVersion)));
  }

  public void addIndividualToHousehold(String toHousehold, String individual) {

    SQLiteDatabase db = getWritableDatabase();

    // Get the current server version (you can use your own method to get this value)
    long hdssMaxServerVersion = getMaxServerVersion();
    hdssMaxServerVersion++; // Increment to get the new server version

    // Define ContentValues for the record
    ContentValues values = new ContentValues();
    values.put(HOUSEHOLD_ID, toHousehold);
    values.put(INDIVIDUAL_ID, individual);
    values.put(SERVER_VERSION, hdssMaxServerVersion);

    // Check if the individual already exists in the table
    Cursor cursor =
        db.query(
            HDSS_HOUSEHOLD_INDIVIDUAL,
            new String[] {INDIVIDUAL_ID},
            INDIVIDUAL_ID + " = ?",
            new String[] {individual},
            null,
            null,
            null);

    if (cursor != null && cursor.moveToFirst()) {
      // If individual exists, update the record
      db.update(
          "hdss_household_individual", values, "individual_id = ?", new String[] {individual});
      cursor.close();
    } else {
      // If individual doesn't exist, insert the new record
      db.insert("hdss_household_individual", null, values);
    }

    // Close the cursor and the database
    if (cursor != null) {
      cursor.close();
    }
    db.close();
  }

  public void deleteIndividualFromHousehold(String individual) {
    getReadableDatabase()
        .delete(HDSS_HOUSEHOLD_INDIVIDUAL, INDIVIDUAL_ID + " = ?", new String[] {individual});
  }

  public void deleteHouseholdFromCompound(String householdId) {
    getReadableDatabase()
        .delete(HDSS_COMPOUND_HOUSEHOLD, HOUSEHOLD_ID + " = ?", new String[] {householdId});
  }

  public void removeHouseholdFromStructure(String householdId) {
    getReadableDatabase()
        .delete(HDSS_HOUSEHOLD_STRUCTURE, HOUSEHOLD_ID + " = ?", new String[] {householdId});
  }

  public void removeHouseholdFromCompound(String householdId) {
    getReadableDatabase()
        .delete(HDSS_COMPOUND_HOUSEHOLD, HOUSEHOLD_ID + " = ?", new String[] {householdId});
  }

  public void addOrUpdateCompounds(List<HdssCompound> compounds) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    compounds.forEach(
        compound -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(COMPOUND_ID, compound.getCompoundId());
          contentValues.put(SERVER_VERSION, compound.getServerVersion());
          writableDatabase.replace(HDSS_COMPOUND, null, contentValues);
        });
  }

  public void addOrUpdateCompoundsBatched(List<HdssCompound> compounds) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction to ensure atomicity and better performance
    writableDatabase.beginTransaction();
    try {
      int batchSize = 40; // Set the batch size to 30
      for (int i = 0; i < compounds.size(); i += batchSize) {
        // Get the sublist representing the current batch (up to 30 items)
        int end = Math.min(i + batchSize, compounds.size());
        List<HdssCompound> batch = compounds.subList(i, end);

        // Start building the SQL insert statement for this batch
        StringBuilder sql =
            new StringBuilder(
                "REPLACE INTO "
                    + HDSS_COMPOUND
                    + " ("
                    + COMPOUND_ID
                    + ", "
                    + SERVER_VERSION
                    + ") VALUES ");

        // Add values for each compound in the batch
        for (int j = 0; j < batch.size(); j++) {
          HdssCompound compound = batch.get(j);
          sql.append("(")
              .append("'")
              .append(compound.getCompoundId())
              .append("'")
              .append(", ")
              .append(compound.getServerVersion())
              .append(")");

          if (j < batch.size() - 1) {
            sql.append(", "); // Separate with commas if not the last item
          }
        }

        // Execute the batch insert for this batch
        writableDatabase.execSQL(sql.toString());
      }

      // Mark the transaction as successful
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any errors
      Timber.tag("batching").e(e, "error");
    } finally {
      // End the transaction
      writableDatabase.endTransaction();
    }
  }

  public void addOrUpdateCompoundHouseholds(List<HdssCompoundHousehold> hdssCompoundHousehold) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    hdssCompoundHousehold.forEach(
        household -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(HOUSEHOLD_ID, household.getHouseholdId());
          contentValues.put(COMPOUND_ID, household.getCompoundId());
          contentValues.put(SERVER_VERSION, household.getServerVersion());
          writableDatabase.replace(HDSS_COMPOUND_HOUSEHOLD, null, contentValues);
        });
  }

  public void addOrUpdateCompoundHouseholdsBatched(
      List<HdssCompoundHousehold> hdssCompoundHousehold) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction for better performance
    writableDatabase.beginTransaction();
    try {
      int batchSize = 40; // Batch size of 30 items
      for (int i = 0; i < hdssCompoundHousehold.size(); i += batchSize) {
        // Get the sublist representing the current batch (up to 30 items)
        int end = Math.min(i + batchSize, hdssCompoundHousehold.size());
        List<HdssCompoundHousehold> batch = hdssCompoundHousehold.subList(i, end);

        // Build the SQL query for batch insert
        StringBuilder sql =
            new StringBuilder(
                "REPLACE INTO "
                    + HDSS_COMPOUND_HOUSEHOLD
                    + " ("
                    + HOUSEHOLD_ID
                    + ", "
                    + COMPOUND_ID
                    + ", "
                    + SERVER_VERSION
                    + ") VALUES ");

        // Add values for each compound household in the batch
        for (int j = 0; j < batch.size(); j++) {
          HdssCompoundHousehold household = batch.get(j);
          sql.append("(")
              .append("'")
              .append(household.getHouseholdId())
              .append("'")
              .append(", ")
              .append("'")
              .append(household.getCompoundId())
              .append("'")
              .append(", ")
              .append(household.getServerVersion())
              .append(")");

          if (j < batch.size() - 1) {
            sql.append(", "); // Separate values with commas, except for the last one
          }
        }

        // Execute the batch insert for this batch
        writableDatabase.execSQL(sql.toString());
      }

      // Commit the transaction after all batches have been processed
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      e.printStackTrace();
    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }

  public void addOrUpdateHouseholdStructure(List<HdssHouseholdStructure> householdStructures) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    householdStructures.forEach(
        household -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(HOUSEHOLD_ID, household.getHouseholdId());
          contentValues.put(STRUCTURE_ID, household.getStructureId());
          contentValues.put(SERVER_VERSION, household.getServerVersion());
          writableDatabase.replace(HDSS_HOUSEHOLD_STRUCTURE, null, contentValues);
        });
  }

  public void addOrUpdateHouseholdStructureBatched(
      List<HdssHouseholdStructure> householdStructures) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction to improve performance and ensure atomicity
    writableDatabase.beginTransaction();
    try {
      int batchSize = 40; // Set the batch size to 30 items
      for (int i = 0; i < householdStructures.size(); i += batchSize) {
        // Get the sublist representing the current batch (up to 30 items)
        int end = Math.min(i + batchSize, householdStructures.size());
        List<HdssHouseholdStructure> batch = householdStructures.subList(i, end);

        // Build the SQL query for batch insert
        StringBuilder sql =
            new StringBuilder(
                "REPLACE INTO "
                    + HDSS_HOUSEHOLD_STRUCTURE
                    + " ("
                    + HOUSEHOLD_ID
                    + ", "
                    + STRUCTURE_ID
                    + ", "
                    + SERVER_VERSION
                    + ") VALUES ");

        // Add values for each household structure in the batch
        for (int j = 0; j < batch.size(); j++) {
          HdssHouseholdStructure household = batch.get(j);
          sql.append("(")
              .append("'")
              .append(household.getHouseholdId())
              .append("'")
              .append(", ")
              .append("'")
              .append(household.getStructureId())
              .append("'")
              .append(", ")
              .append(household.getServerVersion())
              .append(")");

          if (j < batch.size() - 1) {
            sql.append(", "); // Separate values with commas, except for the last one
          }
        }

        // Execute the batch insert for this batch
        writableDatabase.execSQL(sql.toString());
      }

      // Commit the transaction after all batches have been processed
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      e.printStackTrace();
    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }

  public void addOrUpdateHouseholdIndividual(List<HdssHouseholdIndividual> householdIndividuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    householdIndividuals.forEach(
        householdIndividual -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(HOUSEHOLD_ID, householdIndividual.getHouseholdId());
          contentValues.put(INDIVIDUAL_ID, householdIndividual.getIndividualId());
          contentValues.put(SERVER_VERSION, householdIndividual.getServerVersion());
          writableDatabase.replace(HDSS_HOUSEHOLD_INDIVIDUAL, null, contentValues);
        });
  }

  public void deleteFromHouseholdIndividualByIndividualIds(List<String> individuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction for better performance and atomicity
    writableDatabase.beginTransaction();

    try {
      int batchSize = 30; // Batch size of 30 items
      for (int i = 0; i < individuals.size(); i += batchSize) {
        int end = Math.min(i + batchSize, individuals.size());
        List<String> batch = individuals.subList(i, end);

        // Build the WHERE clause with IN for the batch
        StringBuilder whereClause = new StringBuilder("individual_id IN (");
        for (int j = 0; j < batch.size(); j++) {
          whereClause.append("?");
          if (j < batch.size() - 1) {
            whereClause.append(", ");
          }
        }
        whereClause.append(")");

        // Prepare and execute the delete statement
        String[] args = new String[batch.size()];
        for (int j = 0; j < batch.size(); j++) {
          args[j] = batch.get(j);
        }

        writableDatabase.delete(HDSS_HOUSEHOLD_INDIVIDUAL, whereClause.toString(), args);
      }
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      e.printStackTrace();
    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }

  public void deleteFromCompoundHouseholdByHouseholdIds(List<String> householdIds) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction for better performance and atomicity
    writableDatabase.beginTransaction();

    try {
      int batchSize = 30; // Batch size of 30 items
      for (int i = 0; i < householdIds.size(); i += batchSize) {
        int end = Math.min(i + batchSize, householdIds.size());
        List<String> batch = householdIds.subList(i, end);

        // Build the WHERE clause with IN for the batch
        StringBuilder whereClause = new StringBuilder("household_id IN (");
        for (int j = 0; j < batch.size(); j++) {
          whereClause.append("?");
          if (j < batch.size() - 1) {
            whereClause.append(", ");
          }
        }
        whereClause.append(")");

        // Prepare and execute the delete statement
        String[] args = new String[batch.size()];
        for (int j = 0; j < batch.size(); j++) {
          args[j] = batch.get(j);
        }

        writableDatabase.delete(HDSS_COMPOUND_HOUSEHOLD, whereClause.toString(), args);
      }
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      e.printStackTrace();
    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }


    public void deleteFromHouseholdStructureByHouseholdIds(List<String> householdIds) {
        SQLiteDatabase writableDatabase = getWritableDatabase();

        // Start a transaction for better performance and atomicity
        writableDatabase.beginTransaction();

        try {
            int batchSize = 30; // Batch size of 30 items
            for (int i = 0; i < householdIds.size(); i += batchSize) {
                int end = Math.min(i + batchSize, householdIds.size());
                List<String> batch = householdIds.subList(i, end);

                // Build the WHERE clause with IN for the batch
                StringBuilder whereClause = new StringBuilder("household_id IN (");
                for (int j = 0; j < batch.size(); j++) {
                    whereClause.append("?");
                    if (j < batch.size() - 1) {
                        whereClause.append(", ");
                    }
                }
                whereClause.append(")");

                // Prepare and execute the delete statement
                String[] args = new String[batch.size()];
                for (int j = 0; j < batch.size(); j++) {
                    args[j] = batch.get(j);
                }

                writableDatabase.delete(HDSS_HOUSEHOLD_STRUCTURE, whereClause.toString(), args);
            }
            writableDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
        } finally {
            // End the transaction, whether successful or not
            writableDatabase.endTransaction();
        }
    }

  public void addOrUpdateHouseholdIndividualBatched(
      List<HdssHouseholdIndividual> householdIndividuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction for better performance and atomicity
    writableDatabase.beginTransaction();
    try {
      int batchSize = 30; // Batch size of 30 items
      for (int i = 0; i < householdIndividuals.size(); i += batchSize) {
        // Get the sublist representing the current batch (up to 30 items)
        int end = Math.min(i + batchSize, householdIndividuals.size());
        List<HdssHouseholdIndividual> batch = householdIndividuals.subList(i, end);

        // Build the SQL query for batch insert
        StringBuilder sql =
            new StringBuilder(
                "REPLACE INTO "
                    + HDSS_HOUSEHOLD_INDIVIDUAL
                    + " ("
                    + HOUSEHOLD_ID
                    + ", "
                    + INDIVIDUAL_ID
                    + ", "
                    + SERVER_VERSION
                    + ") VALUES ");

        // Add values for each household individual in the batch
        for (int j = 0; j < batch.size(); j++) {
          HdssHouseholdIndividual householdIndividual = batch.get(j);
          sql.append("(")
              .append("'")
              .append(householdIndividual.getHouseholdId())
              .append("'")
              .append(", ")
              .append("'")
              .append(householdIndividual.getIndividualId())
              .append("'")
              .append(", ")
              .append(householdIndividual.getServerVersion())
              .append(")");

          if (j < batch.size() - 1) {
            sql.append(", "); // Separate values with commas, except for the last one
          }
        }

        // Execute the batch insert for this batch
        writableDatabase.execSQL(sql.toString());
      }

      // Commit the transaction after all batches have been processed
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      e.printStackTrace();
    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }

  public int getMaxServerVersion() {
    SQLiteDatabase readableDatabase = getReadableDatabase();
    String query =
        "SELECT MAX(hdss.server_version) as server_version from (\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_individual hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_household_individual hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_household_structure hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_compound hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_compound_household hdss\n"
            + ") as hdss ";

    Cursor cursor;
    try {
      cursor = readableDatabase.rawQuery(query, new String[] {});
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
    String query =
        "SELECT MIN(hdss.server_version) as server_version from (\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_individual hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_household_individual hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_household_structure hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_compound hdss\n"
            + "UNION ALL\n"
            + "SELECT MAX(hdss.server_version) as server_version from hdss_compound_household hdss\n"
            + ") as hdss ";

    Cursor cursor;
    try {
      cursor = readableDatabase.rawQuery(query, new String[] {});
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

    query =
        "SELECT \n"
            + "hi.identifier,\n"
            + "hi.individual_id,\n"
            + "hi.dob,\n"
            + "hi.gender,\n"
            + "hi."
            + NAME
            + ",\n"
            + "hi."
            + CLUSTER
            + ",\n"
            + "hi.server_version,\n"
            + "hhi.household_id,\n"
            + "hhs.structure_id,\n"
            + "hc.compound_id,\n"
            + "hi."
            + FLOATING_LOCATION_ID
            + ",\n"
            + "hi."
            + FLOATING_LOCATION_NAME
            + ",\n"
            + "hi."
            + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
            + ",\n"
            + "hh."
            + FLOATING_HOUSEHOLD_LOCATION_NAME
            + " from hdss_individual hi\n"
            + "left join hdss_household_individual hhi on hhi.individual_id = hi.individual_id\n"
            + "left join hdss_household hh on hhi.household_id = hh.household_id \n"
            + "left join hdss_compound_household hch on hch.household_id = hhi.household_id\n"
            + "left join hdss_household_structure hhs on hhs.household_id = hhi.household_id\n"
            + "left join hdss_compound hc on hc.compound_id = hch.compound_id \n"
            + "WHERE hi.server_version > ? \n"
            + "OR hi.server_version > ? \n"
            + "OR hhi.server_version > ? \n"
            + "OR hch.server_version > ? \n"
            + "OR hhs.server_version > ? \n"
            + "OR hh.server_version > ? \n"
            + "OR hc.server_version > ?";

    Timber.tag("reveal_individual").i("q %s %s",query,serverVersion);
    try {
      cursor =
          readableDatabase.rawQuery(
              query,
              new String[] {
                String.valueOf(serverVersion),
                String.valueOf(serverVersion),
                String.valueOf(serverVersion),
                String.valueOf(serverVersion),
                String.valueOf(serverVersion),
                String.valueOf(serverVersion)
              });

      if (cursor != null) {
        if (cursor.moveToFirst()) {
          do {
            String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

            String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

            String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

            String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

            String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

            String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));

            String structureId = cursor.getString(cursor.getColumnIndexOrThrow(STRUCTURE_ID));

            String floatingLocationId =
                cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
            String floatingLocationName =
                cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
            String floatingLocationGeographicLevel =
                cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));

            String floatingHouseholdLocationName =
                cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_HOUSEHOLD_LOCATION_NAME));

            int serverVersionFromDB = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

            String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

            HdssIndividualHouseHoldCompound hdssIndividual =
                new HdssIndividualHouseHoldCompound(
                    identifier,
                    individualId,
                    dob,
                    gender,
                    householdId,
                    compoundId,
                    structureId,
                    serverVersionFromDB,
                    name,
                    floatingLocationId,
                    floatingLocationName,
                    floatingLocationGeographicLevel,
                    floatingHouseholdLocationName,
                    cluster);

            Timber.tag("reveal_individual").i("i %s",hdssIndividual);
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

  public List<HdssIndividualHouseHoldCompound> searchHouseholdIndividual2(
      String searchtext, String genderSearch, String dob, String name) {
    Timber.tag("searching")
        .i("searchtext %s genderSearch %s dob %s", searchtext, genderSearch, dob);
    SQLiteDatabase readableDatabase = getReadableDatabase();

    String query;
    Cursor cursor = null;

    if (dob != null && genderSearch != null && searchtext != null && name != null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender, hi."
              + NAME
              + "   from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)"
              + " and lower(hi.gender) = lower(?) and hi.dob=? \n";
      cursor =
          readableDatabase.rawQuery(
              query,
              new String[] {
                "%" + searchtext + "%",
                "%" + searchtext + "%",
                "%" + searchtext + "%",
                genderSearch,
                dob
              });
    }

    if (dob != null && genderSearch == null && searchtext != null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender, hi."
              + NAME
              + " from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)"
              + "  and hi.dob=?\n";
      cursor =
          readableDatabase.rawQuery(
              query,
              new String[] {
                "%" + searchtext + "%", "%" + searchtext + "%", "%" + searchtext + "%", dob
              });
    }

    if (dob == null && genderSearch == null && searchtext != null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender, hi."
              + NAME
              + " from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)"
              + "  \n";
      cursor =
          readableDatabase.rawQuery(
              query,
              new String[] {
                "%" + searchtext + "%", "%" + searchtext + "%", "%" + searchtext + "%"
              });
    }

    if (dob == null && genderSearch != null && searchtext != null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender,hi."
              + NAME
              + " from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE (hi.individual_id like ? or hhi.household_id like ? or hc.compound_id like ?)"
              + " and lower(hi.gender) = lower(?) \n";
      cursor =
          readableDatabase.rawQuery(
              query,
              new String[] {
                "%" + searchtext + "%", "%" + searchtext + "%", "%" + searchtext + "%", genderSearch
              });
    }

    if (dob != null && genderSearch == null && searchtext == null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender,hi."
              + NAME
              + " from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE  hi.dob=?\n";
      cursor = readableDatabase.rawQuery(query, new String[] {dob});
    }

    if (dob != null && genderSearch != null && searchtext == null) {
      query =
          "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender,hi."
              + NAME
              + " from hdss_individual hi  \n"
              + "                left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id \n"
              + "                left join hdss_compound_household hch on hch.household_id = hhi.household_id \n"
              + "                left join hdss_compound hc on hc.compound_id = hch.compound_id\n"
              + " WHERE  lower(hi.gender) = lower(?) and hi.dob=?\n";
      cursor = readableDatabase.rawQuery(query, new String[] {genderSearch, dob});
    }

    List<HdssIndividualHouseHoldCompound> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

          String dobStr = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

          String nameStr = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));

          HdssIndividualHouseHoldCompound hdssIndividual =
              new HdssIndividualHouseHoldCompound(
                  identifier, individualId, dobStr, gender, householdId, compoundId, nameStr,cluster);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<HdssIndividualHouseHoldCompound> searchHouseholdIndividual(
      String searchtext, String genderSearch, String dob, String name, String cluster, String startAge, String endAge,boolean useAgeRange, boolean useExactDate) {
    Timber.tag("searching")
        .i("searchtext %s genderSearch %s dob %s startAge %s endAge %s", searchtext, genderSearch, dob,startAge,endAge);
    SQLiteDatabase readableDatabase = getReadableDatabase();

    String query =
        "SELECT hi.identifier, hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender, hi."
            + NAME
            + ",hi." + CLUSTER
            + " "
            + "FROM hdss_individual hi "
            + "LEFT JOIN hdss_household_individual hhi ON hi.individual_id = hhi.individual_id "
            + "LEFT JOIN hdss_compound_household hch ON hch.household_id = hhi.household_id "
            + "left join hdss_household_structure hhs on hhs.household_id = hhi.household_id "
            + "LEFT JOIN hdss_compound hc ON hc.compound_id = hch.compound_id ";

    // List to hold WHERE conditions dynamically
    List<String> whereClauses = new ArrayList<>();

    // List to hold selection arguments for the query
    List<String> args = new ArrayList<>();

    // Handle searchtext if not null
    if (searchtext != null && !searchtext.isEmpty()) {
      whereClauses.add(
          "(hi.individual_id LIKE ? OR hhi.household_id LIKE ? OR hc.compound_id LIKE ?)");
      String searchPattern = "%" + searchtext.trim() + "%";
      args.add(searchPattern);
      args.add(searchPattern);
      args.add(searchPattern);
    }

    // Handle genderSearch if not null
    if (genderSearch != null && !genderSearch.isEmpty()) {
      whereClauses.add("LOWER(hi.gender) = LOWER(?)");
      args.add(genderSearch);
    }

    if (useExactDate) {
      // Handle dob if not null
      if (dob != null && !dob.isEmpty()) {
        whereClauses.add("hi.dob = ?");
        args.add(dob);
      }
    }
    if (useAgeRange){
      if (startAge!=null && endAge!= null){

        String start = "'-"+startAge.trim()+" years'";
        String end = "'-"+endAge.trim()+" years'";
        Timber.tag("hdsssearch").i("HdssRepository startAge %s endAge %s",start,end);
        whereClauses.add("hi.dob BETWEEN DATE('now', "+end+") AND DATE('now', "+start+")");

      }
    }


    if (name != null && !name.isEmpty()) {
      StringBuilder orString = new StringBuilder();
      orString
          .append("(LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", " ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "  ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "   ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "    ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "     ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "      ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) OR ");
      args.add("%" + name.trim().replaceAll("\\s+", "       ") + "%");
      orString
          .append("LOWER(hi." + NAME + ") LIKE LOWER(?) ) ");
      args.add("%" + name.trim().replaceAll("\\s+", "        ") + "%");

      whereClauses.add(orString.toString());
    }


    if (cluster != null && !cluster.isEmpty()) {
      whereClauses.add("LOWER(hi." + CLUSTER + ") LIKE LOWER(?)");
      args.add(cluster + "%");
    }

    whereClauses.add("hhs.structure_id IS NOT NULL");

    // If there are conditions, join them with AND
    if (!whereClauses.isEmpty()) {
      query += " WHERE " + String.join(" AND ", whereClauses);
    }

    // Convert args list to an array
    String[] selectionArgs = args.toArray(new String[0]);

    // Execute the query
    Cursor cursor = readableDatabase.rawQuery(query, selectionArgs);
    Timber.tag("searching")
        .i("query %s %s", query,selectionArgs);
    // Process the results
    List<HdssIndividualHouseHoldCompound> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow("identifier"));
          String individualId = cursor.getString(cursor.getColumnIndexOrThrow("individual_id"));
          String dobStr = cursor.getString(cursor.getColumnIndexOrThrow("dob"));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
          String householdId = cursor.getString(cursor.getColumnIndexOrThrow("household_id"));
          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow("compound_id"));
          String nameStr = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          String clusterStr = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));
          // Create the object
          HdssIndividualHouseHoldCompound hdssIndividual =
              new HdssIndividualHouseHoldCompound(
                  identifier, individualId, dobStr, gender, householdId, compoundId, nameStr, clusterStr);

          // Add to the list
          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }

    return values;
  }

  public void addOrUpdateIndividual(List<HdssIndividual> householdIndividuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    householdIndividuals.forEach(
        individual -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(IDENTIFIER, individual.getIdentifier());
          contentValues.put(INDIVIDUAL_ID, individual.getIndividualId());
          contentValues.put(DOB, individual.getDob());
          contentValues.put(GENDER, individual.getGender());
          contentValues.put(NAME, individual.getName());
          contentValues.put(SERVER_VERSION, individual.getServerVersion());
          if (individual.getFloatingLocationName() != null) {
            contentValues.put(FLOATING_LOCATION_NAME, individual.getFloatingLocationName());
          }
          writableDatabase.replace(HDSS_INDIVIDUAL, null, contentValues);
        });
  }

    public void addOrUpdateHousehold(List<HdssHousehold> households) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        households.forEach(
            hdssHousehold -> {
              Timber.tag("RevealMap").i("content values %s",households.toString());

                ContentValues contentValues = new ContentValues();
                contentValues.put(HOUSEHOLD_ID, hdssHousehold.getHouseholdId());
                contentValues.put(SERVER_VERSION, hdssHousehold.getServerVersion());
                if (hdssHousehold.getFloatingHouseholdLocationName() != null) {
                    contentValues.put(FLOATING_HOUSEHOLD_LOCATION_NAME, hdssHousehold.getFloatingHouseholdLocationName());
                }
              Timber.tag("RevealMap").i("content values %s",contentValues.toString());
                writableDatabase.replace(HDSS_HOUSEHOLD, null, contentValues);
            });
    }

  public void addOrUpdateIndividualBatched(List<HdssIndividual> householdIndividuals) {
    Timber.tag("TotalCount").e("writing to db no of individs %s",householdIndividuals.size());
    SQLiteDatabase writableDatabase = getWritableDatabase();

    // Start a transaction to ensure atomicity and improve performance
    writableDatabase.beginTransaction();
    try {
      int batchSize = 30; // Batch size of 30 items
      for (int i = 0; i < householdIndividuals.size(); i += batchSize) {
        // Get the sublist representing the current batch (up to 30 items)
        int end = Math.min(i + batchSize, householdIndividuals.size());
        List<HdssIndividual> batch = householdIndividuals.subList(i, end);

        // Build the SQL query for batch insert
        StringBuilder sql =
            new StringBuilder(
                "REPLACE INTO "
                    + HDSS_INDIVIDUAL
                    + " ("
                    + IDENTIFIER
                    + ", "
                    + INDIVIDUAL_ID
                    + ", "
                    + DOB
                    + ", "
                    + GENDER
                    + ", "
                    + NAME
                    + ", "
                    + CLUSTER
                    + ", "
                    + FLOATING_LOCATION_ID
                    + ", "
                    + FLOATING_LOCATION_NAME
                    + ", "
                    + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
                    + ", "
                    + SERVER_VERSION
                    + ") VALUES ");

        // Add values for each individual in the batch
        for (int j = 0; j < batch.size(); j++) {
          HdssIndividual individual = batch.get(j);
          sql.append("(")
              .append("'")
              .append(individual.getIdentifier())
              .append("'")
              .append(", ")
              .append("'")
              .append(individual.getIndividualId().replace("'",""))
              .append("'")
              .append(", ")
              .append("'")
              .append(individual.getDob().replace("'",""))
              .append("'")
              .append(", ")
              .append("'")
              .append(individual.getGender().replace("'",""))
              .append("'")
              .append(", ")
              .append("'")
              .append(individual.getName().replace("'",""))
              .append("'");


          if (individual.getCluster() != null) {
            sql.append(", ")
                .append("'")
                .append(individual.getCluster().replace("'",""))
                .append("'");
          } else {
            sql.append(", ")
                .append("NULL");
          }

          if (individual.getFloatingLocationName() != null) {
            sql.append(", ")
                .append("'")
                .append(individual.getFloatingLocationId())
                .append("'")
                .append(", ")
                .append("'")
                .append(individual.getFloatingLocationName().replace("'",""))
                .append("'")
                .append(", ")
                .append("'")
                .append(individual.getFloatingLocationGeographicLevel())
                .append("'")
                .append(", ");
          } else {
            sql.append(", ")
                .append("NULL")
                .append(", ")
                .append("NULL")
                .append(", ")
                .append("NULL")
                .append(", ");
          }

          sql.append(individual.getServerVersion()).append(")");

          if (j < batch.size() - 1) {
            sql.append(", "); // Separate values with commas, except for the last one
          }
        }

        // Execute the batch insert for this batch
        writableDatabase.execSQL(sql.toString());
      }

      // Commit the transaction after all batches have been processed
      writableDatabase.setTransactionSuccessful();
    } catch (Exception e) {
      // Handle any exceptions
      Timber.tag("TotalCount").e("db exception %s",e.getMessage());

    } finally {
      // End the transaction, whether successful or not
      writableDatabase.endTransaction();
    }
  }

  public void addOrUpdateLocalSearchResults(
      List<HdssIndividualHouseHoldCompound> householdIndividuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    householdIndividuals.forEach(
        individual -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(IDENTIFIER, individual.getIdentifier());
          contentValues.put(INDIVIDUAL_ID, individual.getIndividualId());
          contentValues.put(DOB, individual.getDob());
          contentValues.put(GENDER, individual.getGender());
          contentValues.put(COMPOUND_ID, individual.getCompoundId());
          contentValues.put(HOUSEHOLD_ID, individual.getHouseholdId());
          contentValues.put(NAME, individual.getName());
          contentValues.put(CLUSTER,individual.getCluster());
          contentValues.put(SERVER_VERSION, 0);

          Timber.tag("searching").e("retrieved contentValues %s",contentValues);

          writableDatabase.replace(HDSS_SEARCH_RESULTS, null, contentValues);
        });
  }

  public void addOrUpdateSearchResults(
      List<HdssSearchBoxFactory.SearchResponse> householdIndividuals) {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    householdIndividuals.forEach(
        individual -> {
          ContentValues contentValues = new ContentValues();
          contentValues.put(IDENTIFIER, individual.getId());
          contentValues.put(INDIVIDUAL_ID, individual.getIndividualId());
          contentValues.put(DOB, individual.getDob());
          contentValues.put(GENDER, individual.getGender());
          contentValues.put(COMPOUND_ID, individual.getCompoundId());
          contentValues.put(HOUSEHOLD_ID, individual.getHouseholdId());
          contentValues.put(NAME, individual.getName());
          contentValues.put(CLUSTER,individual.getCluster());
          contentValues.put(SERVER_VERSION, 0);
          writableDatabase.replace(HDSS_SEARCH_RESULTS, null, contentValues);
        });
  }

  public void deleteSearchResultsData() {
    SQLiteDatabase writableDatabase = getWritableDatabase();
    writableDatabase.execSQL("DELETE FROM " + HDSS_SEARCH_RESULTS);
  }

  public List<HdssSearchBoxFactory.SearchResponse> getSearchResultsInBatches(
      int limit, int offset) {

    String query =
        "SELECT * from " + HDSS_SEARCH_RESULTS + " order by " + INDIVIDUAL_ID + " LIMIT ? OFFSET ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor =
        db.rawQuery(query, new String[] {String.valueOf(limit), String.valueOf(offset)});
    List<HdssSearchBoxFactory.SearchResponse> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));

          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));

          HdssSearchBoxFactory.SearchResponse hdssIndividual =
              new HdssSearchBoxFactory.SearchResponse(
                  identifier, individualId, compoundId, householdId, dob, gender, name,cluster);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
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

    String query =
        "SELECT hc."
            + HOUSEHOLD_ID
            + " FROM "
            + HDSS_COMPOUND_HOUSEHOLD
            + " hc WHERE hc."
            + COMPOUND_ID
            + " = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {compoundId});
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

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ", i."
            + NAME
            + " , i.server_version From "
            + HDSS_HOUSEHOLD_INDIVIDUAL
            + " hc \n"
            + "left join "
            + HDSS_INDIVIDUAL
            + " i on i."
            + INDIVIDUAL_ID
            + " = hc."
            + INDIVIDUAL_ID
            + "\n"
            + "where hc."
            + HOUSEHOLD_ID
            + " = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {householdId});
    List<HdssIndividual> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssIndividual hdssIndividual =
              new HdssIndividual(identifier, individualId, dob, gender, name, serverVersion);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<HdssIndividual> getIndividuals() {

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ", i."
            + NAME
            + ", i.server_version"
            + " From "
            + HDSS_INDIVIDUAL
            + " i";

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
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssIndividual hdssIndividual =
              new HdssIndividual(identifier, individualId, dob, gender, name, serverVersion);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<HdssIndividual> getIndividualsForOperationalArea(String operationalAreaName) {

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ", i."
            + NAME
            + ", i.server_version"
            + ""
            + " From "
            + HDSS_INDIVIDUAL
            + " i WHERE i."
            + FLOATING_LOCATION_NAME
            + " = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {operationalAreaName});
    List<HdssIndividual> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));

          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));

          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          //
          //                    String floatingLocationId =
          // cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
          //
          //                    String floatingLocationName =
          // cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
          //
          //                    String floatingLocationGeographicLevel =
          // cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssIndividual hdssIndividual =
              new HdssIndividual(identifier, individualId, dob, gender, name, serverVersion);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<HdssHousehold> getHouseholdsForOperationalArea(String operationalAreaName) {

    String query =
        "SELECT "
            + "h." + HOUSEHOLD_ID + ", "
            + "h." + FLOATING_HOUSEHOLD_LOCATION_NAME + ", "
            + "h." + SERVER_VERSION
            + " From "
            + HDSS_HOUSEHOLD
            + " h WHERE h."
            + FLOATING_HOUSEHOLD_LOCATION_NAME
            + " = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {operationalAreaName});
    List<HdssHousehold> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
          String floatingHouseholdLocationName = cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_HOUSEHOLD_LOCATION_NAME));
          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));
          HdssHousehold hdssHousehold =
              HdssHousehold.builder()
                  .householdId(householdId)
                  .floatingHouseholdLocationName(floatingHouseholdLocationName)
                  .serverVersion(serverVersion)
                  .build();

          values.add(hdssHousehold);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public int getCountOfIndividuals() {

    String query = "SELECT count(*) as count" + " From " + HDSS_INDIVIDUAL + " i";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, null);
    int count = 0;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
        cursor.close();
      }
    }
    return count;
  }

  public List<HdssIndividualHouseHoldCompound> getIndividualHouseholdCompound() {

    String query =
        "SELECT hi.identifier ,hi.individual_id, hhi.household_id, hc.compound_id, hi.dob, hi.gender, "
            + "hi."
            + FLOATING_LOCATION_ID
            + ", hi."
            + FLOATING_LOCATION_NAME
            + ", hi."
            + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
            + ", hi."
            + CLUSTER
            + ", hh."
            + FLOATING_HOUSEHOLD_LOCATION_NAME
            + " from hdss_individual hi  "
            + "left join hdss_household_individual hhi on hi.individual_id = hhi.individual_id "
            + "left join hdss_household hh on hh.household_id = hhi.household_id "
            + "left join hdss_compound_household hch on hch.household_id = hhi.household_id "
            + "left join hdss_compound hc on hc.compound_id = hch.compound_id";

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
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));

          int serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          String floatingLocationId =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
          String floatingLocationName =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
          String floatingLocationGeographicLevel =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));
            String floatingHouseholdLocationName =
                cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_HOUSEHOLD_LOCATION_NAME));

          HdssIndividualHouseHoldCompound hdssIndividual =
              new HdssIndividualHouseHoldCompound(
                  identifier,
                  individualId,
                  dob,
                  gender,
                  householdId,
                  compoundId,
                  null,
                  serverVersion,
                  name,
                  floatingLocationId,
                  floatingLocationName,
                  floatingLocationGeographicLevel,
                  floatingHouseholdLocationName,
                  cluster);

          values.add(hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public Set<HdssCompoundHousehold> getHouseholdCompound() {

    String query = "SELECT compound_id, household_id, server_version  from hdss_compound_household";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, null);
    Set<HdssCompoundHousehold> values = new HashSet<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {

          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssCompoundHousehold compoundHousehold =
              new HdssCompoundHousehold(compoundId, householdId, serverVersion);

          values.add(compoundHousehold);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public Set<HdssTask> getTasksByStructure(String structureId, String planIdentifier) {

    String query =
        "SELECT t.*, hc.*, hhi.household_id  from task t "
            + " left join hdss_individual hc on hc.identifier = t.for"
            + " inner join hdss_household_individual hhi on hhi.individual_id = hc.individual_id"
            + " where t.for in (\n"
            + " SELECT i.identifier\n"
            + "--,hi.*, hs.*\n"
            + " From hdss_individual i \n"
            + "inner join hdss_household_individual hi on hi.individual_id = i.individual_id\n"
            + "inner join hdss_household_structure hs on hi.household_id = hs.household_id\n"
            + "where hs.structure_id = ? and t.plan_id = ? \n"
            + ") and t.status <> 'CANCELLED' order by hc.individual_id ";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId, planIdentifier});

    Set<HdssTask> tasks = new HashSet<>();
    while (cursor.moveToNext()) {
      HdssTask task = readHdssTaskCursor(cursor);
      task.setStructureId(structureId);
      String household = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
      task.setHouseholdId(household);
      String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));
      task.setIndividualId(individualId);
      tasks.add(task);
    }

    return tasks;
  }

  public List<String> getTasksForCompoundLinkedToHouseholdId(
      String compoundId, String planIdentifier, String existingIndividual) {
    String query =
        "SELECT DISTINCT t._id, t.business_status , t.code, t.for from hdss_compound_household hch\n"
            + "left join hdss_compound_household hch2 on hch2.compound_id = hch.compound_id\n"
            + "left join hdss_household_structure hhs on hhs.household_id = hch2.household_id\n"
            + "left join hdss_household_individual  hhi on hhi.household_id  = hhs.household_id \n"
            + "left join hdss_individual hi on hi.individual_id = hhi.individual_id\n"
            + "left join task t on t.for = hhs.structure_id or hi.identifier = t.for\n"
            + "WHERE hch.compound_id =  ? and t.plan_id = ? and hhi.individual_id <> ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor =
        db.rawQuery(query, new String[] {compoundId, planIdentifier, existingIndividual});
    List<String> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          values.add(taskId);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<String> getStructureTasksForCompoundLinkedToHouseholdId(
      String compoundId, String planIdentifier) {
    String query =
        "SELECT DISTINCT t._id, t.business_status , t.code, t.for from hdss_compound_household hch \n"
            + "                left join hdss_compound_household hch2 on hch2.compound_id = hch.compound_id \n"
            + "                left join hdss_household_structure hhs on hhs.household_id = hch2.household_id \n"
            + "                left join hdss_household_individual  hhi on hhi.household_id  = hhs.household_id  \n"
            + "                left join hdss_individual hi on hi.individual_id = hhi.individual_id \n"
            + "                left join task t on t.for = hhs.structure_id\n"
            + "                WHERE hch.compound_id =  ? and t.plan_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {compoundId, planIdentifier});
    List<String> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          values.add(taskId);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<String> getIndividualTasksForCompoundExcludingHousehold(
      String compoundId, String household, String planIdentifier) {
    String query =
        "SELECT DISTINCT t._id, t.business_status , t.code, t.for, hch2.compound_id,hhi.household_id , hhs.structure_id from hdss_compound_household hch \n"
            + "                left join hdss_compound_household hch2 on hch2.compound_id = hch.compound_id \n"
            + "                left join hdss_household_structure hhs on hhs.household_id = hch2.household_id \n"
            + "                left join hdss_household_individual  hhi on hhi.household_id  = hhs.household_id  \n"
            + "                left join hdss_individual hi on hi.individual_id = hhi.individual_id \n"
            + "                left join task t on hi.identifier = t.for \n"
            + "                WHERE hch.compound_id = ? and hhi.household_id <> ? and t.plan_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {compoundId, household, planIdentifier});
    List<String> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          values.add(taskId);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<StructureTaskForCompound> getStructuresAndTasksForCompoundByHouseholdId(
      String householdId, String planIdentifier) {
    String query =
        "SELECT DISTINCT hhs.structure_id"
            + ", t._id"
            + ", t.business_status"
            + ", t.code"
            + ", t.status"
            + ", hch.household_id"
            + ", hch.compound_id "
            + "FROM hdss_compound_household hch\n"
            + "left join hdss_compound_household hch2 on hch2.compound_id = hch.compound_id\n"
            + "LEFT join hdss_household_structure hhs on hch2.household_id = hhs.household_id\n"
            + "left join task t on t.for = hhs.structure_id AND t.status!='CANCELLED'\n"
            + "WHERE hch.household_id = ? and (t.plan_id = ?)";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {householdId, planIdentifier});
    List<StructureTaskForCompound> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          String structureId = cursor.getString(cursor.getColumnIndexOrThrow("structure_id"));
          String businessStatus = cursor.getString(cursor.getColumnIndexOrThrow("business_status"));
          String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
          String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
          String householdIdStr = cursor.getString(cursor.getColumnIndexOrThrow("household_id"));
          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow("compound_id"));

          StructureTaskForCompound task = new StructureTaskForCompound();
          task.setCompoundId(compoundId);
          task.setHouseholdId(householdIdStr);
          task.setCode(code);
          task.setTaskId(taskId);
          task.setStatus(status);
          task.setStructureId(structureId);
          task.setBusinessStatus(businessStatus);

          values.add(task);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<StructureTaskForCompound> getStructuresAndTasksForCompound(
      String compundId, String planIdentifier) {
    String query =
        "SELECT DISTINCT hhs.structure_id"
            + ", t._id"
            + ", t.business_status"
            + ", t.code"
            + ", t.status"
            + ", hch.household_id"
            + ", hch.compound_id "
            + "FROM hdss_compound_household hch \n"
            + "LEFT join hdss_household_structure hhs on hch.household_id = hhs.household_id\n"
            + "left join task t on t.for = hhs.structure_id\n"
            + "WHERE hch.compound_id = ? and (t.plan_id = ?)";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {compundId, planIdentifier});
    List<StructureTaskForCompound> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          String structureId = cursor.getString(cursor.getColumnIndexOrThrow("structure_id"));
          String businessStatus = cursor.getString(cursor.getColumnIndexOrThrow("business_status"));
          String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
          String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
          String householdIdStr = cursor.getString(cursor.getColumnIndexOrThrow("household_id"));
          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow("compound_id"));

          StructureTaskForCompound task = new StructureTaskForCompound();
          task.setCompoundId(compoundId);
          task.setHouseholdId(householdIdStr);
          task.setCode(code);
          task.setTaskId(taskId);
          task.setStatus(status);
          task.setStructureId(structureId);
          task.setBusinessStatus(businessStatus);

          values.add(task);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<IndividualsAndTasksForCompound> getIndividualsAndTasksForCompoundByHouseholdId(
      String householdId, String planIdentifier) {
    String query =
        "SELECT DISTINCT hi.identifier, hi.individual_id, hhi.household_id,t._id, t.business_status,t.code, t.status, hch.compound_id  FROM hdss_compound_household hch\n"
            + "left join hdss_compound_household hch2 on hch2.compound_id = hch.compound_id\n"
            + "left join hdss_household_individual hhi on hhi.household_id = hch2.household_id \n"
            + "left join hdss_individual hi on hi.individual_id = hhi.individual_id\n"
            + "left join task t on t.for = hi.identifier AND t.status != 'CANCELLED'\n"
            + "WHERE hch.household_id = ?  and (t.plan_id = ? or plan_id is null);";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {householdId, planIdentifier});
    List<IndividualsAndTasksForCompound> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String taskId = cursor.getString(cursor.getColumnIndexOrThrow(_ID));
          String individualIdentifier =
              cursor.getString(cursor.getColumnIndexOrThrow("identifier"));
          String individualId = cursor.getString(cursor.getColumnIndexOrThrow("individual_id"));
          String businessStatus = cursor.getString(cursor.getColumnIndexOrThrow("business_status"));
          String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
          String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
          String householdIdStr = cursor.getString(cursor.getColumnIndexOrThrow("household_id"));
          String compoundId = cursor.getString(cursor.getColumnIndexOrThrow("compound_id"));

          IndividualsAndTasksForCompound task = new IndividualsAndTasksForCompound();
          task.setIndividualIdentifier(individualIdentifier);
          task.setCompoundId(compoundId);
          task.setHouseholdId(householdIdStr);
          task.setCode(code);
          task.setTaskId(taskId);
          task.setStatus(status);
          task.setIndividualId(individualId);
          task.setBusinessStatus(businessStatus);

          values.add(task);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public String getHouseholdIdByStructureId(String structureId) {
    String query =
        "SELECT hs.household_id from hdss_household_structure hs\n"
            + "        WHERE hs.structure_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    String values = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        values = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
        cursor.close();
      }
    }
    return values;
  }

  public HdssHousehold getHdssHouseholdIdByHouseholdId(String householdId) {
    String query =
        "SELECT * from hdss_household hh\n" + "        WHERE hh.household_id = ? limit 1";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {householdId});

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        String household = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
        String floatingHouseholdLocationName =
            cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_HOUSEHOLD_LOCATION_NAME));
        long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));
        HdssHousehold householdObj =
            HdssHousehold.builder()
                .householdId(household)
                .floatingHouseholdLocationName(floatingHouseholdLocationName)
                .serverVersion(serverVersion)
                .build();
        cursor.close();
        return householdObj;
      }
    }
    return null;
  }

  public String getHouseholdIdByIndividualId(String individualId) {
    String query =
        "SELECT hhi.household_id from hdss_household_individual hhi \n"
            + "        WHERE hhi.individual_id = ? limit 1";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {individualId});
    String values = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        values = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
        cursor.close();
      }
    }
    return values;
  }

  public HdssCompoundHousehold getHouseholdIdCompoundIdByStructureId(String structureId) {
    String query =
        "SELECT hs.household_id, hch.compound_id, hs.server_version from hdss_household_structure hs"
            + " left join hdss_compound_household hch on hch.household_id = hs.household_id\n"
            + " WHERE hs.structure_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    HdssCompoundHousehold hdssCompoundHouseholds = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        hdssCompoundHouseholds =
            HdssCompoundHousehold.builder()
                .compoundId(cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID)))
                .householdId(cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID)))
                .serverVersion(cursor.getLong(cursor.getColumnIndexOrThrow(SERVER_VERSION)))
                .build();
        cursor.close();
      }
    }
    return hdssCompoundHouseholds;
  }

  public List<String> getHouseholdIdsByStructureId(String structureId) {
    String query =
        "SELECT hs.household_id from hdss_household_structure hs\n"
            + "        WHERE hs.structure_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    List<String> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));
        values.add(householdId);
      }
      cursor.close();
    }

    return values;
  }

  public String getStructureIdByHouseholdId(String householdId) {
    String query =
        "SELECT hs.structure_id from hdss_household_structure hs\n"
            + "        WHERE hs.household_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {householdId});
    String values = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        values = cursor.getString(cursor.getColumnIndexOrThrow(STRUCTURE_ID));
        cursor.close();
      }
    }
    return values;
  }

  public List<HdssCompoundHousehold> getCompoundAndHouseholdByStructureId(String structureId) {
    String query =
        "    SELECT hc.compound_id,hch.household_id, hhs.structure_id, hc.server_version from hdss_compound hc "
            + "    left join hdss_compound_household hch on hch.compound_id = hc.compound_id "
            + "    left join hdss_household_structure hhs on hhs.household_id = hch.household_id "
            + "    where structure_id = ? LIMIT 1 ";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    List<HdssCompoundHousehold> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

          String compound = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssCompoundHousehold hdssCompoundHousehold =
              new HdssCompoundHousehold(compound, householdId, serverVersion);
          values.add(hdssCompoundHousehold);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public List<HdssCompoundHousehold> getCompoundAndHouseholdListByStructureId(String structureId) {
    String query =
        "    SELECT hc.compound_id,hch.household_id, hhs.structure_id, hc.server_version from hdss_compound hc "
            + "    left join hdss_compound_household hch on hch.compound_id = hc.compound_id "
            + "    left join hdss_household_structure hhs on hhs.household_id = hch.household_id "
            + "    where structure_id = ? ";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    List<HdssCompoundHousehold> values = new ArrayList<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String householdId = cursor.getString(cursor.getColumnIndexOrThrow(HOUSEHOLD_ID));

          String compound = cursor.getString(cursor.getColumnIndexOrThrow(COMPOUND_ID));

          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          HdssCompoundHousehold hdssCompoundHousehold =
              new HdssCompoundHousehold(compound, householdId, serverVersion);
          values.add(hdssCompoundHousehold);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public Map<String, Boolean> getIsAHouseholdByStructureList(List<String> structureIds) {

    StringBuilder inClause = new StringBuilder();
    for (int i = 0; i < structureIds.size(); i++) {
      inClause.append("?");
      if (i < structureIds.size() - 1) {
        inClause.append(", ");
      }
    }
    String query =
        " SELECT hhs.structure_id from hdss_household_structure hhs where hhs.structure_id in ("
            + inClause
            + ")";

    SQLiteDatabase db = getReadableDatabase();

    List<String> parameters = new ArrayList<>();
    parameters.addAll(structureIds);

    Cursor cursor = db.rawQuery(query, parameters.toArray(new String[0]));
    Set<String> values = new HashSet<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String structureId = cursor.getString(cursor.getColumnIndexOrThrow(STRUCTURE_ID));
          values.add(structureId);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }

    Map<String, Boolean> map = new HashMap<>();
    for (String structure : structureIds) {
      if (values.contains(structure)) {
        map.put(structure, Boolean.TRUE);
      } else {
        map.put(structure, Boolean.FALSE);
      }
    }

    return map;
  }

  public Map<String, HdssIndividual> getIndividualsByStructureId(String structureId) {

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ",i."
            + NAME
            + ",i."
            + CLUSTER
            + ", i."
            + SERVER_VERSION
            + ","
            + "i."
            + FLOATING_LOCATION_ID
            + ", i."
            + FLOATING_LOCATION_NAME
            + ", i."
            + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
            + " From "
            + HDSS_INDIVIDUAL
            + " i "
            + " inner join hdss_household_individual hi on hi.individual_id = i.individual_id\n"
            + " inner join hdss_household_structure hs on hs.household_id = hi.household_id\n"
            + " WHERE hs.structure_id = ?";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {structureId});
    Map<String, HdssIndividual> values = new HashMap<>();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));
          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));
          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          String floatingLocationId =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
          String floatingLocationName =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
          String floatingLocationGeographicLevel =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));

          HdssIndividual hdssIndividual =
              new HdssIndividual(
                  identifier,
                  individualId,
                  dob,
                  gender,
                  name,
                  serverVersion,
                  floatingLocationId,
                  floatingLocationName,
                  floatingLocationGeographicLevel,
                  cluster);
          values.put(identifier, hdssIndividual);
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public HdssIndividual getIndividualsByHdssId(String id) {

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ", i."
            + SERVER_VERSION
            + ", i."
            + NAME
            + ", i."
            + CLUSTER
            + " ,"
            + "i."
            + FLOATING_LOCATION_ID
            + ", i."
            + FLOATING_LOCATION_NAME
            + ", i."
            + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
            + " From "
            + HDSS_INDIVIDUAL
            + " i "
            + " WHERE i.identifier = ? LIMIT 1";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {id});
    HdssIndividual values = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
          String individualId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));
          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));
          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          String floatingLocationId =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
          String floatingLocationName =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
          String floatingLocationGeographicLevel =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));

          HdssIndividual hdssIndividual =
              new HdssIndividual(
                  identifier,
                  individualId,
                  dob,
                  gender,
                  name,
                  serverVersion,
                  floatingLocationId,
                  floatingLocationName,
                  floatingLocationGeographicLevel,
                  cluster);
          cursor.close();
          return hdssIndividual;
        } while (cursor.moveToNext());
      }
      cursor.close();
    }
    return values;
  }

  public HdssIndividual getIndividualByIndividualId(String individualId) {

    String query =
        "SELECT "
            + " i."
            + IDENTIFIER
            + ","
            + "i."
            + INDIVIDUAL_ID
            + ", i."
            + DOB
            + ", i."
            + GENDER
            + ", i."
            + SERVER_VERSION
            + ", i."
            + NAME
            + ", i."
            + CLUSTER
            + " ,"
            + "i."
            + FLOATING_LOCATION_ID
            + ", i."
            + FLOATING_LOCATION_NAME
            + ", i."
            + FLOATING_LOCATION_GEOGRAPHIC_LEVEL
            + " From "
            + HDSS_INDIVIDUAL
            + " i "
            + " WHERE i."
            + INDIVIDUAL_ID
            + " = ? LIMIT 1";

    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[] {individualId});
    HdssIndividual values = null;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          String identifier = cursor.getString(cursor.getColumnIndexOrThrow(IDENTIFIER));
          String indId = cursor.getString(cursor.getColumnIndexOrThrow(INDIVIDUAL_ID));
          String dob = cursor.getString(cursor.getColumnIndexOrThrow(DOB));
          String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER));
          String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
          String cluster = cursor.getString(cursor.getColumnIndexOrThrow(CLUSTER));
          long serverVersion = cursor.getInt(cursor.getColumnIndexOrThrow(SERVER_VERSION));

          String floatingLocationId =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_ID));
          String floatingLocationName =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_NAME));
          String floatingLocationGeographicLevel =
              cursor.getString(cursor.getColumnIndexOrThrow(FLOATING_LOCATION_GEOGRAPHIC_LEVEL));

          HdssIndividual hdssIndividual =
              new HdssIndividual(
                  identifier,
                  indId,
                  dob,
                  gender,
                  name,
                  serverVersion,
                  floatingLocationId,
                  floatingLocationName,
                  floatingLocationGeographicLevel,
                  cluster);
          cursor.close();
          return hdssIndividual;
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
      task.setStatus(
          Task.TaskStatus.valueOf(cursor.getString(cursor.getColumnIndex(TaskRepository.STATUS))));
    }
    task.setBusinessStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.BUSINESS_STATUS)));
    task.setPriority(
        Task.TaskPriority.valueOf(
            cursor.getString(cursor.getColumnIndex(TaskRepository.PRIORITY))));
    task.setCode(cursor.getString(cursor.getColumnIndex(TaskRepository.CODE)));
    task.setDescription(cursor.getString(cursor.getColumnIndex(TaskRepository.DESCRIPTION)));
    task.setFocus(cursor.getString(cursor.getColumnIndex(TaskRepository.FOCUS)));
    task.setForEntity(cursor.getString(cursor.getColumnIndex(TaskRepository.FOR)));
    Period period = new Period();
    period.setStart(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.START))));
    period.setEnd(
        DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.END))));
    task.setExecutionPeriod(period);
    task.setAuthoredOn(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.AUTHORED_ON))));
    task.setLastModified(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.LAST_MODIFIED))));
    task.setOwner(cursor.getString(cursor.getColumnIndex(TaskRepository.OWNER)));
    task.setSyncStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.SYNC_STATUS)));
    task.setServerVersion(cursor.getLong(cursor.getColumnIndex(TaskRepository.SERVER_VERSION)));

    task.setReasonReference(
        cursor.getString(cursor.getColumnIndex(TaskRepository.REASON_REFERENCE)));
    task.setLocation(cursor.getString(cursor.getColumnIndex(TaskRepository.LOCATION)));
    task.setRequester(cursor.getString(cursor.getColumnIndex(TaskRepository.REQUESTER)));
    Period restrictionPeriod = new Period();
    restrictionPeriod.setStart(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_START))));
    restrictionPeriod.setEnd(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_END))));
    Task.Restriction restriction =
        new Task.Restriction(
            cursor.getInt(cursor.getColumnIndex(TaskRepository.RESTRICTION_REPEAT)),
            restrictionPeriod);
    if (restriction.getRepetitions() != 0
        || restrictionPeriod.getStart() != null && restrictionPeriod.getEnd() != null) {
      task.setRestriction(restriction);
    }
    return task;
  }

  public HdssTask readHdssTaskCursor(Cursor cursor) {
    HdssTask task = new HdssTask();
    task.setIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.ID)));
    task.setPlanIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.PLAN_ID)));
    task.setGroupIdentifier(cursor.getString(cursor.getColumnIndex(TaskRepository.GROUP_ID)));
    if (cursor.getString(cursor.getColumnIndex(TaskRepository.STATUS)) != null) {
      task.setStatus(
          Task.TaskStatus.valueOf(cursor.getString(cursor.getColumnIndex(TaskRepository.STATUS))));
    }
    task.setBusinessStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.BUSINESS_STATUS)));
    task.setPriority(
        Task.TaskPriority.valueOf(
            cursor.getString(cursor.getColumnIndex(TaskRepository.PRIORITY))));
    task.setCode(cursor.getString(cursor.getColumnIndex(TaskRepository.CODE)));
    task.setDescription(cursor.getString(cursor.getColumnIndex(TaskRepository.DESCRIPTION)));
    task.setFocus(cursor.getString(cursor.getColumnIndex(TaskRepository.FOCUS)));
    task.setForEntity(cursor.getString(cursor.getColumnIndex(TaskRepository.FOR)));
    Period period = new Period();
    period.setStart(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.START))));
    period.setEnd(
        DateUtil.getDateTimeFromMillis(cursor.getLong(cursor.getColumnIndex(TaskRepository.END))));
    task.setExecutionPeriod(period);
    task.setAuthoredOn(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.AUTHORED_ON))));
    task.setLastModified(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.LAST_MODIFIED))));
    task.setOwner(cursor.getString(cursor.getColumnIndex(TaskRepository.OWNER)));
    task.setSyncStatus(cursor.getString(cursor.getColumnIndex(TaskRepository.SYNC_STATUS)));
    task.setServerVersion(cursor.getLong(cursor.getColumnIndex(TaskRepository.SERVER_VERSION)));

    task.setReasonReference(
        cursor.getString(cursor.getColumnIndex(TaskRepository.REASON_REFERENCE)));
    task.setLocation(cursor.getString(cursor.getColumnIndex(TaskRepository.LOCATION)));
    task.setRequester(cursor.getString(cursor.getColumnIndex(TaskRepository.REQUESTER)));
    Period restrictionPeriod = new Period();
    restrictionPeriod.setStart(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_START))));
    restrictionPeriod.setEnd(
        DateUtil.getDateTimeFromMillis(
            cursor.getLong(cursor.getColumnIndex(TaskRepository.RESTRICTION_END))));
    Task.Restriction restriction =
        new Task.Restriction(
            cursor.getInt(cursor.getColumnIndex(TaskRepository.RESTRICTION_REPEAT)),
            restrictionPeriod);
    if (restriction.getRepetitions() != 0
        || restrictionPeriod.getStart() != null && restrictionPeriod.getEnd() != null) {
      task.setRestriction(restriction);
    }
    return task;
  }
}
