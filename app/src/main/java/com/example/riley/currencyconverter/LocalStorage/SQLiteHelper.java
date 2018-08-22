package com.example.riley.currencyconverter.LocalStorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DEFAULT_DATABASE_NAME = "ITEM_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private String table;
    private String[] columns;
    private String[] types;

    /**
     * Construct a SQLiteOpenHelper for table in the default database
     *
     * @param context Application context
     * @param table Name of table in database
     */
    public SQLiteHelper(Context context, String table, String[] columns, String[] types) {
        this(context, DEFAULT_DATABASE_NAME, table, columns, types);
    }

    /**
     * Construct a SQLiteOpenHelper for databaseName.db
     *
     * @param context Application context
     * @param databaseName Name of database file
     * @param table Name of table in database
     * @param columns Array of column names
     * @param types Array of associated types
     */
    public SQLiteHelper(Context context, String databaseName, String table, String[] columns, String[] types) {
        super(context, databaseName + ".db", null, DATABASE_VERSION);
        this.table = table;
        this.columns = columns;
        this.types = types;
        createTable(getReadableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createTable(database);
    }

    private void createTable(SQLiteDatabase database) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + " (" + columns[0] + " " + types[0]);
        for (int i = 1; i < columns.length; i++) {
            builder.append(", ");
            builder.append(columns[i]);
            builder.append(" ");
            builder.append(types[i]);
        }
        builder.append(", CONSTRAINT ");
        builder.append(columns[0]);
        builder.append("_unique UNIQUE (");
        builder.append(columns[0]);
        builder.append("));");
        database.execSQL(builder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          int oldVersion,
                          int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(database);
    }

    /**
     * Inserts the given values into the database in the corresponding
     * columns
     * @param contentValues Values to be inserted into database
     * @throws IllegalArgumentException if contentValues == null
     *             || contentValues.size() != columns.length
     */
    public void insertRecord(ContentValues contentValues) {
        if (contentValues == null || contentValues.size() != columns.length) {
            throw new IllegalArgumentException();
        }
        SQLiteDatabase database = getReadableDatabase();
        database.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Helper method to compile columns and values to be inserted
    private ContentValues constructContent(String[] columns, String[] values) {
        ContentValues content = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            content.put(columns[i], values[i]);
        }
        return content;
    }

    /**
     * Update a record in the database based on the first column's value
     *
     * @param columns Columns in database
     * @param values  Values to be updated in database for entry
     * @throws IllegalArgumentException columns.length != values.length
     *         || columns != database.columns
     */
    public void updateRecord(String[] columns, String[] values) {
        if (columns.length != values.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null && !this.columns[i].equals(columns[i])) {
                throw new IllegalArgumentException();
            }
        }
        ContentValues content = constructContent(columns, values);
        SQLiteDatabase db = getWritableDatabase();
        db.update(table, content, columns[0] + " = ?", new String[]{values[0]});
    }

    /**
     * Return a cursor that can iterate over the entire table
     *
     * @requires table exists
     * @param table Name of table to check for
     * @return Cursor pointing to beginning of table
     */
    public Cursor getTable(String table) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + table, null);
    }

    /**
     * Checks if the given table exists in the database
     *
     * @param table Table to check for
     * @return True if table exists, false otherwise
     */
    public boolean tableExists(String table) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("SQLITE_MASTER", null, "type=\'table\' AND name=\'" + table + "\'", null, null, null, null);
        return cursor.getCount() > 0;
    }

}
