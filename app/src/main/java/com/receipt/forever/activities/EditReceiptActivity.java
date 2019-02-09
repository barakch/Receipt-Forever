package com.receipt.forever.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import com.receipt.forever.R;
import com.receipt.forever.view.EditReceiptFragment;
import com.receipt.forever.model.Receipt;

public class EditReceiptActivity extends AppCompatActivity {

    public static String TAG = "EditReceiptActivity";
    private Receipt receipt;

    private Fragment firstFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_receipt_activity);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (savedInstanceState == null) {
            Intent i = getIntent();

            if(i.getSerializableExtra("Receipt") == null)
                finish();

            receipt = (Receipt) i.getSerializableExtra("Receipt");
            firstFragment = new EditReceiptFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame_container, firstFragment).commit();

        }
    }


    public Receipt getReceipt() {
        return receipt;
    }
}
