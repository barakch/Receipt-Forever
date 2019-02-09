package com.receipt.forever.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.receipt.forever.R;
import com.receipt.forever.activities.EditReceiptActivity;
import com.receipt.forever.activities.ViewReceiptActivity;
import com.receipt.forever.model.Category;
import com.receipt.forever.model.Receipt;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReceiptsListAdapter extends RecyclerView.Adapter<ReceiptsListAdapter.ReceiptViewHolder> implements Filterable {

    private final String TAG = "ReceiptsListAdapter";
    private List<Receipt> receiptsList;
    private List<Receipt> filteredReceiptsList;
    private Activity parentActivity;
    private int position;
    private List<Category> categories;
    private RecyclerView recyclerView;


    public ReceiptsListAdapter(List<Receipt> receiptsList, RecyclerView recyclerView, Activity parentActivity) {
        this.receiptsList = receiptsList;
        this.filteredReceiptsList = receiptsList;
        this.parentActivity = parentActivity;
        this.recyclerView = recyclerView;
        this.categories = new ArrayList<>();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Receipt> getReceiptsList() {
        return receiptsList;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder() >>");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receipt_item, parent, false);

        Log.e(TAG, "onCreateViewHolder() <<");
        return new ReceiptViewHolder(parent.getContext(), itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final ReceiptViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder() >> " + position);
        final Receipt receipt = filteredReceiptsList.get(position);

        holder.setCurrReceipt(receipt);
        holder.name.setText(receipt.getReceiptName());
        holder.purchase_date.setText(receipt.getPurchaseDate());
        holder.company.setText(receipt.getCompanyName() == null ? "" : receipt.getCompanyName());
        holder.price.setText(receipt.getPrice() + "");
        holder.currency.setText(receipt.getCurrency() == null ? "" : receipt.getCurrency().toString());
        Picasso.get().load("file://" + receipt.getImagePath()).into(holder.thumbImage);

        holder.receiptCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });

        holder.receiptCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewReceipt(receipt);
            }
        });
    }


    @Override
    public int getItemCount() {
        return filteredReceiptsList.size();
    }

    public class ReceiptViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        CardView receiptCard;
        ImageView thumbImage;
        TextView name;
        TextView purchase_date;
        TextView price;
        TextView currency;
        TextView company;
        Receipt currReceipt;
        Context context;

        public ReceiptViewHolder(Context context, View parent) {
            super(parent);
            this.context = context;
            receiptCard = (CardView) parent.findViewById(R.id.cv_receipt);
            thumbImage = (ImageView) parent.findViewById(R.id.img_receipt);
            name = (TextView) parent.findViewById(R.id.tv_receipt_name);
            purchase_date = (TextView) parent.findViewById(R.id.tv_purchase_date);
            price = parent.findViewById(R.id.tv_price);
            currency = parent.findViewById(R.id.tv_currency);
            company = parent.findViewById(R.id.tv_company);
            parent.setOnCreateContextMenuListener(this);
        }

        public Receipt getCurrReceipt() {
            return currReceipt;
        }

        public void setCurrReceipt(Receipt currReceipt) {
            this.currReceipt = currReceipt;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater inflater = parentActivity.getMenuInflater();
            inflater.inflate(R.menu.list_menu, menu);
        }
    }

    public void editReceipt(Receipt receipt) {
        Intent i = new Intent(parentActivity, EditReceiptActivity.class);
        i.putExtra("Receipt", receipt);
        parentActivity.startActivityForResult(i, 1);

    }

    public void viewReceipt(Receipt receipt) {
        Intent i = new Intent(parentActivity, ViewReceiptActivity.class);
        i.putExtra("Receipt", receipt);
        parentActivity.startActivityForResult(i, 1);

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                if (categories.size() > 0) {
                    Set<Receipt> filteredList = new HashSet<>();
                    for (Receipt row : receiptsList) {
                        for (Category category : categories)
                            if (category.isSelected() && row.getCategories().contains(category.getName())) {
                                filteredList.add(row);
                            }
                    }
                    if(filteredList.size() > 0 )
                        filteredReceiptsList = new ArrayList<>(filteredList);
                }


                String charString = charSequence.toString();
                if (charString.isEmpty() && !categoryWasChosen()) {
                    filteredReceiptsList = receiptsList;
                } else {
                    if(!categoryWasChosen())
                        filteredReceiptsList = receiptsList;

                    List<Receipt> filteredList = new ArrayList<>();
                    for (Receipt row : filteredReceiptsList) {
                        if (row.getReceiptName().toLowerCase().contains(charString.toLowerCase()) || row.getReceiptName().contains(charSequence) || row.getAllData().contains(charSequence.toString().toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    filteredReceiptsList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredReceiptsList;
                return filterResults;
            }


            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredReceiptsList = (ArrayList<Receipt>) filterResults.values;
                notifyDataSetChanged();
                recyclerView.invalidate();
            }
        };
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private boolean categoryWasChosen(){
        for (Category category : categories)
            if(category.isSelected())
                return true;
        return false;
    }

}
