package ru.nettrash.sibcoin;

import android.app.Service;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import ru.nettrash.crypto.MD5;
import ru.nettrash.sibcoin.classes.sibHistoryItem;
import ru.nettrash.util.Arrays;

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
        urlAPIRoot = "https://api.sib.moe/wallet/sib.svc";
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

            return result.getDouble("Value");
        } else {
            throw new Exception("Error get balance");
        }
    }

    @Nullable
    public ArrayList<sibHistoryItem> getLastTransaction(String[] addresses) throws Exception {
        String url = urlAPIRoot + "/transactions";

        JSONObject postDataParams = new JSONObject();
        postDataParams.put("addresses", new JSONArray(addresses));
        postDataParams.put("last", 3);

        String sResponse = _sendPOST(url, postDataParams.toString());
        JSONObject resp = new JSONObject(sResponse);
        JSONObject result = resp.getJSONObject("TransactionsResult");
        if (result.getBoolean("Success")) {

            ArrayList<sibHistoryItem> retVal = new ArrayList<sibHistoryItem>();

            try {
                JSONArray items = resp.getJSONArray("Items");
                for (int idx = 0; idx < items.length(); idx++) {
                    JSONObject obj = items.getJSONObject(idx);
                }
            } catch (Exception ex) {

            }

            return retVal;


        } else {
            throw new Exception("Error get balance");
        }
    }
}
