package com.example.nirmal.stripeapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.stripe.android.model.Card;
import com.stripe.android.view.CardInputWidget;


public class MainActivity extends AppCompatActivity {
    CardInputWidget cardInputWidget;
    Button submitButton,chargeUserButton;
    EditText amountToCharge;
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
        amountToCharge = findViewById(R.id.amount);
        chargeUserButton.setVisibility(View.INVISIBLE);
        amountToCharge.setVisibility(View.INVISIBLE);
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
                StripeUtil.getInstance().generateToken(getApplicationContext(),cardToSave);
                chargeUserButton.setVisibility(View.VISIBLE);
                amountToCharge.setVisibility(View.VISIBLE);
            }else if(isCardValid == 2){
                Log.i(StripeConstants.LOGGER_CONSTANT,"Card validation fails, Card details are invalid");
            }

        }
    };

    View.OnClickListener chargeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            chargeUser();
        }
    };

    private void chargeUser(){
        try {
            StripeUtil.getInstance().chargeUser(getApplicationContext(),Integer.valueOf(amountToCharge.getText().toString()), "usd", "test-currency");
        }catch (Exception e){
            Log.e(StripeConstants.LOGGER_CONSTANT,e.toString());
        }
    }


}
