package ru.nettrash.sibcoin.classes;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ru.nettrash.sibliteandroid.R;

/**
 * Created by nettrash on 02.02.2018.
 */

public final class sibMemPoolItem {

    public String Address;
    public String txId;
    public int N;
    public long value;
    public long seconds;
    public String prev_txId;
    public int prev_N;
    public boolean isInput;

    public sibMemPoolItem(JSONObject object) {

    }

    public HashMap<String, Object> getHashMap() {
        HashMap retVal = new HashMap<String, Object>();
        retVal.put("amount", (value>=0 ? "+ " : "- ")+String.format("%.2f", value * (value >=0 ? 1 : -1)));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        retVal.put("date", formatter.format(new Date()));
        retVal.put("icon", value>=0 ? R.drawable.incoming : R.drawable.outgoing);
        return retVal;
    }
}
