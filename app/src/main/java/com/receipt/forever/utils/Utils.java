package com.receipt.forever.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import com.receipt.forever.BuildConfig;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Utils {

    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public static Date getFormattedDate(String date){
        Date formattedDate = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formattedDate = simpleDateFormat.parse(date);
        }catch (ParseException e){

        }
        return formattedDate;
    }


    /**
     * Log On Debug flag
     * change in ApplicationProperties
     *
     * @param tag
     * @param message
     */
    public static void Log(String tag, String message) {
        if (ApplicationProperties.DEBUG) {
            if (tag == null)
                tag = "VEEMS";

            Log.d(tag, message + "");
        }
    }

    private static final String LIST_SEPARATOR = "__,__";

    public static String convertListToString(List<String> stringList) {
        StringBuilder stringBuilder = new StringBuilder();
        if(stringList == null)
            return "";

        for (String str : stringList) {
            stringBuilder.append(str).append(LIST_SEPARATOR);
        }

        // Remove last separator
        if((stringBuilder.length() - LIST_SEPARATOR.length()) >= 0)
            stringBuilder.setLength(stringBuilder.length() - LIST_SEPARATOR.length());
        return stringBuilder.toString();
    }

    public static List<String> convertStringToList(String str) {
        return new ArrayList<>(Arrays.asList(str.split(LIST_SEPARATOR)));
    }

    public static Map<String, Integer> AllShortMonths =
            new HashMap<String, Integer>(){{
                put("Jan",1);
                put("Feb",2);
                put("Mar",3);
                put("Apr",4);
                put("May",5);
                put("Jun",6);
                put("Jul",7);
                put("Aug",8);
                put("Sep",9);
                put("Oct",10);
                put("Nov",11);
                put("Dec",12);}};

    public static Map<String, Integer> AllFullMonths =
            new HashMap<String, Integer>(){{
                put("January",1);
                put("February",2);
                put("March",3);
                put("April",4);
                put("May",5);
                put("June",6);
                put("July",7);
                put("August",8);
                put("September",9);
                put("October",10);
                put("November",11);
                put("December",12);
            }};

    public static String getMonths(String monthsToCheck){

        if(!Utils.stringContainsNumber(monthsToCheck)) {
            if(Utils.AllShortMonths.containsKey(monthsToCheck))
                return Utils.AllShortMonths.get(monthsToCheck).toString();
            if(Utils.AllFullMonths.containsKey(monthsToCheck))
                return Utils.AllFullMonths.get(monthsToCheck).toString();
        }
        return monthsToCheck;
    }

    public static boolean stringContainsNumber(String s)
    {
        return Pattern.compile( "[0-9]" ).matcher( s ).find();
    }

    public static void shareImageOnPlatform(Activity activity, String message, String filePath) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
            Uri photoURI = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            activity.startActivity(Intent.createChooser(sendIntent, "Choose your sharing app"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            activity.startActivity(sendIntent);

        }
    }

    public static boolean isNumber(String str)
    {
        return str.matches("-?\\d+(.\\d+)?");
    }


    public static void addACalendarReminder(Activity activity, Date date, String title, String description){
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.getTime() + 8*60*60*1000)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.getTime() + 20*60*60*1000)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        activity.startActivity(intent);
    }

}

