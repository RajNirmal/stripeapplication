package com.example.nirmal.stripeapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.nirmal.stripeapplication.StripeConstants.AMOUNT;
import static com.example.nirmal.stripeapplication.StripeConstants.CURRENCY;
import static com.example.nirmal.stripeapplication.StripeConstants.ID;
import static com.example.nirmal.stripeapplication.StripeConstants.LOGGER_CONSTANT;
import static com.example.nirmal.stripeapplication.StripeConstants.URL2;

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
                        Toast.makeText(context,"Token generated, perform payment tasks only after this",Toast.LENGTH_LONG).show();
                    }
                    public void onError(Exception error) {
                        Log.i(StripeConstants.LOGGER_CONSTANT,"Error occured while fetching the token");
                    }
                });
    }

    public void chargeUser(final Context context, final int amount, final String currency, String description) throws Exception{
        StringRequest mRequest = new StringRequest(Request.Method.POST, URL2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(StripeConstants.LOGGER_CONSTANT,response.toString());
                try {
                    JSONObject body = new JSONObject(new JSONObject(response).getString("body"));
                    //body will contain data about the amount paid and stuff
                    Log.i(LOGGER_CONSTANT,"\n" +body.toString() );
                    if(new JSONObject(response).getInt("statusCode") == 200) {
                        Toast.makeText(context, "Card has been charged successfully", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context, "Payment failed charged successfully", Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    Log.e(LOGGER_CONSTANT,e.toString());
                }
            }
        }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(LOGGER_CONSTANT,error.toString());
                }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> mParams = new HashMap<>();
                mParams.put(ID,getToken(context));
                mParams.put(AMOUNT, String.valueOf(amount));
                mParams.put(CURRENCY,currency);
                return mParams;
            }
        };
        RequestQueue singleQueue = Volley.newRequestQueue(context);
        singleQueue.add(mRequest);
    }


    private String getToken(Context context){
        SharedPreferences tokenPrefs = context.getSharedPreferences(StripeConstants.SHARED_PREFS_CONSTANT,Context.MODE_PRIVATE);
        return tokenPrefs.getString(StripeConstants.TOKEN,"");
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
