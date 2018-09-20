package ru.nettrash.sibcoin.classes;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ru.nettrash.sibliteandroid.R;

/**
 * Created by nettrash on 04.02.2018.
 */

public final class sibRateItem {

    public double Rate;
    public String Currency;

    public sibRateItem(double Rate, String Currency) {
        this.Rate = Rate;
        this.Currency = Currency;
    }

    public sibRateItem(JSONObject object) throws Exception {
        this.Currency = object.getString("Currency");
        this.Rate = object.getDouble("Rate");
    }

    public HashMap<String, Object> getHashMap() {
        HashMap retVal = new HashMap<String, Object>();
        if (Currency.toUpperCase().equals("BTC"))
            retVal.put("rate", String.format("~ %.8f %s", Rate, Currency));
        else
            retVal.put("rate", String.format("~ %.2f %s", Rate, Currency));

        return retVal;
    }

    @NonNull
    @Contract(pure = true)
    public static String[] getListAdapterFrom() {
        return new String[] { "rate" };
    }

    @NonNull
    @Contract(pure = true)
    public static int[] getListAdapterTo() {
        return new int[] { R.id.rate_value };
    }
}
