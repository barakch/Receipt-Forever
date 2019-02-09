package com.receipt.forever.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Booleans;
import com.google.firebase.auth.FirebaseAuth;
import com.receipt.forever.R;
import com.receipt.forever.activities.EditReceiptActivity;
import com.receipt.forever.activities.ViewReceiptActivity;
import com.receipt.forever.adapters.ReceiptsListAdapter;
import com.receipt.forever.db.DBAction;
import com.receipt.forever.db.DBCallback;
import com.receipt.forever.model.Category;
import com.receipt.forever.model.Receipt;
import com.receipt.forever.utils.AnalyzePictureUtility;
import com.receipt.forever.utils.ImagePicker;
import com.receipt.forever.utils.Utils;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReceiptsListFragment extends Fragment implements View.OnClickListener, AnalyzePictureUtility.AnalyzePictureCallback, DBCallback {

    public static String TAG = "ReceiptsListFragment";
    public static final int PICK_IMAGE = 12;
    Receipt currReceipt;
    private LovelyProgressDialog progressDialog;
    private RecyclerView rcReceiptsList;
    private ReceiptsListAdapter listAdapter;
    private EditText edSearch;
    private RadioButton rbPrice, rbDate;
    private boolean diffOrder;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        View layout = inflater.inflate(R.layout.receipts_list_layout, container, false);

        rcReceiptsList = layout.findViewById(R.id.rv_receipts_list);
        rcReceiptsList.setHasFixedSize(true);
        rcReceiptsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcReceiptsList.setItemAnimator(new DefaultItemAnimator());
        registerForContextMenu(rcReceiptsList);
        FloatingActionButton fab = layout.findViewById(R.id.add_new_receipt);
        edSearch = layout.findViewById(R.id.ed_search_receipt);
        rbPrice = layout.findViewById(R.id.rb_order_by_price);
        rbDate = layout.findViewById(R.id.rb_order_by_date);
        layout.findViewById(R.id.btn_filter_categories).setOnClickListener(this);
        fab.setOnClickListener(this);
        rbPrice.setOnClickListener(this);
        rbDate.setOnClickListener(this);
        diffOrder = true;

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(rcReceiptsList);
        edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchItem(edSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit) {
                searchItem(mEdit.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    void searchItem(String hint) {
        if (hint == null || listAdapter == null)
            return;

        listAdapter.getFilter().filter(hint);
    }


    private void orderByPrice() {
        Collections.sort(listAdapter.getReceiptsList(), new Comparator<Receipt>() {
            @Override
            public int compare(Receipt rc1, Receipt rc2) {
                if (rc1.getPrice() > rc2.getPrice())
                    return diffOrder? 1 : -1;
                if (rc1.getPrice() == rc2.getPrice())
                    return 0;
                else
                    return diffOrder? -1 : 1;
            }
        });
        if (diffOrder)
            diffOrder = false;
        else
            diffOrder = true;
        listAdapter.notifyDataSetChanged();
    }

    private void orderByDate() {
        Collections.sort(listAdapter.getReceiptsList(), new Comparator<Receipt>() {
            @Override
            public int compare(Receipt rc1, Receipt rc2) {
                if (rc1.getFormattedPurchaseDate().after(rc2.getFormattedPurchaseDate()))
                    return diffOrder? 1 : -1;
                if (rc1.getFormattedPurchaseDate().equals(rc2.getFormattedPurchaseDate()))
                    return 0;
                else
                    return diffOrder? -1 : 1;
            }
        });
        if (diffOrder)
            diffOrder = false;
        else
            diffOrder = true;
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReceiptList();
    }

    public void loadReceiptList() {
        new DBAction(getActivity(), mAuth.getUid()).getAllReceipts(this);
    }

    @Override
    public void onInsertDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onUpdateDone(Receipt receipt, boolean success) {

    }

    @Override
    public void onDeleteDone(Receipt receipt, boolean success) {
        loadReceiptList();
    }

    @Override
    public void onReceiptsLoaded(List<Receipt> receipts, boolean success) {
        listAdapter = new ReceiptsListAdapter(receipts,rcReceiptsList, getActivity());
        rcReceiptsList.setAdapter(listAdapter);
        listAdapter.setCategories(Receipt.getAllCategories(receipts));
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = listAdapter.getPosition();
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }
        final Receipt currReceipt = listAdapter.getReceiptsList().get(position);
        switch (item.getItemId()) {
            case R.id.menu_open:
                Intent i = new Intent(getActivity(), ViewReceiptActivity.class);
                i.putExtra("Receipt", currReceipt);
                getActivity().startActivity(i);

                break;
            case R.id.menu_edit:
                listAdapter.editReceipt(currReceipt);
                break;

            case R.id.menu_delete:
                new LovelyStandardDialog(getActivity()).setPositiveButton("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DBAction(getActivity(), mAuth.getUid()).deleteReceipt(currReceipt, ReceiptsListFragment.this);
                    }
                }).setNegativeButtonText("Cancel").setTitle("Do you want to delete this receipt?").create().show();
                break;

            case R.id.menu_share:
                Utils.shareImageOnPlatform(getActivity(), currReceipt.toString(), currReceipt.getImagePath());
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_receipt:
                pickImage();
                break;

            case R.id.rb_order_by_price:
                rbDate.setChecked(false);
                rbPrice.setChecked(true);
                orderByPrice();
                break;
            case R.id.rb_order_by_date:
                rbDate.setChecked(true);
                rbPrice.setChecked(false);
                orderByDate();
                break;

            case R.id.btn_filter_categories:
                filterCategories();

                break;
        }
    }

    private void pickImage() {
        if ((isStorageReadPermissionGranted() && isStorageWritePermissionGranted())) {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getActivity());
            startActivityForResult(chooseImageIntent, PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    String imagePath = ImagePicker.getCopiedFilePath(getActivity(), resultCode, data);
                    Log.d(TAG, "Image path: " + imagePath);
                    if (imagePath == null) {
                        Toast.makeText(getActivity(), "You didn't pick any image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showProgressDialog();
                    new AnalyzePictureUtility(getActivity()).startAnalyzingAsync(imagePath, this);
                }
                break;
        }
    }

    @Override
    public void onAnalyzeDone(boolean success, Receipt receipt) {
        if (success) {
            Log.d(TAG, "Receipt: " + receipt.toString());
            this.currReceipt = receipt;
            hideProgressDialog();
            Intent i = new Intent(getActivity(), EditReceiptActivity.class);
            i.putExtra("Receipt", receipt);
            startActivityForResult(i, 1);
        }
    }


    /**
     * Helpers
     */
    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }

        progressDialog = new LovelyProgressDialog(getActivity())
                .setTitle("Analyzing receipt...");
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }

    private void filterCategories(){
        List<String> cats = new ArrayList<>();
        List<Boolean> poses = new ArrayList<>();
        for (Category category : listAdapter.getCategories()){
            cats.add(category.getName());
            poses.add(category.isSelected());
        }
        new LovelyChoiceDialog(getActivity(), R.style.AppTheme)
                .setTitle("Choose categories")
                .setItemsMultiChoice(cats, Booleans.toArray(poses), new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                    @Override
                    public void onItemsSelected(List<Integer> positions, List<String> items) {
                            for(Category cat : listAdapter.getCategories())
                                cat.setSelected(false);

                            for (int pos : positions)
                                listAdapter.getCategories().get(pos).setSelected(true);
                        listAdapter.getFilter().filter(edSearch.getText().toString());

                    }
                })
                .setConfirmButtonText("Confirm")
                .show();
    }


    /**
     * Handle READ and WRITE permissions from the user
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            pickImage();
        }
    }


    public boolean isStorageReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public boolean isStorageWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }
}