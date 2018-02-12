package ru.nettrash.sibliteandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.qrcode.QRCodeWriter;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import ru.nettrash.sibcoin.classes.sibHistoryItem;
import ru.nettrash.sibcoin.classes.sibUnspentTransaction;
import ru.nettrash.sibcoin.database.Address;
import ru.nettrash.sibcoin.sibAPI;
import ru.nettrash.sibcoin.sibAddress;
import ru.nettrash.sibcoin.sibBroadcastTransactionResult;
import ru.nettrash.sibcoin.sibTransaction;
import ru.nettrash.sibcoin.sibWallet;
import ru.nettrash.util.Arrays;

import static android.content.ContentValues.TAG;

public class SendActivity extends BaseActivity {

    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private EditText mAddressView;
    private EditText mAmountView;
    private EditText mCommissionView;
    private ImageButton mScanView;
    private Button mSendView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    private boolean mVisible;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mContentView.requestFocus();
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        final SendActivity self = this;

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);
        mAddressView = findViewById(R.id.send_address_value);
        mAmountView = findViewById(R.id.send_amount_value);
        mCommissionView = findViewById(R.id.send_commission_value);
        mScanView = findViewById(R.id.btn_camera);
        mSendView = findViewById(R.id.btn_send);
        TextView tv = findViewById(R.id.send_balance_value);
        tv.setText(String.format("%.2f SIB", sibApplication.model.getBalance().doubleValue()));

        mAddressView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hide();
                }
                return false;
            }
        });
        mAmountView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hide();
                }
                return false;
            }
        });
        mCommissionView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hide();
                }
                return false;
            }
        });
        mScanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    self.requestPermissions(new String[] { Manifest.permission.CAMERA }, 0);
                } catch (Exception ex) {
                }
            }
        });
        mSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSend();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.initiateScan();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

            if (format.equals("QR_CODE")) {
                if (sibAddress.verify(contents)) {
                    mAddressView.setText(contents);
                    mAmountView.requestFocus();
                } else {
                    if (contents.startsWith("sibcoin:")) {
                        try {
                            Uri url = Uri.parse(contents.startsWith("sibcoin://") ? contents : contents.replace("sibcoin:", "sibcoin://"));
                            if (sibAddress.verify(url.getHost())) {
                                mAddressView.setText(url.getHost());
                                String amount = url.getQueryParameter("amount");
                                if (amount != null && !amount.equalsIgnoreCase("")) {
                                    mAmountView.setText(amount);
                                    mCommissionView.requestFocus();
                                } else {
                                    mAmountView.requestFocus();
                                }
                            }
                        } catch (Exception ex) {

                        }
                    }
                }
            }

        } else if (resultCode == RESULT_CANCELED) {
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void doSend() {

        final class unspentTransactionsAsyncTask extends AsyncTask<Void, Void, ArrayList<sibUnspentTransaction>> {

            protected sibAPI api = new sibAPI();
            protected String[] addresses = new String[0];

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                findViewById(R.id.fullscreen_wait).setVisibility(View.VISIBLE );
                ArrayList<String> addrs = new ArrayList<String>();
                try {

                    for (Address a : sibApplication.model.getAddresses()) {
                        addrs.add(a.getAddress());
                    }

                    addresses = addrs.toArray(new String[0]);

                } catch (Exception ex) {
                    this.cancel(true);
                }
            }

            @Nullable
            @Override
            protected ArrayList<sibUnspentTransaction> doInBackground(Void... params) {
                try {
                    return api.getUnspentTransactions(addresses);
                } catch (Exception ex) {
                    this.cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<sibUnspentTransaction> result) {
                super.onPostExecute(result);
                if (result != null) {
                    try {

                        sibTransaction tx = prepareTransaction(result.toArray(new sibUnspentTransaction[0]));
                        sendTransaction(tx);

                    } catch (Exception ex) {
                        findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );
                    }
                }
            }

            @Override
            protected void onCancelled(ArrayList<sibUnspentTransaction> result) {
                super.onCancelled(result);
                findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );
            }
        }

        hideKeyboard();

        new unspentTransactionsAsyncTask().execute();

    }

    private sibTransaction prepareTransaction(sibUnspentTransaction[] unspent) throws Exception {
        Double spent = 0.0;
        Double amount = Double.valueOf(mAmountView.getText().toString());
        Double commission = Double.valueOf(mCommissionView.getText().toString());

        sibTransaction tx = new sibTransaction();
        tx.addOutput(mAddressView.getText().toString(), amount);

        for (sibUnspentTransaction u: unspent) {
            if (spent < amount + commission) {
                spent += u.Amount;
                tx.addInput(u);
			} else {
                break;
            }
        }
        tx.addChange(spent - amount - commission);
        return tx;
    }

    private void sendTransaction(sibTransaction tx) throws Exception {
        sibApplication.model.storeWallet(tx.getChange(), (short)1);
        int[] sign = tx.sign(sibApplication.model.getAddresses().toArray(new Address[0]));

        final SendActivity self = this;

        final class broadcastTransactionAsyncTask extends AsyncTask<int[], Void, sibBroadcastTransactionResult> {

            protected sibAPI api = new sibAPI();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected sibBroadcastTransactionResult doInBackground(int[]... params) {
                try {
                    return api.broadcastTransaction(params[0]);
                } catch (Exception ex) {
                    this.cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(sibBroadcastTransactionResult result) {
                super.onPostExecute(result);
                String message = "";
                if (result.IsBroadcasted) {
                    message = getResources().getString(R.string.successBroadcasted) + " " + result.TransactionId;
                } else {
                    message = result.Message;
                }

                final String txid = result.TransactionId;

                findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );

                AlertDialog.Builder builder = new AlertDialog.Builder(self);
                builder.setTitle(R.string.alertDialogBroadcastTitle)
                        .setMessage(message)
                        .setCancelable(false)
                        .setNeutralButton(R.string.CopyToClipboard,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText(getResources().getString(R.string.sibTransactionId), txid);
                                        clipboard.setPrimaryClip(clip);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(self);
                                        builder.setTitle(R.string.alertDialogClipboardTitle)
                                                .setMessage(R.string.alertDialogClipboardMessage)
                                                .setCancelable(false)
                                                .setNegativeButton(R.string.OK,
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();

                                                                self.finish();
                                                            }
                                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();                                    }
                                })
                        .setNegativeButton(R.string.OK,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        self.finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            protected void onCancelled(sibBroadcastTransactionResult result) {
                super.onCancelled(result);
                findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                findViewById(R.id.fullscreen_wait).setVisibility(View.INVISIBLE );
            }
        }

        new broadcastTransactionAsyncTask().execute(sign);
    }
}
