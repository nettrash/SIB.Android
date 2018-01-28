package ru.nettrash.sibcoin.classes;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ru.nettrash.sibliteandroid.R;

/**
 * Created by nettrash on 24.01.2018.
 */

public final class sibHistoryItem {

    public String txId;
    public short txType;
    public Date txDate;
    public double Amount;
    public String Address;

    public sibHistoryItem(String txId, short txType, Date txDate, double Amount, String Address) {
        this.txId = txId;
        this.txType = txType;
        this.txDate = txDate;
        this.Amount = Amount;
        this.Address = Address;
    }

    public HashMap<String, Object> getHashMap() {
        HashMap retVal = new HashMap<String, Object>();
        retVal.put("amount", (txType==0 ? "+ " : "- ")+String.format("%.2f", Amount));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        retVal.put("date", formatter.format(txDate));
        retVal.put("icon", txType==0 ? R.drawable.incoming : R.drawable.outgoing);
        return retVal;
    }

    @NonNull
    @Contract(pure = true)
    public static String[] getListAdapterFrom() {
        return new String[] { "amount", "date", "icon" };
    }

    @NonNull
    @Contract(pure = true)
    public static int[] getListAdapterTo() {
        return new int[] { R.id.history_item_amount, R.id.history_item_date, R.id.history_item_icon };
    }
}
