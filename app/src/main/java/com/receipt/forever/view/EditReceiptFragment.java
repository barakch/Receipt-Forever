package com.receipt.forever.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.firebase.auth.FirebaseAuth;
import com.receipt.forever.R;
import com.receipt.forever.activities.EditReceiptActivity;
import com.receipt.forever.adapters.HintAdapter;
import com.receipt.forever.db.DBAction;
import com.receipt.forever.db.DBCallback;
import com.receipt.forever.model.Receipt;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EditReceiptFragment extends Fragment implements View.OnClickListener, DBCallback {

    public static String TAG = "EditReceiptFragment";
    EditText edReceiptName, edCompanyName, edPurchaseDate, edPrice, edWarrantyExpirationDate, edLastReturnDate, edPhoneNum, edNotes, edCategory;
    AwesomeValidation mAwesomeValidation;
    Spinner currenciesSpinner, paymentsSpinner;
    Button addCategory;
    private FirebaseAuth mAuth;


    public static EditReceiptFragment newInstance() {
        return new EditReceiptFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        View layout = inflater.inflate(R.layout.edit_receipt_fragment, container, false);

        mAwesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        layout.findViewById(R.id.btn_done).setOnClickListener(this);
        layout.findViewById(R.id.btn_clr).setOnClickListener(this);


        edReceiptName = layout.findViewById(R.id.ed_receipt_name);
        edCompanyName = layout.findViewById(R.id.ed_company_name);
        edPurchaseDate = layout.findViewById(R.id.ed_purchase_date);
        edPrice = layout.findViewById(R.id.ed_price);
        edWarrantyExpirationDate = layout.findViewById(R.id.ed_warranty_expiration_date);
        edLastReturnDate = layout.findViewById(R.id.ed_last_return_date);
        edPhoneNum = layout.findViewById(R.id.ed_phone_number);
        edNotes = layout.findViewById(R.id.ed_notes);
        currenciesSpinner = layout.findViewById(R.id.spinner_currency);
        paymentsSpinner = layout.findViewById(R.id.spinner_payment_method);
        addCategory = layout.findViewById(R.id.btn_add_category);
        edCategory = layout.findViewById(R.id.ed_categories);
        edReceiptName.setText(getReceipt().getReceiptName() == null ? "" : getReceipt().getReceiptName());
        edCompanyName.setText(getReceipt().getCompanyName() == null ? "" : getReceipt().getCompanyName());
        edPrice.setText(getReceipt().getPrice() == 0 ? "0" : getReceipt().getPrice() + "");
        edPhoneNum.setText(getReceipt().getPhoneNumber() == null ? "" : getReceipt().getPhoneNumber());
        edCategory.setText(getReceipt().getCategories().toString() == null ? "" : getReceipt().getCategoriesAsString());
        edNotes.setText(getReceipt().getNotes() == null ? "" : getReceipt().getNotes());


        final Calendar myCalendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);
        edPurchaseDate.setText(sdf.format(getReceipt().getFormattedPurchaseDate()));
        edWarrantyExpirationDate.setText(sdf.format(getReceipt().getFormattedWarrantyExpirationDate()));
        edLastReturnDate.setText(sdf.format(getReceipt().getFormattedLastReturnDate()));


        final DatePickerDialog.OnDateSetListener purchaseDateDialog = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edPurchaseDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        final DatePickerDialog.OnDateSetListener warrantyExpirationDateDialog = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edWarrantyExpirationDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        final DatePickerDialog.OnDateSetListener lastReturnDateDialog = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edLastReturnDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        edPurchaseDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), purchaseDateDialog, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edWarrantyExpirationDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), warrantyExpirationDateDialog, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edLastReturnDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), lastReturnDateDialog, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        return layout;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAwesomeValidation.addValidation(edReceiptName, "^(.{2,25})", getActivity().getResources().getString(R.string.err_name));
        mAwesomeValidation.addValidation(edPrice,"^\\s*(?=.*[1-9])\\d*(?:\\.\\d{1,2})?\\s*$", getActivity().getResources().getString(R.string.err_price));

        HintAdapter currenciesAdapter = new HintAdapter(getActivity(), android.R.layout.simple_spinner_item, currencies);
        currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currenciesSpinner.setAdapter(currenciesAdapter);
        currenciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                if (position < currencies.length - 1)
                    getReceipt().setCurrency(currencies[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (getReceipt().getCurrency() != null)
            currenciesSpinner.setSelection(currenciesAdapter.getPosition(getReceipt().getCurrency().toString()));
        else
            currenciesSpinner.setSelection(currencies.length - 1);

        HintAdapter paymentAdapter = new HintAdapter(getActivity(), android.R.layout.simple_spinner_item, Receipt.PAYMENT_METHODS);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentsSpinner.setAdapter(paymentAdapter);
        paymentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                if (position < 2)
                    getReceipt().setPaymentMethod(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (getReceipt().getPaymentMethodValue() == -1)
            paymentsSpinner.setSelection(2);
        else
            paymentsSpinner.setSelection(getReceipt().getPaymentMethodValue());


        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Category name");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        if (input.getText().toString().length() > 0)
                            getReceipt().getCategories().add(input.getText().toString().toLowerCase());
                        edCategory.setText(getReceipt().getCategoriesAsString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        dialog.cancel();

                    }
                });

                builder.show();
            }
        });
    }


    public void collectData() {
        if (!mAwesomeValidation.validate())
            return;

        getReceipt().setReceiptName(edReceiptName.getText().toString());
        getReceipt().setCompanyName(edCompanyName.getText().toString());
        getReceipt().setPurchaseDate(edPurchaseDate.getText().toString());
        getReceipt().setPrice(Float.parseFloat(edPrice.getText().toString() == "" ? "0" : edPrice.getText().toString()));
        getReceipt().setWarrantyExpirationDate(edWarrantyExpirationDate.getText().toString());
        getReceipt().setLastReturnDate(edLastReturnDate.getText().toString());
        getReceipt().setPhoneNumber(edPhoneNum.getText().toString());
        getReceipt().setNotes(edNotes.getText().toString());
        getReceipt().setCategories(parseCategories(edCategory.getText().toString()));
        saveReceipt(getReceipt());

    }

    public void clearData() {
        edReceiptName.setText("");
        edCompanyName.setText("");
        edPrice.setText("0");
        edPhoneNum.setText("");
        edCategory.setText("");
        edNotes.setText("");

        currenciesSpinner.setSelection(currencies.length - 1);
        paymentsSpinner.setSelection(2);

        final Calendar myCalendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(Receipt.DATE_FORMAT, Locale.US);

        edPurchaseDate.setText(sdf.format(new Date()));
        edWarrantyExpirationDate.setText(sdf.format(new Date()));
        edLastReturnDate.setText(sdf.format(new Date()));

    }

    private List<String> parseCategories(String categories) {
        if (categories == null || categories.isEmpty())
            return null;
        return new LinkedList<>(Arrays.asList(categories.split(" \\| ")));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_done:
                collectData();
                break;

            case R.id.btn_clr:
                clearData();
                break;
        }
    }

    private void saveReceipt(Receipt receipt) {
        if (receipt.getDbID() == -1)
            new DBAction(getActivity(), mAuth.getUid()).addReceipt(receipt, this);
        else
            new DBAction(getActivity(), mAuth.getUid()).updateReceipt(receipt, this);
    }


    public Receipt getReceipt() {
        return ((EditReceiptActivity) getActivity()).getReceipt();
    }

    @Override
    public void onInsertDone(Receipt receipt, boolean success) {
        Log.d(TAG, "Receipt was added to DB successfully" + receipt.getDbID());

        Toast.makeText(getActivity(), "Receipt was added successfully", Toast.LENGTH_SHORT).show();
        getActivity().setResult(1);
        getActivity().finish();
    }

    @Override
    public void onUpdateDone(Receipt receipt, boolean success) {
        Log.d(TAG, "Receipt was updated in DB successfully" + receipt.getDbID());
        Toast.makeText(getActivity(), "Receipt was updated successfully", Toast.LENGTH_SHORT).show();
        getActivity().setResult(1);
        getActivity().finish();
    }

    @Override
    public void onDeleteDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onReceiptsLoaded(List<Receipt> receipts, boolean success) {

    }

    private String[] currencies = {"JPY", "CNY", "SDG", "RON", "MKD", "MXN", "CAD",
            "ZAR", "AUD", "NOK", "ILS", "ISK", "SYP", "LYD", "UYU", "YER", "CSD",
            "EEK", "THB", "IDR", "LBP", "AED", "BOB", "QAR", "BHD", "HNL", "HRK",
            "COP", "ALL", "DKK", "MYR", "SEK", "RSD", "BGN", "DOP", "KRW", "LVL",
            "VEF", "CZK", "TND", "KWD", "VND", "JOD", "NZD", "PAB", "CLP", "PEN",
            "GBP", "DZD", "CHF", "RUB", "UAH", "ARS", "SAR", "EGP", "INR", "PYG",
            "TWD", "TRY", "BAM", "OMR", "SGD", "MAD", "BYR", "NIO", "HKD", "LTL",
            "SKK", "GTQ", "BRL", "EUR", "HUF", "IQD", "CRC", "PHP", "SVC", "PLN",
            "USD", "Currency"};
}
