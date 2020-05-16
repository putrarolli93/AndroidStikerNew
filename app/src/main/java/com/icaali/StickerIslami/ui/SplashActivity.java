package com.icaali.StickerIslami.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.vending.billing.IInAppBillingService;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.icaali.StickerIslami.Manager.AdManager;
import com.icaali.StickerIslami.Manager.PrefManager;
import com.icaali.StickerIslami.R;
import com.icaali.StickerIslami.api.apiClient;
import com.icaali.StickerIslami.api.apiRest;
import com.icaali.StickerIslami.config.Config;
import com.icaali.StickerIslami.entity.ApiResponse;
import com.icaali.StickerIslami.entity.ApiValue;
import com.icaali.StickerIslami.entity.CategoryApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashActivity extends AppCompatActivity {


    private PrefManager prf;


    IInAppBillingService mService;


    private static final String LOG_TAG = "iabv3";

    // put your Google merchant id here (as stated in public profile of your Payments Merchant Center)
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID=null;

    private BillingProcessor bp;
    private boolean readyToPurchase = false;



    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initBuy();
        loadLang();
        setContentView(R.layout.activity_splash);
        prf= new PrefManager(getApplicationContext());

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                       checkUser();

                    }
                });
            }
        }, 1000);
    }

    private void checkUser() {
        if (prf.getString("LOGGED").equals("TRUE")){
            Integer user_id =  Integer.parseInt(prf.getString("ID_USER"));
            String user_key = prf.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.checkUser(user_id,user_key);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body().getCode() == 500){
                            logout();
                        }
                    }
                    checkAccount();
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    checkAccount();
                }
            });
        }else{
            checkAccount();
        }
    }
    public void logout() {
        prf.remove("ID_USER");
        prf.remove("SALT_USER");
        prf.remove("TOKEN_USER");
        prf.remove("NAME_USER");
        prf.remove("TYPE_USER");
        prf.remove("USERN_USER");
        prf.remove("IMAGE_USER");
        prf.remove("LOGGED");
        Toasty.warning(getApplicationContext(),getString(R.string.message_logout),Toast.LENGTH_LONG).show();
    }
    private void checkAccount() {

        int version = -1;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.check(version);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    updateTextViews();

                    if (response.isSuccessful()){

                        for (int i = 0; i < response.body().getValues().size(); i++) {

                            ApiValue data = response.body().getValues().get(i);
                            
                            if (data.getName().equals("ADMIN_APP_ID") ){
                                AdManager.ADMOB_APP_ID = data.getValue();
                            }
                            else if (data.getName().equals("ADMIN_PUBLISHER_ID") ){
                                AdManager.PUBLISHER_ID = data.getValue();
                            }
                            else if (data.getName().equals("ADMIN_REWARDED_ADMOB_ID") ){
                                AdManager.ADMOB_REWARD_ID = data.getValue();
                            }
                            else if (data.getName().equals("ADMIN_INTERSTITIAL_ADMOB_ID") ) {
                                AdManager.ADMOB_INTERSTITIAL_ID = data.getValue();
                            }
                            else if (data.getName().equals("ADMIN_INTERSTITIAL_CLICKS") ) {
                                AdManager.ADMOB_INTERSTITIAL_SHOW_CLICKS = Integer.parseInt(response.body().getValues().get(i).getValue());
                            }
                            else if (data.getName().equals("ADMIN_BANNER_ADMOB_ID") ){
                                AdManager.ADMOB_BANNER_ID = data.getValue();
                            }

                            else if (data.getName().equals("ADMIN_NATIVE_ADMOB_ID") ){
                                AdManager.ADMOB_NATIVE_ID = data.getValue();
                            }
                            else if (data.getName().equals("ADMIN_NATIVE_LINES") ){
                                AdManager.ADMOB_NATIVE_LINES = Integer.parseInt(response.body().getValues().get(i).getValue());
                            }
                            else if (data.getName().equals("ADMIN_NATIVE_TYPE") ){
                                AdManager.ADMOB_NATIVE_ENABLED =data.getValue();
                            }
                        }

                        if (response.body().getCode().equals(200)) {
                            if (!prf.getString("first").equals("true")){
                                Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                                prf.setString("first","true");
                            }else{
                                Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                            }
                        }
                        else {
                            if (!prf.getString("first").equals("true")){
                                Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                                prf.setString("first","true");
                            }else{
                                Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                            }
                        }
                    }else {
                        if (!prf.getString("first").equals("true")){
                            Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                            prf.setString("first","true");
                        }else{
                            Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                        }
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                    if (!prf.getString("first").equals("true")){
                        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        finish();
                        prf.setString("first","true");
                    }else{
                        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                        finish();
                    }

                }
            });


    }

    private void initBuy() {
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);


        if(!BillingProcessor.isIabServiceAvailable(this)) {
            //  showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        bp = new BillingProcessor(this, Config.LICENSE_KEY, MERCHANT_ID, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                updateTextViews();
            }
            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
            }
            @Override
            public void onBillingInitialized() {
                readyToPurchase = true;
                updateTextViews();
            }
            @Override
            public void onPurchaseHistoryRestored() {
                for(String sku : bp.listOwnedProducts())
                    Log.d(LOG_TAG, "Owned Managed Product: " + sku);
                for(String sku : bp.listOwnedSubscriptions())
                    Log.d(LOG_TAG, "Owned Subscription: " + sku);
                updateTextViews();
            }
        });
        bp.loadOwnedPurchasesFromGoogle();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);
    }

    private void updateTextViews() {
        bp.loadOwnedPurchasesFromGoogle();
        if(isSubscribe(Config.SUBSCRIPTION_ID)){
            prf.setString("SUBSCRIBED","TRUE");
            // showToast("SUBSCRIBED");

        }
        else{
            prf.setString("SUBSCRIBED","FALSE");
            // showToast("NOT SUBSCRIBED");
        }
    }

    private void showToast(String message) {
        //  Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public Bundle getPurchases(){
        if (!bp.isInitialized()) {
            return null;
        }
        try{

            return  mService.getPurchases(Constants.GOOGLE_API_VERSION, getApplicationContext().getPackageName(), Constants.PRODUCT_TYPE_SUBSCRIPTION, null);
        }catch (Exception e) {
            // Toast.makeText(this, "ex", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
        return null;
    }


    public Boolean isSubscribe(String SUBSCRIPTION_ID_CHECK){

        if (!bp.isSubscribed(Config.SUBSCRIPTION_ID))
            return false;


        Bundle b =  getPurchases();
        if (b==null)
            return  false;
        if( b.getInt("RESPONSE_CODE") == 0){
            ArrayList<String> ownedSkus =
                    b.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            ArrayList<String>  purchaseDataList =
                    b.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            ArrayList<String>  signatureList =
                    b.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
            String continuationToken =
                    b.getString("INAPP_CONTINUATION_TOKEN");


            if(purchaseDataList == null){
                return  false;

            }
            if(purchaseDataList.size()==0){
                return  false;
            }
            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku_1 = ownedSkus.get(i);
                //Long tsLong = System.currentTimeMillis()/1000;

                try {
                    JSONObject rowOne = new JSONObject(purchaseData);
                    String  productId =  rowOne.getString("productId") ;

                    if (productId.equals(SUBSCRIPTION_ID_CHECK)){

                        Boolean  autoRenewing =  rowOne.getBoolean("autoRenewing");
                        if (autoRenewing){
                            Long tsLong = System.currentTimeMillis()/1000;
                            Long  purchaseTime =  rowOne.getLong("purchaseTime")/1000;
                            return  true;
                        }else{
                            // Toast.makeText(this, "is not autoRenewing ", Toast.LENGTH_SHORT).show();
                            Long tsLong = System.currentTimeMillis()/1000;
                            Long  purchaseTime =  rowOne.getLong("purchaseTime")/1000;
                            if (tsLong > (purchaseTime + (Config.SUBSCRIPTION_DURATION*86400)) ){
                                //   Toast.makeText(this, "is Expired ", Toast.LENGTH_SHORT).show();
                                return  false;
                            }else{
                                return  true;
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }else{
            return false;
        }

        return  false;
    }

    public void loadLang(){
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<CategoryApi>> call = service.AllCategories();
        call.enqueue(new Callback<List<CategoryApi>>() {
            @Override
            public void onResponse(Call<List<CategoryApi>> call, Response<List<CategoryApi>> response) {

            }

            @Override
            public void onFailure(Call<List<CategoryApi>> call, Throwable t) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }
}
