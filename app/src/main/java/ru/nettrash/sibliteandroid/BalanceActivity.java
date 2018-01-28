package ru.nettrash.sibliteandroid;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import ru.nettrash.sibcoin.classes.sibHistoryItem;
import ru.nettrash.sibcoin.database.Address;
import ru.nettrash.sibcoin.sibAPI;

public class BalanceActivity extends BaseActivity {

    private int _refreshLastOpsCount = 0;

    private int _firstX;
    private int _firstY;
    private static final int SWIPE_MIN_X_DISTANCE = 100;
    private static final int SWIPE_MIN_Y_DISTANCE = 10;
    private int mSelectedSegment = 0;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private BalanceActivity self;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private TextView mBalanceView;
    private ImageButton mActionButton;
    private ImageButton mActionButtonReceive;
    private ImageButton mActionButtonSend;
    private ImageButton mActionButtonHistory;
    private ImageButton mActionButtonSettings;
    private LinearLayout mLayoutActionButtonReceive;
    private LinearLayout mLayoutActionButtonSend;
    private LinearLayout mLayoutActionButtonHistory;
    private ImageView mImageTap;

    private ViewFlipper mViewFlipper;

    private ListView mLastHistoryListView;
    private TextView mLabelNoOps;
    private SwipeRefreshLayout mSwipeRefreshLastOps;

    private Button mSegmentButtonSIB;
    private Button mSegmentButtonRates;
    private Button mSegmentButtonBuy;
    private Button mSegmentButtonSell;

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
            // Delayed display of UI elements
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
            hide();
        }
    };

    private void refreshLastHistory() {
        //get last history

        final class lastTransactionsAsyncTask extends AsyncTask<Void, Void, ArrayList<sibHistoryItem>> {

            protected sibAPI api = new sibAPI();
            protected String[] addresses = new String[0];

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mBalanceView.setText(R.string.balanceRefreshInProgress);
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

            @Override
            protected ArrayList<sibHistoryItem> doInBackground(Void... params) {
                try {
                    return api.getLastTransaction(addresses);
                } catch (Exception ex) {
                    this.cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<sibHistoryItem> result) {
                super.onPostExecute(result);
                if (result != null) {
                    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                    for (sibHistoryItem item: result) {
                        data.add(item.getHashMap());
                    }
                    SimpleAdapter adapter = new SimpleAdapter(self, data, R.layout.last_history_item, sibHistoryItem.getListAdapterFrom(), sibHistoryItem.getListAdapterTo());
                    mLastHistoryListView.setAdapter(adapter);
                    mLabelNoOps.setVisibility(result.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                }
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }

            @Override
            protected void onCancelled(ArrayList<sibHistoryItem> result) {
                super.onCancelled(result);
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }
        }

        _refreshLastOpsCount++;
        new lastTransactionsAsyncTask().execute();
    }

    private void refreshBalance()  {
        final class balanceAsyncTask extends AsyncTask<Void, Void, Double> {

            protected sibAPI api = new sibAPI();
            protected String[] addresses = new String[0];

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mBalanceView.setText(R.string.balanceRefreshInProgress);
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

            @Override
            protected Double doInBackground(Void... params) {
                try {
                    return Double.valueOf(api.getBalance(addresses));
                } catch (Exception ex) {
                    this.cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Double result) {
                super.onPostExecute(result);
                if (result != null) {
                    mBalanceView.setText(String.format("%.2f", result.doubleValue()).replace(",", "."));
                }
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }

            @Override
            protected void onCancelled(Double result) {
                super.onCancelled(result);
                mBalanceView.setText(R.string.balanceRefreshError);
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                mBalanceView.setText(R.string.balanceRefreshError);
                _refreshLastOpsCount--;
                if (_refreshLastOpsCount<=0) {
                    _refreshLastOpsCount = 0;
                    mSwipeRefreshLastOps.setRefreshing(false);
                }
            }
        }

        _refreshLastOpsCount++;
        new balanceAsyncTask().execute();
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
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

        setContentView(R.layout.activity_balance);

        self = this;

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);
        mBalanceView = findViewById(R.id.balance_value);
        mActionButton = findViewById(R.id.button_action);
        mActionButtonReceive = findViewById(R.id.button_receive);
        mActionButtonSend = findViewById(R.id.button_send);
        mActionButtonHistory = findViewById(R.id.button_history);
        mActionButtonSettings = findViewById(R.id.button_settings);
        mImageTap = findViewById(R.id.image_tap);

        mLayoutActionButtonReceive = findViewById(R.id.action_layout_receive);
        mLayoutActionButtonSend = findViewById(R.id.action_layout_send);
        mLayoutActionButtonHistory = findViewById(R.id.action_layout_history);


        mViewFlipper = findViewById(R.id.view_flipper);

        mLastHistoryListView = findViewById(R.id.last_history_view);
        mLabelNoOps = findViewById(R.id.label_no_ops);
        mSwipeRefreshLastOps = findViewById(R.id.last_ops_refresh);

        mSegmentButtonSIB = findViewById(R.id.segment_button_sib);
        mSegmentButtonRates = findViewById(R.id.segment_button_rates);
        mSegmentButtonBuy = findViewById(R.id.segment_button_buy);
        mSegmentButtonSell = findViewById(R.id.segment_button_sell);

        final BalanceActivity self = this;

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float deg = mActionButton.getRotation() + 270F;
                mActionButton.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
                if (mImageTap.getVisibility() == View.VISIBLE) {
                    mImageTap.setVisibility(View.INVISIBLE);
                    mLayoutActionButtonReceive.setVisibility(View.VISIBLE);
                    mLayoutActionButtonSend.setVisibility(View.VISIBLE);
                    mLayoutActionButtonHistory.setVisibility(View.VISIBLE);
                    mActionButtonSettings.setVisibility(View.VISIBLE);
                } else {
                    mImageTap.setVisibility(View.VISIBLE);
                    mLayoutActionButtonReceive.setVisibility(View.INVISIBLE);
                    mLayoutActionButtonSend.setVisibility(View.INVISIBLE);
                    mLayoutActionButtonHistory.setVisibility(View.INVISIBLE);
                    mActionButtonSettings.setVisibility(View.INVISIBLE);
                }
            }
        });

        mBalanceView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mSwipeRefreshLastOps.setRefreshing(true);
                refreshBalance();
                refreshLastHistory();
                return true;
            }
        });

        mSwipeRefreshLastOps.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshBalance();
                        refreshLastHistory();
                    }
                }
        );

        mViewFlipper.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _firstX = (int) event.getX();
                    _firstY = (int) event.getY();
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int LastX = (int) event.getX();

                    if (_firstX - LastX > SWIPE_MIN_X_DISTANCE) {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(self, R.anim.flip_right_in));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(self, R.anim.flip_left_out));
                        mViewFlipper.showNext();
                        mSelectedSegment++;
                        if (mSelectedSegment > 3) mSelectedSegment = 0;
                        updateButtonState();
                    } else if (LastX - _firstX > SWIPE_MIN_X_DISTANCE) {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(self, R.anim.flip_left_in));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(self, R.anim.flip_right_out));
                        mViewFlipper.showPrevious();
                        mSelectedSegment--;
                        if (mSelectedSegment < 0) mSelectedSegment = 3;
                        updateButtonState();
                    }
                }

                return true;
            }

        });

        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/
    }

    public void segmentButtonClick(View view) {
        int nOldSelectedSegment = mSelectedSegment;
        int nNewSelectedSegment = nOldSelectedSegment;
        if (view == mSegmentButtonSIB) {
            nNewSelectedSegment = 0;
        }
        if (view == mSegmentButtonRates) {
            nNewSelectedSegment = 1;
        }
        if (view == mSegmentButtonBuy) {
            nNewSelectedSegment = 2;
        }
        if (view == mSegmentButtonSell) {
            nNewSelectedSegment = 3;
        }
        int delta = nOldSelectedSegment > nNewSelectedSegment ? -1 : 1;
        while (mSelectedSegment != nNewSelectedSegment) {
            if (mSelectedSegment + delta == nNewSelectedSegment) {
                mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flip_right_in));
                mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flip_left_out));
            }
            if (delta > 0) {
                mViewFlipper.showNext();
            } else {
                mViewFlipper.showPrevious();
            }
            mSelectedSegment += delta;
            updateButtonState();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
        refreshLastHistory();
        refreshBalance();
    }

    private void deselectAllSegments() {
        mSegmentButtonSIB.setBackground(getResources().getDrawable(R.drawable.segment_button_start, this.getTheme()));
        mSegmentButtonSIB.setTextColor(getResources().getColor(R.color.colorWhite, this.getTheme()));
        mSegmentButtonRates.setBackground(getResources().getDrawable(R.drawable.segment_button_middle, this.getTheme()));
        mSegmentButtonRates.setTextColor(getResources().getColor(R.color.colorWhite, this.getTheme()));
        mSegmentButtonBuy.setBackground(getResources().getDrawable(R.drawable.segment_button_middle, this.getTheme()));
        mSegmentButtonBuy.setTextColor(getResources().getColor(R.color.colorWhite, this.getTheme()));
        mSegmentButtonSell.setBackground(getResources().getDrawable(R.drawable.segment_button_end, this.getTheme()));
        mSegmentButtonSell.setTextColor(getResources().getColor(R.color.colorWhite, this.getTheme()));
    }

    private void updateButtonState() {
        deselectAllSegments();
        switch (mSelectedSegment) {
            case 1:
                mSegmentButtonRates.setBackground(getResources().getDrawable(R.drawable.segment_button_middle_selected, this.getTheme()));
                mSegmentButtonRates.setTextColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
                break;
            case 2:
                mSegmentButtonBuy.setBackground(getResources().getDrawable(R.drawable.segment_button_middle_selected, this.getTheme()));
                mSegmentButtonBuy.setTextColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
                break;
            case 3:
                mSegmentButtonSell.setBackground(getResources().getDrawable(R.drawable.segment_button_end_selected, this.getTheme()));
                mSegmentButtonSell.setTextColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
                break;
            default:
                mSegmentButtonSIB.setBackground(getResources().getDrawable(R.drawable.segment_button_start_selected, this.getTheme()));
                mSegmentButtonSIB.setTextColor(getResources().getColor(R.color.colorBlack, this.getTheme()));
                break;
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
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

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

}
