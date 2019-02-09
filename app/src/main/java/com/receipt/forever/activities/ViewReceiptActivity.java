package com.receipt.forever.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.receipt.forever.BuildConfig;
import com.receipt.forever.R;
import com.receipt.forever.model.Receipt;
import com.receipt.forever.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewReceiptActivity extends AppCompatActivity {

    private Receipt receipt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);

        if (savedInstanceState == null) {
            Intent i = getIntent();

            if (i.getSerializableExtra("Receipt") == null)
                finish();

            receipt = (Receipt) i.getSerializableExtra("Receipt");


            ((TextView) findViewById(R.id.tv_receipt_name)).setText(receipt.getReceiptName() + "");
            ((TextView) findViewById(R.id.tv_company_name)).setText(receipt.getCompanyName() + "");
            ((TextView) findViewById(R.id.tv_payment_method)).setText(receipt.getPaymentMethod() != null? receipt.getPaymentMethod().toString() : "");
            ((TextView) findViewById(R.id.tv_price)).setText(String.format("%.2f",  receipt.getPrice()) + " " + (receipt.getCurrency() != null ? receipt.getCurrency().toString() : ""));
            ((TextView) findViewById(R.id.tv_purchase_date)).setText(receipt.getPurchaseDate());
            ((TextView) findViewById(R.id.tv_warranty_expiration_date)).setText(receipt.getWarrantyExpirationDate());
            ((TextView) findViewById(R.id.tv_last_return_date)).setText(receipt.getLastReturnDate());
            ((TextView) findViewById(R.id.tv_phone_number)).setText(receipt.getPhoneNumber() + "");
            ((TextView) findViewById(R.id.tv_categories)).setText(receipt.getCategoriesAsString());
            ((TextView) findViewById(R.id.tv_notes)).setText(receipt.getNotes() + "");
            ((TextView) findViewById(R.id.tv_web_links)).setText(receipt.getFormattedWebItems());


            ImageView imgReceipt = findViewById(R.id.img_receipt);
            Picasso.get().load("file://" + receipt.getImagePath()).into(imgReceipt);

            imgReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri photoURI = FileProvider.getUriForFile(ViewReceiptActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(receipt.getImagePath()));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(photoURI, "image/*");
                    startActivity(intent);
                }
            });


            findViewById(R.id.img_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.shareImageOnPlatform(ViewReceiptActivity.this, receipt.toString(), receipt.getImagePath());
                }
            });

            findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editReceiptActivityIntent = new Intent(ViewReceiptActivity.this, EditReceiptActivity.class);
                    editReceiptActivityIntent.putExtra("Receipt", receipt);
                    ViewReceiptActivity.this.startActivityForResult(editReceiptActivityIntent, 1);
                    finish();
                }
            });


            findViewById(R.id.btn_add_calendar_expiration).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String description = "This is the last day to use the warranty for " + receipt.getReceiptName();
                    if(receipt.getCompanyName() != null && !receipt.getCompanyName().isEmpty())
                        description += " at " + receipt.getCompanyName();

                    String title = receipt.getReceiptName() + " warranty expiration date";

                    Utils.addACalendarReminder(ViewReceiptActivity.this, receipt.getFormattedWarrantyExpirationDate(), title, description);
                }
            });


            findViewById(R.id.btn_add_calendar_return).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String description = "This is the last day to return " + receipt.getReceiptName();
                    if(receipt.getCompanyName() != null && !receipt.getCompanyName().isEmpty())
                        description += " to " + receipt.getCompanyName();
                    String title = receipt.getReceiptName() + " return date";

                    Utils.addACalendarReminder(ViewReceiptActivity.this, receipt.getFormattedLastReturnDate(), title, description);
                }
            });
        }
    }
}
