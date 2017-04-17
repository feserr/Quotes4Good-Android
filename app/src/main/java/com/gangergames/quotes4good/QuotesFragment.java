package com.gangergames.quotes4good;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Copyright by Elias Serrano [2016].
 */
public class QuotesFragment extends Fragment implements
        View.OnClickListener {
    private static final int NOTIFICATION_ID = 1234;
    private static final String ANOTHER_ACTION = "Another";
    private final String TAG = "QuotesFragment";

    private static final String QUOTES_SHARE_HASHTAG = " #Quotes4Good";
    private String mQuote;

    private ShareActionProvider mShareActionProvider;

    private Context activityContext = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private NotificationManager notificationManager = null;

    private NotificationCompat.Builder notificationBuilder = null;
    private Notification quoteNotification = null;

    private TextView quoteView = null;
    private TextView authorView = null;

    private List listQuotes = null;
    private Random rand = null;
    private static Row actualRow = null;

    private static BroadcastReceiver mReceiver;

    public class UpdateQuote extends AsyncTask<Void, Void, Void> {
        Row row = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            row = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set quote
            quoteView.setText("\"" + row.quote + "\"");

            // Set author
            authorView.setText("- " + row.author);

            changeNotification(row);

            // Notify swipeRefreshLayout that the refresh has finished
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public QuotesFragment() {
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the main view
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get the main activity context
        activityContext = this.getActivity();

        // Set the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_main);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        UpdateQuote updateQuote = new UpdateQuote();
                        updateQuote.execute();
                    }
                }
        );

        notificationManager = (NotificationManager) activityContext.getSystemService(NOTIFICATION_SERVICE);

        rand = new Random();

        InputStream xmlQuotes = getResources().openRawResource(R.raw.quotes);

        try {
            listQuotes = Utility.parse(xmlQuotes);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get quotes textviews
        quoteView = (TextView) rootView.findViewById(R.id.quote);
        authorView = (TextView) rootView.findViewById(R.id.author);

        // Get a randon row
        if (actualRow == null) {
            actualRow = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));

            changeMainQuote();
        }

        makeNotification(activityContext);
        changeNotification(actualRow);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ANOTHER_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ANOTHER_ACTION)) {
                    actualRow = getNewQuote();
                    changeNotification(actualRow);
                    changeMainQuote();
                }
            }
        };

        activityContext.registerReceiver(mReceiver, filter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // We still need this for the share intent
        mQuote = String.format("\"%s\" - %s", actualRow.quote, actualRow.author);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mQuote != null) {
            mShareActionProvider.setShareIntent(createShareQuoteIntent());
        }
    }

    private Intent createShareQuoteIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mQuote);
        return shareIntent;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (actualRow != null)
            changeMainQuote();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                Utility.writeLog(TAG, "Unknown button id");
                break;
        }
    }

    private Row getNewQuote() {
        // Get a randon row
        actualRow = (Row) listQuotes.get(rand.nextInt(listQuotes.size()));
        return actualRow;
    }

    private void changeMainQuote() {
        if (actualRow != null) {
            // Set quote
            quoteView.setText("\"" + actualRow.quote + "\"");

            // Set author
            authorView.setText("-" + actualRow.author);

            // We still need this for the share intent
            mQuote = String.format("\"%sª\" - %s", actualRow.quote, actualRow.author);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void makeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent anotherReceive = new Intent(ANOTHER_ACTION);
        PendingIntent pendingIntentAnother = PendingIntent.getBroadcast(context, NOTIFICATION_ID, anotherReceive, 0);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mQuote);
        sendIntent.setType("text/plain");
        PendingIntent pendingIntentShare = PendingIntent.getActivity(context, NOTIFICATION_ID,
                Intent.createChooser(sendIntent, getString(R.string.share)), PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Quotes4Good")
                .setContentText("Test")
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_quotes_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_quotes_launcher))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Test"))
                .addAction (R.drawable.ic_next_quotes,
                        getString(R.string.get_another), pendingIntentAnother)
                .addAction (R.drawable.ic_share_quote,
                        getString(R.string.share), pendingIntentShare);

        quoteNotification = notificationBuilder.build();

        quoteNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, quoteNotification);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void changeNotification(Row row) {
        notificationBuilder.setContentText("\"" + row.quote + "\"\n -" + row.author);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("\"" + row.quote + "\"\n -" + row.author));

        quoteNotification = notificationBuilder.build();
        quoteNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, quoteNotification);
    }
}
