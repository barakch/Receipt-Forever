package com.receipt.forever.utils;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.receipt.forever.model.Receipt;
import org.jsoup.Jsoup;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyzePictureUtility {

    public static String TAG = "AnalyzePictureUtility";
    private Activity context;
    private AnalyzePictureCallback callback;
    private Receipt currReceipt;

    public AnalyzePictureUtility(Activity context) {
        this.context = context;
    }

    public interface AnalyzePictureCallback {
        void onAnalyzeDone(boolean success, Receipt receipt);
    }

    public void startAnalyzingAsync(String imagePath, AnalyzePictureCallback callback) {
        this.callback = callback;
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(context, Uri.fromFile(new File(imagePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        currReceipt = new Receipt(imagePath);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                analyzeResult(firebaseVisionText, currReceipt);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        e.printStackTrace();
                                        Toast.makeText(context, "Image process failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
    }


    // TODO Analyze the text - will probably work only in *English*
    // How is it working? https://firebase.google.com/docs/ml-kit/recognize-text
    // More info about android implementation: https://firebase.google.com/docs/ml-kit/android/recognize-text
    // Use logs to print data into the log - the objective is to fill up the receipt object with data
    private void analyzeResult(final FirebaseVisionText firebaseVisionText, final Receipt receipt) {
        new AsyncTask<String, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                String resultText = firebaseVisionText.getText();
                Log.d(TAG, "All text: " + resultText);
                System.out.println("All text: " + resultText);

                receipt.setReceiptName("");
                receipt.setAllData(resultText.toLowerCase());

                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                    String blockText = block.getText();
                    Float blockConfidence = block.getConfidence();
                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (FirebaseVisionText.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Float lineConfidence = line.getConfidence();
                        List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        searchInLineResult(receipt, line);
                        for (FirebaseVisionText.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Float elementConfidence = element.getConfidence();
                            List<RecognizedLanguage> eƒlementLanguages = element.getRecognizedLanguages();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            searchInElementResult(receipt, element);
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                Log.d(TAG, "Receipt: " + receipt.toString());
                callback.onAnalyzeDone(true, receipt);
            }
        }.execute();
    }

    private void searchInLineResult(Receipt receipt, FirebaseVisionText.Line line) {
        searchCompanyNameInLineResult(receipt, line);
        searchDateInLineResult(receipt, line);
        searchPhoneNumberInLineResult(receipt, line);
        searchCurrencyInResult(receipt, line);
        searchPaymentMethodInResult(receipt, line);
        searchPriceInLineResult(receipt, line);
    }

    private void searchCompanyNameInLineResult(Receipt receipt, FirebaseVisionText.Line line) {
        String result = line.getText();

        if (receipt.getCompanyName() == null && !result.isEmpty()) {
            receipt.setCompanyName(result);
        }
    }

    private void searchPhoneNumberInLineResult(Receipt receipt, FirebaseVisionText.Line line) {
        String[] wordOfPrice = new String[]{"TEL", "Phone", "Fax"};
        String result = line.getText().toLowerCase();

        for (String str : wordOfPrice) {
            int index = result.indexOf(str.toLowerCase());
            if (index != -1) {
                int subIndex = index + str.length();
                String strSubResult = result.substring(subIndex);
                if (strSubResult != null && !strSubResult.isEmpty()) {
                    String numberString = strSubResult.replaceAll("[^0-9]", "");
                    if (numberString.length() > 4) {
                        receipt.setPhoneNumber(numberString);
                    }
                }
            }
        }
    }

    private void searchPhoneNumberInElementResult(Receipt receipt, FirebaseVisionText.Element element) {
        String result = element.getText().toLowerCase();
        String numberString = result.replaceAll("[^0-9]", "");

        if (receipt.getPhoneNumber() == null) {
            if (numberString.length() > 8) {
                receipt.setPhoneNumber(numberString);
            }
        }
    }

    private void searchDateInLineResult(Receipt receipt, FirebaseVisionText.Line line) {
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);
        String day = "", months = "", year = "";
        String result = line.getText();
        ArrayList<String> arrayOfMonths = new ArrayList<String>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
                "January", "February", "March", "April", "June", "July", "August", "September", "October", "November", "December",
                "01", "1", "02", "2", "03", "3", "04", "4", "05", "5", "06", "6", "07", "7", "08", "8", "09", "9", "10", "11", "12"));

        SimpleDateFormat[] formatList = new SimpleDateFormat[]{new SimpleDateFormat("M/dd/yyyy"), new SimpleDateFormat("M/d/yyyy")};

        Date parsedDate = null;
        String[] splitResult = result.split("[ :./-]");
        String strResult = "";

        if (splitResult.length >= 3) {
            for (String strSplit : splitResult) {
                if (!strSplit.isEmpty()) {
                    try {

                        if (arrayOfMonths.contains(strSplit) && months.isEmpty()) {
                            months = strSplit;
                        }

                        int res = Integer.parseInt(strSplit);
                        if (res >= 1 && res <= 31 && day.isEmpty()) {
                            day = strSplit;
                        }
                        if (strSplit.length() == 4 && res >= 1000 && res <= 3000 && year.isEmpty()) {
                            year = strSplit;
                        }

                    } catch (Exception ex) {
                    }
                }

                if (!year.isEmpty() && !months.isEmpty() && !day.isEmpty()) {
                    months = Utils.getMonths(months);
                    strResult = months + "/" + day + "/" + year;

                    if (strResult != null && !strResult.isEmpty()) {
                        for (int i = 0; i < formatList.length; i++) {
                            if (parsedDate == null) {
                                try {
                                    parsedDate = formatList[i].parse(strResult);
                                    break;
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }

                    if (parsedDate != null) {
                        break;
                    }
                }
            }
        }

        if (parsedDate != null) {
            receipt.setPurchaseDate(sdf.format(parsedDate));
            setLastReturnDateFromResult(receipt, parsedDate);
        }
    }

    private void searchDateInElementResult(Receipt receipt, FirebaseVisionText.Element element) {
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);
        String day = "", months = "", year = "";
        String result = element.getText();
        ArrayList<String> arrayOfMonths = new ArrayList<String>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
                "January", "February", "March", "April", "June", "July", "August", "September", "October", "November", "December",
                "01", "1", "02", "2", "03", "3", "04", "4", "05", "5", "06", "6", "07", "7", "08", "8", "09", "9", "10", "11", "12"));

        SimpleDateFormat[] formatList = new SimpleDateFormat[]{new SimpleDateFormat("M/dd/yyyy"), new SimpleDateFormat("M/d/yyyy")};

        Date parsedDate = null;
        String[] splitResult = result.split("[/]");
        String strResult = "";

        if (!result.isEmpty() && splitResult.length == 3) {
            if (splitResult[0].length() <= 2 && splitResult[1].length() <= 2 && splitResult[2].length() <= 4 && splitResult[2].length() >= 2 &&
                    Utils.isNumber(splitResult[0]) && Utils.isNumber(splitResult[1]) && Utils.isNumber(splitResult[2])) {
                for (String strSplit : splitResult) {
                    if (!strSplit.isEmpty()) {
                        try {

                            if (arrayOfMonths.contains(strSplit) && months.isEmpty()) {
                                months = strSplit;
                            }

                            int res = Integer.parseInt(strSplit);
                            if (res >= 1 && res <= 31 && day.isEmpty()) {
                                day = strSplit;
                            }
                            if (strSplit.length() == 4 && res >= 1000 && res <= 3000 && year.isEmpty()) {
                                year = strSplit;
                            }

                        } catch (Exception ex) {
                        }
                    }

                    if (year.isEmpty()) {
                        year = splitResult[2];
                    }

                    if (!year.isEmpty() && !months.isEmpty() && !day.isEmpty()) {
                        months = Utils.getMonths(months);
                        strResult = months + "/" + day + "/" + year;

                        if (strResult != null && !strResult.isEmpty()) {
                            for (int i = 0; i < formatList.length; i++) {
                                if (parsedDate == null) {
                                    try {
                                        parsedDate = formatList[i].parse(strResult);
                                        break;
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }

                        if (parsedDate != null) {
                            break;
                        }
                    }
                }
            }
        }

        if (parsedDate != null) {
            receipt.setPurchaseDate(sdf.format(parsedDate));
            setLastReturnDateFromResult(receipt, parsedDate);
        }
    }

    private void  setLastReturnDateFromResult(Receipt receipt, Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);

        //calculate the lastReturnDateAfter14Day
        long lastReturnDate = date.getTime() + (14 * 24 * 60 * 60 * 1000);
        Date lastReturnDateAfter14Day = new Date(lastReturnDate);
        receipt.setLastReturnDate(sdf.format(lastReturnDateAfter14Day));

        long warrantyExp = date.getTime() + (365L * 24L * 60L * 60L * 1000L);
        Date warrantyExpDate = new Date(warrantyExp);
        receipt.setWarrantyExpirationDate(sdf.format(warrantyExpDate));

        receipt.setReceiptName("Receipt " + sdf.format(date));
    }

    private void searchPriceInLineResult(Receipt receipt, FirebaseVisionText.Line line) {
        String[] wordOfPrice = new String[]{"TOTAL", "AMOUNT", "PRICE"};
        String result = line.getText().toLowerCase();

        for (String str : wordOfPrice) {
            int index = result.indexOf(str.toLowerCase());
            if (index != -1) {
                int subIndex = index + str.length();
                String strResult = result.substring(subIndex);
                strResult = strResult.replaceAll("[^\\d.]", "");
                if (strResult != null && !strResult.isEmpty()) {
                    float price = 0;
                    try {
                        price = Float.parseFloat(strResult);
                    }catch (NumberFormatException ex){
                    }
                    if (price > receipt.getPrice()) {
                        receipt.setPrice(price);
                    }
                }
            }
        }
    }

    private void searchPriceInElementResult(Receipt receipt, FirebaseVisionText.Element element) {
        String result = element.getText().toLowerCase();
        String[] splitResult = result.split("[.]");

        if (!result.isEmpty() && splitResult.length == 2) {
            if (splitResult[1].length() == 2 && Utils.isNumber(splitResult[0]) && Utils.isNumber(splitResult[1])) {
                float price = 0;
                try {
                    price = Float.parseFloat(result);
                } catch (NumberFormatException ex) {
                }
                if (price > receipt.getPrice()) {
                    receipt.setPrice(price);
                }
            }
        }
    }

    private void searchCurrencyInResult(Receipt receipt, FirebaseVisionText.Line line) {
        Map<String, String> AllCurrency =
                new HashMap<String, String>() {
                    {
                        put("$", "USD");
                        put("€", "EUR");
                        put("£", "GBP");
                        put("JPY", "AUD");
                        put("&", "GBP");
                    }
                };
        String result = line.getText();
        Currency currencyCode;
        int index = -1;

        for (Map.Entry<String, String> entry : AllCurrency.entrySet()) {
            if (result.contains(entry.getKey())) {
                currencyCode = Currency.getInstance(entry.getValue());
                receipt.setCurrency(currencyCode);
                index = result.indexOf(entry.getKey());
            }
            else if (result.contains(entry.getValue())) {
                currencyCode = Currency.getInstance(entry.getValue());
                receipt.setCurrency(currencyCode);
                index = result.indexOf(entry.getValue());
            }

            //setPrice
            if (index != -1) {
                int subIndex = index + entry.getKey().length(); //without the Currency type
                String strResult = result.substring(subIndex);
                strResult = strResult.replaceAll("[^\\d.]", "");
                if (strResult != null && !strResult.isEmpty()) {
                    float price = 0;
                    try {
                        price = Float.parseFloat(strResult);
                    } catch (NumberFormatException e) {
                        price = 0;
                    }
                    if (price > receipt.getPrice()) {
                        receipt.setPrice(price);
                    }
                }
            }
        }
    }

    private void searchPaymentMethodInResult(Receipt receipt, FirebaseVisionText.Line line) {
        String[] wordOfPaymentMethodIn = new String[]{"CASH", "VISA", "CREDIT", "CARD", "CREDIT CARD"};

        String result = line.getText().toLowerCase();

        for (String str : wordOfPaymentMethodIn) {
            if (result.contains(str.toLowerCase()))
                if(str == "VISA" || str == "CREDIT" || str == "CARD" || str == "CREDIT CARD")
                    receipt.setPaymentMethod(Receipt.PaymentMethod.CREDIT_CARD);
                else if(str == "CASH")
                    receipt.setPaymentMethod(Receipt.PaymentMethod.CASH);
        }
    }

    private void searchInElementResult(Receipt receipt, FirebaseVisionText.Element element){
        searchPriceInElementResult(receipt, element);
        searchDateInElementResult(receipt, element);
        searchPhoneNumberInElementResult(receipt, element);
        searchEbayItem(receipt, element);
    }

    private void searchEbayItem(final Receipt receipt,final FirebaseVisionText.Element element) {
        try {
            String[] allWordNotInclude = new String[]{"TOTAL", "AMOUNT", "PRICE", "PAYMENT", "CASH", "VISA", "CREDIT", "CARD", "CREDIT CARD",
                    "CONDITIONS", "TERM", "SERVICE", "CLIENT", "DATE", "RECEIPT", "DESCRIPTION", "DAYS", "BILL"};

            if(receipt.getWebItems().size() >= 2) return;
            if(!element.getText().matches("[a-zA-Z]+")) return;
            if(element.getText().replaceAll("[^a-zA-Z]+", "").length() < 4) return;
            for (String str : allWordNotInclude) {
                {
                    if (element.getText().toLowerCase().contains(str.toLowerCase()))
                        return;
                }
            }

            String url = "https://www.ebay.com/sch/i.html?_nkw=" + element.getText();
            String body = Jsoup.connect(url).userAgent("Mozilla").get().body().text();
            String error = "No exact matches found";

            if (!body.contains(error)) {
                Log.wtf("tag", "web found!!" + url);
                receipt.addWebItem(url);
            } else {
                Log.wtf("tag", "web not found@@@@" + url);
            }

        } catch (Exception ex) {
            Log.wtf("tag", "ex web: " + ex.getMessage());
        }
    }
}
