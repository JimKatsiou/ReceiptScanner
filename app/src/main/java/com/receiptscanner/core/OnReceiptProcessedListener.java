package com.receiptscanner.core;

import com.receiptscanner.models.ReceiptData;

public interface OnReceiptProcessedListener {
    void onSuccess(ReceiptData receiptData);
    void onError(String error);
} 