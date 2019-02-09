package com.receipt.forever.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DBHandler extends SQLiteOpenHelper {

    //information of database
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ReceiptDB.db";
    private static DBHandler instance;

    public DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHandler getHelper(Context context){
        if(instance == null)
            instance = new DBHandler(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {

    }


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ReceiptContract.ReceiptEntry.TABLE_NAME + " (" +
                    ReceiptContract.ReceiptEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ReceiptContract.ReceiptEntry.USER_ID + " TEXT ," +
                    ReceiptContract.ReceiptEntry.RECEIPT_TEXT + " TEXT ," +
                    ReceiptContract.ReceiptEntry.NAME + " TEXT," +
                    ReceiptContract.ReceiptEntry.IMAGE_PATH + " TEXT," +
                    ReceiptContract.ReceiptEntry.COMPANY_NAME + " TEXT," +
                    ReceiptContract.ReceiptEntry.PAYMENT_METHOD + " REAL," +
                    ReceiptContract.ReceiptEntry.PURCHASE_DATE + " TEXT," +
                    ReceiptContract.ReceiptEntry.PRICE + " REAL," +
                    ReceiptContract.ReceiptEntry.CURRENCY + " TEXT," +
                    ReceiptContract.ReceiptEntry.CATEGORIES + " TEXT," +
                    ReceiptContract.ReceiptEntry.NOTES + " TEXT," +
                    ReceiptContract.ReceiptEntry.WARRANTY_EXP_DATE + " TEXT," +
                    ReceiptContract.ReceiptEntry.LAST_RETURN_DATE + " TEXT," +
                    ReceiptContract.ReceiptEntry.PHONE_NUM + " TEXT," +
                    ReceiptContract.ReceiptEntry.WEB_LINKS + " TEXT )";


}
