package com.example.nirmal.stripeapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

public class StripeUtil {

    public static StripeUtil stripeUtil = new StripeUtil();
    SharedPreferences tokenPrefs;

    public static StripeUtil getInstance(){
        if(stripeUtil == null){
            stripeUtil = new StripeUtil();
        }
        return stripeUtil;
    }

    public int isCardValid(Card cardToValidate){
        int returnValue = 0;
        if (cardToValidate == null) {
            returnValue = 0;
        }else{
            if(cardToValidate.validateCard()){
                returnValue = 1;
            }else{
                returnValue = 2;
            }
        }
        return returnValue;
    }

    public void generateToken(final Context context, Card card){
        Log.i(StripeConstants.LOGGER_CONSTANT,"Got the generate token callback");
        Stripe stripe = new Stripe(context, StripeConstants.PUBLISHABLE_KEY);
        stripe.createToken(card, new TokenCallback() {
                    public void onSuccess(Token token) {
                        Log.i(StripeConstants.LOGGER_CONSTANT,"Token generated "+ token.getId());
                        updateTokenStatus(context,token.getId(),1);
                    }
                    public void onError(Exception error) {
                        Log.i(StripeConstants.LOGGER_CONSTANT,"Error occured while fetching the token");
                    }
                });
    }

    public Charge chargeUser(Context context,int amount, String currency, String description) throws Exception{
        com.stripe.Stripe.apiKey = StripeConstants.TEST_SECRET_KEY;
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("description", description);
        params.put("source", getToken(context));
        Charge charge = Charge.create(params);
        return  charge;
    }

    private String getToken(Context context) throws Exception{
        if(isTokenPresent(context)) {
            SharedPreferences tokenPrefs = context.getSharedPreferences(StripeConstants.SHARED_PREFS_CONSTANT, Context.MODE_PRIVATE);
            return tokenPrefs.getString(StripeConstants.TOKEN,"");
        }else {
            Exception e = new Exception("The card details are not present");
            throw e;
        }

    }
    private void updateTokenStatus(Context context,String tokenId, Integer status){
        SharedPreferences tokenPrefs = context.getSharedPreferences(StripeConstants.SHARED_PREFS_CONSTANT,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  tokenPrefs.edit();
        if(status == 0) {
            editor.putBoolean(StripeConstants.IS_TOKEN_PRESENT,false);
        }else{
            editor.putBoolean(StripeConstants.IS_TOKEN_PRESENT,true);
            editor.putString(StripeConstants.TOKEN,tokenId);
        }
        editor.commit();
    }

    public boolean isTokenPresent(Context context){
        SharedPreferences tokenPrefs = context.getSharedPreferences(StripeConstants.SHARED_PREFS_CONSTANT,Context.MODE_PRIVATE);
        return tokenPrefs.getBoolean(StripeConstants.IS_TOKEN_PRESENT,false);
    }




}
