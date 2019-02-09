package com.receipt.forever.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Receipt.java
 * The class holds all the receipt data
 */
@SuppressWarnings("serial")
public class Receipt implements Serializable {

    public static final String DATE_FORMAT = "MM/dd/yy";
    public static final String[] PAYMENT_METHODS = new String[]{"Credit Card", "Cash", "Select"};

    public enum  PaymentMethod {CREDIT_CARD, CASH}

    private int dbID;
    private String imagePath;
    private String receiptName;
    private String companyName;
    private PaymentMethod paymentMethod;
    private String purchaseDate;
    private float price;
    private Currency currency;
    private List<String> categories;
    private String notes;
    private String warrantyExpirationDate;
    private String LastReturnDate;
    private String phoneNumber;
    private List<String> webItems;
    private String allData;



    public Receipt(String imagePath) {
        this.dbID = -1;
        this.imagePath = imagePath;
        categories = new ArrayList<>();
        webItems = new ArrayList<>();
    }

    public Receipt(int dbID) {
        this.dbID = dbID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Receipt(int dbID, String imagePath, String receiptName, String companyName, PaymentMethod paymentMethod, String purchaseDate, float price, Currency currency, List<String> categories, String notes, String warrantyExpirationDate, String lastReturnDate) {
        this.dbID = dbID;
        this.imagePath = imagePath;
        this.receiptName = receiptName;
        this.companyName = companyName;
        this.paymentMethod = paymentMethod;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.currency = currency;
        this.categories = categories;
        this.notes = notes;
        this.warrantyExpirationDate = warrantyExpirationDate;
        LastReturnDate = lastReturnDate;

        if(categories == null)
            this.categories = new ArrayList<>();
    }

    public String getAllData() {
        return allData;
    }

    public void setAllData(String allData) {
        this.allData = allData;
    }

    public int getDbID() {
        return dbID;
    }

    public void setDbID(int dbID) {
        this.dbID = dbID;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public int getPaymentMethodValue() {
        if(paymentMethod == null)
            return -1;

        return paymentMethod.ordinal();
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentMethod(int paymentMethod) {
        switch (paymentMethod){
            case 0:
                this.paymentMethod = PaymentMethod.CREDIT_CARD;
                break;
            case 1:
                this.paymentMethod = PaymentMethod.CASH;
                break;

            default:
                this.paymentMethod = null;
        }
    }

    public Date getFormattedPurchaseDate() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            if(purchaseDate == null)
                return new Date();
            return format.parse(purchaseDate);
        }catch (ParseException e){
            e.printStackTrace();
            return new Date();
        }
    }


    public String getPurchaseDate() {
        return purchaseDate;
    }


    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCurrency(String currencyCode) {
        this.currency = Currency.getInstance(currencyCode);
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getCategoriesAsString() {
        String cats = "";
        if (categories != null){
            for(int i=0; i< categories.size(); i++) {
                if(i==0)
                    cats += categories.get(i);
                else
                    cats += " | " + categories.get(i);
            }
        }
        return cats;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWarrantyExpirationDate() {
        return warrantyExpirationDate;
    }

    public Date getFormattedWarrantyExpirationDate() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            if(warrantyExpirationDate == null)
                return new Date();
            return format.parse(warrantyExpirationDate);
        }catch (ParseException e){
            return new Date();
        }
    }

    public void setWarrantyExpirationDate(String warrantyExpirationDate) {
        this.warrantyExpirationDate = warrantyExpirationDate;
    }

    public String getLastReturnDate() {
        return LastReturnDate;
    }

    public Date getFormattedLastReturnDate() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            if(LastReturnDate == null)
                return new Date();
            return format.parse(LastReturnDate);
        }catch (ParseException e){
            return new Date();
        }
    }

    public void setLastReturnDate(String lastReturnDate) {
        LastReturnDate = lastReturnDate;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getWebItems() {
        return webItems;
    }

    public String getFormattedWebItems() {
        String items = "";
        for(String item : webItems)
            items += item +"\n";
        return items;
    }

    public void addWebItem(String webLink){
        webItems.add(webLink);
    }

    public void setWebItems(List<String> webItems) {
        this.webItems = webItems;
    }

    @Override
    public String toString() {
        return "ReceiptName='" + receiptName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", purchaseDate='" + purchaseDate + '\'' +
                ", price=" + price +
                ", currency=" + currency +
                ", categories=" + categories +
                ", notes='" + notes + '\'' +
                ", warrantyExpirationDate='" + warrantyExpirationDate + '\'' +
                ", LastReturnDate='" + LastReturnDate + '\'' +
                ", phoneNumber='" + phoneNumber
                ;
    }

    private String getFormattedLinks(){
        String res = "";
        for (String link: webItems)
            res += link+"\n";
        return res;
    }

    public static List<Category> getAllCategories(List<Receipt> receipts){
        Set<Category> categories = new HashSet<>();
        for (Receipt receipt : receipts)
        {
            for (String category : receipt.getCategories())
                if(!category.equals("") && !category.equals(" "))
                    categories.add(new Category(category, false));
        }

        return new ArrayList<>(categories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return dbID == receipt.dbID &&
                Objects.equals(receiptName, receipt.receiptName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(dbID, receiptName);
    }
}
