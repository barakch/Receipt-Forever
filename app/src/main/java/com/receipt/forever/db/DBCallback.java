package com.receipt.forever.db;

import com.receipt.forever.model.Receipt;
import java.util.List;

public interface DBCallback {

    void onInsertDone(Receipt receipt, boolean success);
    void onUpdateDone(Receipt receipt, boolean success);
    void onDeleteDone(Receipt receipt, boolean success);
    void onReceiptsLoaded(List<Receipt> receipts, boolean success);
}
