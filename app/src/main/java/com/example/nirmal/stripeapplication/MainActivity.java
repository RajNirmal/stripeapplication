package com.example.nirmal.stripeapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.stripe.android.model.Card;
import com.stripe.android.view.CardInputWidget;
import com.stripe.model.Charge;

public class MainActivity extends AppCompatActivity {
    CardInputWidget cardInputWidget;
    Button submitButton,chargeUserButton;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListeners();
    }

    private void setListeners(){
        submitButton.setOnClickListener(cardListener);
        chargeUserButton.setOnClickListener(chargeListener);
    }

    private void initView(){
        cardInputWidget = findViewById(R.id.card_input_widget);
        submitButton = findViewById(R.id.card_details_submit);
        chargeUserButton = findViewById(R.id.charge_user);
        chargeUserButton.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait token in being generated");
        progressDialog.setTitle("Generating token");
    }

    android.view.View.OnClickListener cardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Card cardToSave = cardInputWidget.getCard();
            int isCardValid = StripeUtil.getInstance().isCardValid(cardToSave);
            if(isCardValid == 0) {
                Log.i(StripeConstants.LOGGER_CONSTANT,"Incomplete card data");
            }else if(isCardValid == 1){
                Log.i(StripeConstants.LOGGER_CONSTANT,"Valid card data");
                progressDialog.show();
                StripeUtil.getInstance().generateToken(getApplicationContext(),cardToSave);
                stopSpinner();
            }else if(isCardValid == 2){
                Log.i(StripeConstants.LOGGER_CONSTANT,"Card validation fails, Card details are invalid");
            }

        }
    };

    View.OnClickListener chargeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.doInBackground("amount","currency","description");
        }
    };

    private void chargeUser(){
        try {
            Charge charge = StripeUtil.getInstance().chargeUser(getApplicationContext(), 1, "usd", "test-currency");
            if(charge.getPaid() == Boolean.TRUE){
                Log.e(StripeConstants.LOGGER_CONSTANT,"Payment success");
            }else{
                /**
                 *https://stripe.com/docs/api/metadata
                 * More details about why the payment failed can be found here
                 */
                Log.e(StripeConstants.LOGGER_CONSTANT,"Payment failed");
            }
        }catch (Exception e){
            Log.e(StripeConstants.LOGGER_CONSTANT,e.toString());
        }
    }

    public void stopSpinner(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(StripeUtil.getInstance().isTokenPresent(getApplicationContext())) {
                    chargeUserButton.setVisibility(View.VISIBLE);
                }

            }
        }, 3000);
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {
            chargeUser();
            return "";
        }


        @Override
        protected void onPostExecute(String result) {

        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}
