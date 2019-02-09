package com.receipt.forever.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.receipt.forever.R;
import com.receipt.forever.db.DBAction;
import com.receipt.forever.db.DBCallback;
import com.receipt.forever.model.Receipt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoreFeaturesFragment extends Fragment implements DBCallback {

    public static String TAG = "MoreFeaturesFragment";
    private List<Receipt> receiptList;

    TextView tvCount, tvCashAmount, tvCreditAmount, tvInBetween;
    EditText edFrom, edTo;
    Date from, upTo;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        View layout =  inflater.inflate(R.layout.more_features_layout, container, false);

        tvCount = layout.findViewById(R.id.tv_receipts_count);
        tvCashAmount = layout.findViewById(R.id.tv_amount_cash);
        tvCreditAmount = layout.findViewById(R.id.tv_amount_credit);
        tvInBetween = layout.findViewById(R.id.tv_total_between);

        edFrom = layout.findViewById(R.id.ed_date_from);
        edTo = layout.findViewById(R.id.ed_date_to);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new DBAction(getActivity(), mAuth.getUid()).getAllReceipts(this);

        final Calendar myCalendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);
        from = upTo = myCalendar.getTime();

        final DatePickerDialog.OnDateSetListener fromDateDialog = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edFrom.setText(sdf.format(myCalendar.getTime()));
                from = myCalendar.getTime();
                updateData(receiptList);
            }

        };

        final DatePickerDialog.OnDateSetListener toDateDialog = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edTo.setText(sdf.format(myCalendar.getTime()));
                upTo = myCalendar.getTime();
                updateData(receiptList);
            }

        };

        edFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), fromDateDialog, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), toDateDialog, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateData(List<Receipt> receipts){
        tvCount.setText(receipts.size()+"");

        float amountInCash = 0, amountInCredit = 0, amountBetween = 0;
        Currency currency  = Currency.getInstance("USD");
        for (Receipt receipt : receipts){
            if(receipt.getPaymentMethod() == Receipt.PaymentMethod.CASH)
                amountInCash += receipt.getPrice();
            else if(receipt.getPaymentMethod() == Receipt.PaymentMethod.CREDIT_CARD)
                amountInCredit += receipt.getPrice();

            currency = receipt.getCurrency();


            if(receipt.getFormattedPurchaseDate().after(from) && receipt.getFormattedPurchaseDate().before(upTo))
                amountBetween += receipt.getPrice();
        }

        tvCashAmount.setText(String.format("%.2f",  amountInCash)  + " " + (currency != null ? currency.toString() : ""));
        tvCreditAmount.setText(String.format("%.2f",  amountInCredit)  + " " + (currency != null ? currency.toString() : ""));
        tvInBetween.setText(String.format("%.2f",  amountBetween)  + " " + (currency != null ? currency.toString() : ""));


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onInsertDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onUpdateDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onDeleteDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onReceiptsLoaded(List<Receipt> receipts, boolean success) {
        receiptList = receipts;
        updateData(receipts);
    }
}