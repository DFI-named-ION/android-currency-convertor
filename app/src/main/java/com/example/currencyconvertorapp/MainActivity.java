package com.example.currencyconvertorapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView currency_rate_left;
    private TextView currency_rate_right;
    private EditText valueTop;
    private EditText valueBottom;
    private Spinner currencyTop;
    private Spinner direction;
    private Spinner currencyBottom;
    private Button convertBtn;
    private Button getRateBtn;

    private class ImageArrayAdapter extends ArrayAdapter<String> {

        public ImageArrayAdapter(Context context, String[] values) {
            super(context, R.layout.direction_item, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        private View createCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.direction_item, parent, false);
            ImageView imageView = view.findViewById(R.id.imageView);

            String item = getItem(position);
            int imageResId = getContext().getResources()
                    .getIdentifier(item, "drawable", getContext().getPackageName());
            imageView.setImageResource(imageResId);

            return view;
        }
    }

    private class TetxArrayAdapter extends ArrayAdapter<String> {

        public TetxArrayAdapter(Context context, String[] values) {
            super(context, R.layout.currency_item, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createCustomView(position, convertView, parent);
        }

        private View createCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.currency_item, parent, false);
            TextView textView = view.findViewById(R.id.currencyView);

            String item = getItem(position);
            String text = getContext().getResources().getStringArray(R.array.currency_array)[position];
            textView.setText(text);

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        currencyTop = findViewById(R.id.currency_top);
        direction = findViewById(R.id.change_direction);
        currencyBottom = findViewById(R.id.currency_bottom);

        direction.setAdapter(new ImageArrayAdapter(this, getResources().getStringArray(R.array.direction_array)));
        currencyTop.setAdapter(new TetxArrayAdapter(this, getResources().getStringArray(R.array.currency_array)));
        currencyBottom.setAdapter(new TetxArrayAdapter(this, getResources().getStringArray(R.array.currency_array)));

        valueBottom = findViewById(R.id.value_bottom);
        valueTop = findViewById(R.id.value_top);
        currency_rate_left = findViewById(R.id.currency_rate_left);
        currency_rate_right = findViewById(R.id.currency_rate_right);

        convertBtn = findViewById(R.id.btn_convert);
        getRateBtn = findViewById(R.id.btn_get_coefs);

        convertBtn.setOnClickListener(view -> {
            String convertTo = direction.getSelectedItem().toString();
            String cT = currencyTop.getSelectedItem().toString();
            String cB = currencyBottom.getSelectedItem().toString();

            String accessKey = "3ab2dc12b4726794a9dbdef0";
            String pattern = "https://v6.exchangerate-api.com/v6/%s/latest/%s";
            String apiUrl = String.format(pattern, accessKey, convertTo.equals("arrow_up") ? cB : cT);

            new ApiDataParser(OperationType.Calculations).execute(apiUrl);
        });

        getRateBtn.setOnClickListener(view -> {
            String convertTo = direction.getSelectedItem().toString();
            String cT = currencyTop.getSelectedItem().toString();
            String cB = currencyBottom.getSelectedItem().toString();

            String accessKey = "3ab2dc12b4726794a9dbdef0";
            String pattern = "https://v6.exchangerate-api.com/v6/%s/latest/%s";
            String apiUrl = String.format(pattern, accessKey, convertTo.equals("arrow_up") ? cB : cT);

            new ApiDataParser(OperationType.Gathering).execute(apiUrl);
        });
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "MyChannel", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel for My App");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void showNotification(String title, String content) {
        int notificationId = new Random().nextInt((9999 - 1) + 1) + 1;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    private enum OperationType {
        Calculations,
        Gathering
    }

    @SuppressLint("StaticFieldLeak")
    private class ApiDataParser extends AsyncTask<String, String, String> {
        private final OperationType operationType;

        public ApiDataParser(OperationType operationType) {
            this.operationType = operationType;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection();

                InputStream stream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buff = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buff.append(line).append('\n');
                }

                return buff.toString();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        @SuppressLint("DefaultLocale")
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String cT = currencyTop.getSelectedItem().toString();
                    String cB = currencyBottom.getSelectedItem().toString();
                    String convertTo = direction.getSelectedItem().toString();
                    JSONObject json = new JSONObject(result);

                    switch (operationType){
                        case Gathering:
                            switch (convertTo) {
                                case "arrow_up":
                                    currency_rate_left.setText(json.getJSONObject("conversion_rates").getString(cB) + " " + cB);
                                    currency_rate_right.setText(json.getJSONObject("conversion_rates").getString(cT) + " " + cT);
                                    break;
                                case "arrow_down":
                                    currency_rate_left.setText(json.getJSONObject("conversion_rates").getString(cT) + " " + cT);
                                    currency_rate_right.setText(json.getJSONObject("conversion_rates").getString(cB) + " " + cB);
                                    break;
                            }

                            break;
                        case Calculations:
                            double res = 0;

                            switch (convertTo) {
                                case "arrow_up":
                                    String valueBottomText = valueBottom.getText().toString();
                                    if (!valueBottomText.isEmpty()) {
                                        double vB = Double.parseDouble(valueBottomText);
                                        res = Double.parseDouble(json.getJSONObject("conversion_rates").getString(cT)) * vB;
                                        valueTop.setText(String.valueOf(res));
                                    }
                                    break;

                                case "arrow_down":
                                    String valueTopText = valueTop.getText().toString();
                                    if (!valueTopText.isEmpty()) {
                                        double vT = Double.parseDouble(valueTopText);
                                        res = Double.parseDouble(json.getJSONObject("conversion_rates").getString(cB)) * vT;
                                        valueBottom.setText(String.valueOf(res));
                                    }
                                    break;
                            }
                            break;
                    }

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}