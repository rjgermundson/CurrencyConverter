package com.example.riley.currencyconverter.LocalStorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
        table = table.toUpperCase();
        this.table = DatabaseUtils.sqlEscapeString(table);
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
        builder.append(");");
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
        database.insert(table, null, contentValues);
    }

    /**
     * Update a record in the database based on the given identifier column and value
     * with the given value for the corresponding column
     *
     * @requires identifier and value are formatted properly
     * @param identifierColumn Column which will be used to identify which rows to update
     * @param identifier Value for which rows will be checked for whether they match
     * @param column Column in which the values will be updated
     * @param value  Value to be updated in database for entry
     * @throws IllegalArgumentException identifierColumn >= columns.length
     *              || column >= columns.length
     */
    public void updateRecord(int identifierColumn, String identifier, int column, String value) {
        if (identifierColumn >= columns.length || column >= columns.length) {
            throw new IllegalArgumentException();
        }
        SQLiteDatabase db = getReadableDatabase();
        String columnName = columns[column];
        ContentValues content = new ContentValues();
        content.put(columnName, value);

        db.update(table, content, columns[identifierColumn] + " = ?", new String[]{identifier});
    }

    /**
     * Update a record in the database based on the given identifierColumn value
     *
     * @requires identifier and values are formatted properly
     * @param identifierColumn Column which will be used to identify which rows to update
     * @param identifier Value for which rows will be checked for whether they match
     * @param values Values to replace found columns values
     */
    public void updateRecord(int identifierColumn, String identifier, ContentValues values) {
        if (identifierColumn >= columns.length || values.size() >= columns.length) {
            throw new IllegalArgumentException();
        }
        SQLiteDatabase db = getReadableDatabase();
        db.update(table, values, columns[identifierColumn] + " = ?", new String[]{identifier});
    }

    /**
     * Deletes the record where the first column matches the given key
     *
     * @param key Defining column value for a given entry
     */
    public void removeRecord(String key) {
        SQLiteDatabase db = getReadableDatabase();
        key = DatabaseUtils.sqlEscapeString(key);
        db.delete(table, columns[0] + "=" + key, null);
    }

    /**
     * Return a cursor that can iterate over the entire table
     *
     * @requires table exists
     * @param table Name of table to check for
     * @return Cursor pointing to beginning of table
     */
    public Cursor getTable(String table) {
        table = table.toUpperCase();
        table = DatabaseUtils.sqlEscapeString(table);
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + table, null);
    }

    /**
     * Drops the given table
     * @param table Name of the table to drop
     */
    public void dropTable(String table) {
        table = table.toUpperCase();
        table = DatabaseUtils.sqlEscapeString(table);
        SQLiteDatabase database = getReadableDatabase();
        database.execSQL("DROP TABLE " + table);
    }

    /**
     * Checks if the given table exists in the database
     *
     * @param table Table to check for
     * @return True if table exists, false otherwise
     */
    public boolean tableExists(String table) {
        table = table.toUpperCase();
        System.err.println(table);
        table = DatabaseUtils.sqlEscapeString(table);
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("SQLITE_MASTER", null, "type=\'table\' AND name=" + table, null, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }
}
