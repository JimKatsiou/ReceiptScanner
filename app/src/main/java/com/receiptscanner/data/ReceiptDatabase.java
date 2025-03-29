package com.receiptscanner.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.receiptscanner.models.ReceiptItem;

@Database(entities = {ReceiptItem.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class ReceiptDatabase extends RoomDatabase {
    public abstract ReceiptItemDao receiptItemDao();

    private static volatile ReceiptDatabase INSTANCE;

    public static ReceiptDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ReceiptDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        ReceiptDatabase.class, "receipt_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
} 