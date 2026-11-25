
package com.example.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shopping.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "purchases";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "created_at";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_PRICE + " REAL NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL" +
                ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление покупки
    public boolean addPurchase(String name, double price,
                               String category, String date) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);

        long result = db.insert(TABLE_NAME, null, values);

        return result != -1;
    }

    // Получение всех покупок
    public Cursor getAllPurchases(String orderBy) {
        SQLiteDatabase db = getReadableDatabase();

        String sortColumn = getSafeSortColumn(orderBy);

        return db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                sortColumn + " ASC"
        );
    }

    // Получение покупок с сортировкой
    public Cursor getSortedPurchases(String orderBy, boolean ascending) {
        SQLiteDatabase db = getReadableDatabase();

        String sortColumn = getSafeSortColumn(orderBy);
        String direction = ascending ? "ASC" : "DESC";

        return db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                sortColumn + " " + direction
        );
    }

    // Безопасное определение столбца для сортировки
    private String getSafeSortColumn(String orderBy) {

        if (orderBy == null) {
            return COLUMN_ID;
        }

        switch (orderBy) {
            case COLUMN_NAME:
                return COLUMN_NAME;

            case COLUMN_PRICE:
                return COLUMN_PRICE;

            case COLUMN_CATEGORY:
                return COLUMN_CATEGORY;

            case COLUMN_DATE:
                return COLUMN_DATE;

            default:
                return COLUMN_ID;
        }
    }

    // Фильтрация покупок по цене
    public Cursor getPurchasesByPrice(double minPrice, double maxPrice) {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                COLUMN_PRICE + " BETWEEN ? AND ?",
                new String[]{
                        String.valueOf(minPrice),
                        String.valueOf(maxPrice)
                },
                null,
                null,
                COLUMN_PRICE + " ASC"
        );
    }

    // Получение количества покупок
    public int getPurchaseCount() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NAME,
                null
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }
    // Удаление покупки по ID
    public boolean deletePurchase(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int deletedRows = db.delete(
                TABLE_NAME,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return deletedRows > 0;
    }
    // Получение общей стоимости покупок
    public double getTotalPrice() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_PRICE + ") FROM " + TABLE_NAME,
                null
        );

        double total = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }
}
