package com.receipt.forever.db;

import android.provider.BaseColumns;

public class ReceiptContract {

    private ReceiptContract() { }

    public static class ReceiptEntry implements BaseColumns {
        public static final String TABLE_NAME = "Receipts";
        public static final String USER_ID = "user_id";
        public static final String RECEIPT_TEXT = "receipt_text";
        public static final String NAME = "name";
        public static final String IMAGE_PATH = "image_path";
        public static final String COMPANY_NAME = "company";
        public static final String PAYMENT_METHOD = "payment_method";
        public static final String PURCHASE_DATE = "purchase_date";
        public static final String PRICE = "price";
        public static final String CURRENCY = "currency";
        public static final String CATEGORIES = "categories";
        public static final String NOTES = "notes";
        public static final String WARRANTY_EXP_DATE = "warranty_exp_date";
        public static final String LAST_RETURN_DATE = "last_return_date";
        public static final String PHONE_NUM = "phone_num";
        public static final String WEB_LINKS = "web_links";
    }
}
