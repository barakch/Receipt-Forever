package com.receipt.forever.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.receipt.forever.model.Receipt;
import com.receipt.forever.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class DBAction {

    private Context mContext;
    private SQLiteDatabase mDB;
    private DBHandler mDBHandler;
    private String uid;

    public DBAction(Context mContext, String uid) {
        this.mContext = mContext;
        this.uid = uid;
        mDBHandler = mDBHandler.getHelper(mContext);
    }

    public void addReceipt(final Receipt receipt, final DBCallback callback){
        if(receipt == null){
            callback.onInsertDone(null, false);
            return;
        }

        new AsyncTask<String, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {

                mDB = mDBHandler.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(ReceiptContract.ReceiptEntry.NAME, receipt.getReceiptName());
                cv.put(ReceiptContract.ReceiptEntry.USER_ID, uid);
                cv.put(ReceiptContract.ReceiptEntry.RECEIPT_TEXT, receipt.getAllData());
                cv.put(ReceiptContract.ReceiptEntry.IMAGE_PATH, receipt.getImagePath());
                cv.put(ReceiptContract.ReceiptEntry.COMPANY_NAME, receipt.getCompanyName());
                cv.put(ReceiptContract.ReceiptEntry.PAYMENT_METHOD, receipt.getPaymentMethodValue());
                cv.put(ReceiptContract.ReceiptEntry.PURCHASE_DATE, receipt.getPurchaseDate());
                cv.put(ReceiptContract.ReceiptEntry.PRICE, receipt.getPrice());
                cv.put(ReceiptContract.ReceiptEntry.CURRENCY, receipt.getCurrency() == null? "" : receipt.getCurrency().toString());
                cv.put(ReceiptContract.ReceiptEntry.CATEGORIES, Utils.convertListToString(receipt.getCategories()));
                cv.put(ReceiptContract.ReceiptEntry.NOTES, receipt.getNotes());
                cv.put(ReceiptContract.ReceiptEntry.WARRANTY_EXP_DATE, receipt.getWarrantyExpirationDate());
                cv.put(ReceiptContract.ReceiptEntry.LAST_RETURN_DATE, receipt.getLastReturnDate());
                cv.put(ReceiptContract.ReceiptEntry.PHONE_NUM, receipt.getPhoneNumber());
                cv.put(ReceiptContract.ReceiptEntry.WEB_LINKS, Utils.convertListToString(receipt.getWebItems()));

                long id = mDB.insert(ReceiptContract.ReceiptEntry.TABLE_NAME, null, cv);
                receipt.setDbID((int) id);
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                callback.onInsertDone(receipt, true);
            }

        }.execute();
    }


    public void deleteReceipt(final Receipt receipt, final DBCallback callback){
        if(receipt == null){
            callback.onDeleteDone(null, false);
            return;
        }

        new AsyncTask<String, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {

                mDB = mDBHandler.getWritableDatabase();
                mDB.delete(ReceiptContract.ReceiptEntry.TABLE_NAME, ReceiptContract.ReceiptEntry._ID + "=" + receipt.getDbID(), null);
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                callback.onDeleteDone(receipt, true);
            }

        }.execute();
    }

    public void updateReceipt(final Receipt receipt, final DBCallback callback){
        if(receipt == null){
            callback.onUpdateDone(null, false);
            return;
        }

        new AsyncTask<String, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {

                mDB = mDBHandler.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(ReceiptContract.ReceiptEntry.NAME, receipt.getReceiptName());
                cv.put(ReceiptContract.ReceiptEntry.IMAGE_PATH, receipt.getImagePath());
                cv.put(ReceiptContract.ReceiptEntry.COMPANY_NAME, receipt.getCompanyName());
                cv.put(ReceiptContract.ReceiptEntry.PAYMENT_METHOD, receipt.getPaymentMethodValue());
                cv.put(ReceiptContract.ReceiptEntry.PURCHASE_DATE, receipt.getPurchaseDate());
                cv.put(ReceiptContract.ReceiptEntry.PRICE, receipt.getPrice());
                cv.put(ReceiptContract.ReceiptEntry.CURRENCY, receipt.getCurrency() == null? "" : receipt.getCurrency().toString());
                cv.put(ReceiptContract.ReceiptEntry.CATEGORIES, Utils.convertListToString(receipt.getCategories()));
                cv.put(ReceiptContract.ReceiptEntry.NOTES, receipt.getNotes());
                cv.put(ReceiptContract.ReceiptEntry.WARRANTY_EXP_DATE, receipt.getWarrantyExpirationDate());
                cv.put(ReceiptContract.ReceiptEntry.LAST_RETURN_DATE, receipt.getLastReturnDate());
                cv.put(ReceiptContract.ReceiptEntry.PHONE_NUM, receipt.getPhoneNumber());
                cv.put(ReceiptContract.ReceiptEntry.WEB_LINKS, Utils.convertListToString(receipt.getWebItems()));

                mDB.update(ReceiptContract.ReceiptEntry.TABLE_NAME, cv, ReceiptContract.ReceiptEntry._ID + "=" + receipt.getDbID(), null);
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                callback.onUpdateDone(receipt, true);
            }

        }.execute();
    }


    public void getAllReceipts(final DBCallback callback) {
        final List<Receipt> receipts = new ArrayList<>();

        new AsyncTask<String, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                mDB = mDBHandler.getWritableDatabase();
                Cursor cursor = mDB.rawQuery("SELECT * FROM " + ReceiptContract.ReceiptEntry.TABLE_NAME + " WHERE " + ReceiptContract.ReceiptEntry.USER_ID + " = '" + uid + "'", null);

                while (cursor.moveToNext()) {
                    long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(ReceiptContract.ReceiptEntry._ID));
                    Receipt receipt = new Receipt((int) itemId);
                    receipt.setReceiptName(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.NAME)));
                    receipt.setAllData(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.RECEIPT_TEXT)));
                    receipt.setImagePath(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.IMAGE_PATH)));
                    receipt.setCompanyName(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COMPANY_NAME)));
                    receipt.setPaymentMethod(cursor.getInt(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.PAYMENT_METHOD)));
                    receipt.setPurchaseDate(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.PURCHASE_DATE)));
                    receipt.setPrice(cursor.getFloat(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.PRICE)));
                    String currency = cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.CURRENCY));
                    if(!currency.equals(""))
                        receipt.setCurrency(currency);
                    receipt.setCategories(Utils.convertStringToList(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.CATEGORIES))));
                    receipt.setNotes(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.NOTES)));
                    receipt.setWarrantyExpirationDate(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.WARRANTY_EXP_DATE)));
                    receipt.setLastReturnDate(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.LAST_RETURN_DATE)));
                    receipt.setPhoneNumber(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.PHONE_NUM)));
                    receipt.setWebItems(Utils.convertStringToList(cursor.getString(cursor.getColumnIndex(ReceiptContract.ReceiptEntry.WEB_LINKS))));
                    receipts.add(receipt);

                }
                cursor.close();
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                callback.onReceiptsLoaded(receipts, true);
            }

        }.execute();
    }

}
