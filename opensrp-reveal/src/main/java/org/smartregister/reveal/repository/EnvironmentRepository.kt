package org.smartregister.reveal.repository

import net.sqlcipher.database.SQLiteDatabase
import org.smartregister.repository.DrishtiRepository

class EnvironmentRepository : DrishtiRepository() {

    companion object {

        private const val ENVIRONMENT_TABLE_NAME = "environments"
        private const val ENVIRONMENT_KEY_COLUMN = "key"
        private const val ENVIRONMENT_VALUE_COLUMN = "value"
        private const val ENVIRONMENT_VERSION_COLUMN = "version"
        private const val ENVIRONMENT_SYNC_STATUS_COLUMN = "sync_status"
        const val ENVIRONMENT_SQL =
            "CREATE TABLE $ENVIRONMENT_TABLE_NAME($ENVIRONMENT_KEY_COLUMN VARCHAR PRIMARY KEY, $ENVIRONMENT_VALUE_COLUMN BLOB,$ENVIRONMENT_VERSION_COLUMN TEXT,$ENVIRONMENT_SYNC_STATUS_COLUMN TEXT)"
    }

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(ENVIRONMENT_SQL)
    }
}