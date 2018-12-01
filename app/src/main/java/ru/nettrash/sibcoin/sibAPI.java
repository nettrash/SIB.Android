package ru.nettrash.sibcoin;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import ru.nettrash.crypto.MD5;
import ru.nettrash.sibcoin.classes.sibBuyState;
import ru.nettrash.sibcoin.classes.sibHistoryItem;
import ru.nettrash.sibcoin.classes.sibMemPoolItem;
import ru.nettrash.sibcoin.classes.sibRateItem;
import ru.nettrash.sibcoin.classes.sibUnspentTransaction;
import ru.nettrash.sibcoin.models.rootModel;
import ru.nettrash.util.Arrays;

import static java.lang.Math.pow;

/**
 * Created by nettrash on 26.01.2018.
 */

public final class sibAPI {

    private String urlAPIRoot;
    private String userAPI;
    private String passwordAPI;

    @NonNull
    private String _calcAuth() {
        String md5src = userAPI+passwordAPI;
        MD5 md5 = new MD5();
        md5.update(md5src.getBytes());
        byte[] md5digest = md5.digest();
        String ServicePassword = Arrays.toHexString(md5digest).toUpperCase();
        String data = userAPI+":"+ ServicePassword;
        return new String(Base64.encode(data.getBytes(), Base64.NO_WRAP));
    }

    @NonNull
    private String _sendPOST(String sUrl, String sRequest) throws Exception {
        URL url = new URL(sUrl);
        HttpsURLConnection c = (HttpsURLConnection)url.openConnection();
        c.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return HttpsURLConnection.getDefaultHostnameVerifier().verify("your_domain.com", session);
                return true;
            }
        });
        c.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        c.setUseCaches(false);
        c.setDoInput(true);
        c.setDoOutput(true);
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Authorization", "Basic " + _calcAuth());
        c.setRequestProperty("Content-Length", String.valueOf(sRequest.length()));
        c.setConnectTimeout(30000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);

        OutputStream output = c.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(output, "UTF-8"));
        writer.write(sRequest);
        writer.flush();
        writer.close();
        output.close();

        c.connect();

        if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream input = c.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return new String(baos.toByteArray(), "UTF-8");
        } else {
            throw new Exception(Integer.toString(c.getResponseCode()) + " " + c.getResponseMessage());
        }
    }

    public sibAPI() {
        urlAPIRoot = "https://service.biocoin.pro/wallet/sib/sib.svc";
        userAPI = "SIB";
        passwordAPI = "E0FB115E-80D8-4F4E-9701-E655AF9E84EB";
    }

    public sibAPI(String url) {
        urlAPIRoot = url;
        userAPI = "SIB";
        passwordAPI = "E0FB115E-80D8-4F4E-9701-E655AF9E84EB";
    }

    public sibAPI(String url, String user, String password) {
        urlAPIRoot = url;
        userAPI = user;
        passwordAPI = password;
    }

    @Contract(pure = true)
    public double getBalance(String[] addresses) throws Exception {
        String url = urlAPIRoot + "/balance";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("addresses", new JSONArray(addresses));

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("BalanceResult");
        if (result.getBoolean("Success")) {

            return result.getDouble("Value") / pow(10, 8);
        } else {
            throw new Exception("Error get balance");
        }
    }

    @Nullable
    public ArrayList<sibHistoryItem> getLastTransactions(int last, String[] addresses, String[] inputAddresses, String[] changeAddresses) throws Exception {
        String url = urlAPIRoot + "/transactions";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("addresses", new JSONArray(addresses));
        postDataParams.put("last", last);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("TransactionsResult");
        if (result.getBoolean("Success")) {

            ArrayList<sibHistoryItem> retVal = new ArrayList<sibHistoryItem>();

            try {
                JSONArray items = result.getJSONArray("Items");
                for (int idx = 0; idx < items.length(); idx++) {
                    JSONObject obj = items.getJSONObject(idx);

                    retVal.add(new sibHistoryItem(obj, inputAddresses, changeAddresses));

                }
            } catch (Exception ex) {

            }

            return retVal;


        } else {
            throw new Exception("Error get last ops");
        }
    }

    @Nullable
    public ArrayList<sibMemPoolItem> getMemPoolTransactions(String[] addresses) throws Exception {
        String url = urlAPIRoot + "/mempool";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("addresses", new JSONArray(addresses));

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("MemoryPoolResult");
        if (result.getBoolean("Success")) {

            ArrayList<sibMemPoolItem> retVal = new ArrayList<sibMemPoolItem>();

            try {
                JSONArray items = result.getJSONArray("Items");
                for (int idx = 0; idx < items.length(); idx++) {
                    JSONObject obj = items.getJSONObject(idx);

                    retVal.add(new sibMemPoolItem(obj));

                }
            } catch (Exception ex) {

            }

            return retVal;


        } else {
            throw new Exception("Error get balance");
        }
    }

    public boolean checkInputExists(String address) throws Exception {
        String url = urlAPIRoot + "/hasInput";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("address", address);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("InputExistsResult");
        return result.getBoolean("Success") && result.getBoolean("Exists");
    }

    @Nullable
    public ArrayList<sibUnspentTransaction> getUnspentTransactions(String[] addresses) throws Exception {
        String url = urlAPIRoot + "/unspentTransactions";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("addresses", new JSONArray(addresses));
        postDataParams.put("last", 3);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("UnspentTransactionsResult");
        if (result.getBoolean("Success")) {

            ArrayList<sibUnspentTransaction> retVal = new ArrayList<sibUnspentTransaction>();

            try {
                JSONArray items = result.getJSONArray("Items");
                for (int idx = 0; idx < items.length(); idx++) {
                    JSONObject obj = items.getJSONObject(idx);

                    retVal.add(new sibUnspentTransaction(obj));
                }
            } catch (Exception ex) {

            }

            return retVal;


        } else {
            throw new Exception("Error get unspent");
        }
    }

    public sibBroadcastTransactionResult broadcastTransaction(int[] sign) throws Exception {
        String url = urlAPIRoot + "/broadcastTransaction";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("rawtx", Base64.encodeToString(Arrays.toByteArray(sign), Base64.NO_WRAP));

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("BroadcastTransactionResult");
        sibBroadcastTransactionResult retVal = new sibBroadcastTransactionResult();
        retVal.IsBroadcasted = result.getBoolean("Success");
        if (result.getBoolean("Success")) {
            retVal.TransactionId = result.getString("TransactionId");
        } else {
            retVal.Message = result.getString("Message");
        }
        return retVal;
    }

    public ArrayList<sibRateItem> getRates() throws Exception {
        String url = urlAPIRoot + "/currentRates";

        String sResponse = _sendPOST(url, "");
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("CurrentRatesResult");
        if (result.getBoolean("Success")) {

            ArrayList<sibRateItem> retVal = new ArrayList<sibRateItem>();

            try {
                JSONArray items = result.getJSONArray("Items");
                for (int idx = 0; idx < items.length(); idx++) {
                    JSONObject obj = items.getJSONObject(idx);

                    retVal.add(new sibRateItem(obj));

                }
            } catch (Exception ex) {

            }

            return retVal;


        } else {
            throw new Exception("Error get rates");
        }
    }

    @Contract(pure = true)
    public double getSellRate(String currency) throws Exception {
        String url = urlAPIRoot + "/sellRate";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("currency", currency);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("SellRateResult");

        if (result.getBoolean("Success")) {
            return result.getDouble("Rate");
        } else {
            throw new Exception("Error get sell rate");
        }
    }

    @Contract(pure = true)
    public String processSell(String currency, Double amountSIB, Double amount, String pan) throws Exception {
        String url = urlAPIRoot + "/registerSell";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("pan", pan);
        postDataParams.put("amountSIB", amountSIB);
        postDataParams.put("amount", amount);
        postDataParams.put("currency", currency);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("RegisterSellResult");

        if (result.getBoolean("Success")) {
            return result.getString("Address");
        } else {
            throw new Exception("Error process sell");
        }
    }

    @Contract(pure = true)
    public double getBuyRate(String currency) throws Exception {
        String url = urlAPIRoot + "/buyRate";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("currency", currency);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("BuyRateResult");

        if (result.getBoolean("Success")) {
            return result.getDouble("Rate");
        } else {
            throw new Exception("Error get buy rate");
        }
    }

    @Contract(pure = true)
    public double getBuyRateWithAmount(String currency, Double amount) throws Exception {
        String url = urlAPIRoot + "/buyRateWithAmount";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("currency", currency);
        postDataParams.put("amount", amount);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("BuyRateWithAmountResult");

        if (result.getBoolean("Success")) {
            return result.getDouble("Rate");
        } else {
            throw new Exception("Error get buy rate");
        }
    }

    @NonNull
    @Contract(pure = true)
    public sibBuyState processBuy(String currency, Double amountSIB, Double amount, String pan, String exp, String cvv, String account, String address) throws Exception {
        String url = urlAPIRoot + "/registerBuy";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("account", account);
        postDataParams.put("pan", pan);
        postDataParams.put("exp", exp);
        postDataParams.put("cvv", cvv);
        postDataParams.put("amountSIB", amountSIB);
        postDataParams.put("amount", amount);
        postDataParams.put("currency", currency);
        postDataParams.put("address", address);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("RegisterBuyResult");

        if (result.getBoolean("Success")) {
            return new sibBuyState(result.getString("State"), result.getString("RedirectUrl"));
        } else {
            throw new Exception("Error process buy");
        }
    }

    @NonNull
    @Contract(pure = true)
    public sibBuyState checkOperation(String opKey) throws Exception {
        String url = urlAPIRoot + "/checkOp";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("OpKey", opKey);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("CheckOpResult");

        if (result.getBoolean("Success")) {
            return new sibBuyState(result.getString("State"), "");
        } else {
            throw new Exception("Error process buy");
        }
    }

    @NonNull
    public String getNewBitPayAddress() throws Exception {
        String url = urlAPIRoot + "/getNewBitPayAddress";

        String sResponse = _sendPOST(url, "");
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("GetNewBitPayAddressResult");
        if (result.getBoolean("Success")) {
            return result.getString("Address");
        } else {
            throw new Exception("Unable to get SIB address for payment.");
        }
    }

    public sibBroadcastTransactionResult payInvoice(String invoice, int[] sign, String sibAddress, Double sibAmount, String otherAddress, Double otherAmount) throws Exception {
        String url = urlAPIRoot + "/payInvoice";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("invoice", Base64.encodeToString(invoice.getBytes(), Base64.NO_WRAP));
        postDataParams.put("tx", Base64.encodeToString(Arrays.toByteArray(sign), Base64.NO_WRAP));
        postDataParams.put("address", sibAddress);
        postDataParams.put("amount", sibAmount);
        postDataParams.put("otherAddress", otherAddress);
        postDataParams.put("otherAmount", otherAmount);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("PayInvoiceResult");
        sibBroadcastTransactionResult retVal = new sibBroadcastTransactionResult();
        retVal.IsBroadcasted = result.getBoolean("Success");
        if (result.getBoolean("Success")) {
            retVal.TransactionId = result.getString("TransactionId");
            //retVal.BTCTransactionId = result.getString("BTCTransactionId");
            retVal.Message = result.getString("Message");
        } else {
            retVal.Message = result.getString("Message");
        }
        return retVal;
    }

}

