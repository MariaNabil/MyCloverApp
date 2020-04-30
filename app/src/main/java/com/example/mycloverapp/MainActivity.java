package com.example.mycloverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.animation.TypeConverter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Base64DataException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Payment;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;

import maria.testtt.com.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Account mAccount;
    private InventoryConnector mInventoryConnector;
    private TextView mTextView;
    private EditText mAmountEditText;
    private OrderConnector orderConnector;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.main_textView);
        mAmountEditText = (EditText)findViewById(R.id.amountEntry);
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Retrieve the Clover account
        if (mAccount == null) {
            mAccount = CloverAccount.getAccount(this);

            if (mAccount == null) {
                mTextView.setText("maccount = null ");
                Toast.makeText(this, "No Clover Account", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            mTextView.setText("maccount = "+ mAccount.name + "\n");
        }

        // Connect InventoryConnector
        connect();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    // Establishes a connection with the connectors
    private void connect() {
        disconnect();
        if (mAccount != null) {
            orderConnector = new OrderConnector(this, mAccount, null);
            orderConnector.connect();
            mInventoryConnector = new InventoryConnector(this, mAccount, null);
            mInventoryConnector.connect();
        }
    }

    // Disconnects from the connectors
    private void disconnect() {
        if (orderConnector != null) {
            orderConnector.disconnect();
            orderConnector = null;
        }
        if (mInventoryConnector != null) {
            mInventoryConnector.disconnect();
            mInventoryConnector = null;
        }
    }

    public void testBtnClicked(View view) {
        // Get Item
        new InventoryAsyncTask().execute();
    }

    public void getOrderBtnClicked(View view) {
        new MerchantDataAsyncTask().execute();

            /*if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy =
                        new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            try  {

                CloverAuth.AuthResult authResult = CloverAuth.authenticate(MainActivity.this, mAccount);

                //CustomHttpClient httpClient = CustomHttpClient.getHttpClient();
                OkHttpClient client = new OkHttpClient();


// First we get the base URL, merchant ID, and API token
                String baseUrl = authResult.authData.getString(CloverAccount.KEY_BASE_URL);
                String merchantId = authResult.merchantId;
                final String apiToken = authResult.authToken;

// Now we get merchant information using the merchant ID
                String merchantUri = "/v3/merchants/" + merchantId;
                String url = authResult.baseUrl + merchantUri + "?access_token=" + authResult.authToken;
                //result = httpClient.get(url);


                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("accept", "application/json")
                        .build();
                final Response response = client.newCall(request).execute();

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://sandbox.dev.clover.com/v3/YCDDTAQRE1Y11/mId/orders")
                        .get()
                        .addHeader("accept", "application/json")
                        .build();
                final Response response = client.newCall(request).execute();
                mTextView.setText(response.message());
            } catch (Exception e) {
                mTextView.setText(e.getMessage().toString());
            }*/
    }

    public void testPayBtnClicked(View view) {
        try {

            //Thread.sleep(30000);
            startRegisterIntent(false);
            mTextView.append("\ntestPayBtnClicked : Payment Process Done \n\n");
        }catch (Exception ex){
            mTextView.append("\ntestPayBtnClicked : "+ex.getMessage()+"\n\n");
        }
    }


    private void startRegisterIntent(boolean autoLogout) {
        try {
            mTextView.append("\nStartRegisterIntent : Start Payment Process : " );
            Intent intent = new Intent(Intents.ACTION_CLOVER_PAY);
            intent.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, order.getId());
            intent.putExtra(Intents.EXTRA_AMOUNT, "3000");
            intent.putExtra(Intents.EXTRA_OBEY_AUTO_LOGOUT, autoLogout);
            startActivity(intent);
        }catch (Exception ex){
            mTextView.append("\nStartRegisterIntent : " + ex.getMessage());
        }
    }

    public void testSecurePayBtnClicked(View view) {
        try {
            //new OrderAsyncTask().execute();
            //Thread.sleep(30000);
            startSecurePayIntent(false);
            mTextView.append("\ntestSecurePayBtnClicked : Payment Process Done \n\n");
        }catch (Exception ex){
            mTextView.append("\ntestSecurePayBtnClicked : "+ex.getMessage()+"\n\n");
        }
    }
    private void startSecurePayIntent(boolean autoLogout) {
        try {
            mTextView.append("\nstartSecurePayIntent : Start Payment Process : " );
            Intent intent = new Intent(Intents.ACTION_SECURE_PAY);
            intent.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, order.getId());
            intent.putExtra(Intents.EXTRA_OBEY_AUTO_LOGOUT, autoLogout);
            startActivity(intent);
        }catch (Exception ex){
            mTextView.append("\nstartSecurePayIntent : " + ex.getMessage());
        }
    }

    public void finishTripBtnClicked(View view) {
        mTextView.append("\n"+" doInBackground : Start Creating Order " );
        Long amount = Long.parseLong(mAmountEditText.getText().toString());
        new OrderAsyncTask().execute(amount );
    }

    // Creates a new order w/ the first inventory item
    private class OrderAsyncTask extends AsyncTask<Long, Void, Order> {

        @Override
        protected final Order doInBackground(Long... amount) {
            Order mOrder;
            //List<Item> merchantItems;
           // Item mItem;
           // Long amount = (long) 0;
            try {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\n"+" doInBackground : Start Creating Order " );
                        // amount = Long.parseLong(mAmountEditText.getText().toString());

                    }
                });
                // Create a new order
                mOrder = orderConnector.createOrder(new Order());
                // Grab the items from the merchant's inventory

                /*merchantItems = mInventoryConnector.getItems();
                // If there are no item's in the merchant's inventory, then call a toast and return null
                if (merchantItems.isEmpty()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), " doInBackground : Empty Inventory", Toast.LENGTH_SHORT).show();
                            mTextView.append("\n"+" doInBackground : Empty Inventory" );
                        }
                    });
                    finish();
                    return null;
                }
                // Taking the first item from the inventory
                mItem = merchantItems.get(0);

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : Item ID : "+mItem.getName());
                    }
                });

                // Add this item to the order, must add using its PriceType
                if (mItem.getPriceType() == PriceType.FIXED) {
                    orderConnector.addFixedPriceLineItem(mOrder.getId(), mItem.getId(), null, null);
                } else if (mItem.getPriceType() == PriceType.PER_UNIT) {
                    orderConnector.addPerUnitLineItem(mOrder.getId(), mItem.getId(), 1, null, null);
                } else { // The item must be of a VARIABLE PriceType
                    orderConnector.addVariablePriceLineItem(mOrder.getId(), mItem.getId(), 5, null, null);
                }*/
                //orderConnector.add
                LineItem li = new LineItem();
                li.setPrice(amount[0]);
                orderConnector.addCustomLineItem(mOrder.getId(),li,false);

               /* Payment p = new Payment();
                p.setAmount((long)4000);
                orderConnector.addPayment(mOrder.getId(),p,null);*/
                return mOrder;
            } catch (RemoteException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : "+e.getMessage());
                    }
                });
            } catch (ClientException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : "+e.getMessage());
                    }
                });
            } catch (ServiceException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : "+e.getMessage());
                    }
                });
            } catch (BindingException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : "+e.getMessage());
                    }
                });            }catch (Exception ex){
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\ndoInBackground : "+ex.getMessage());
                    }
                });
            }
            return null;
        }

        @Override
        protected final void onPostExecute(Order order1) {
            // Enables the pay buttons if the order is valid
            try {
                if (!isFinishing()) {
                    MainActivity.this.order = order1;
                    if (order != null) {
                        mTextView.append("\nonPostExecute : OrderID : " + order1.getId() + "\nCreatedTime : " + order1.getClientCreatedTime().toString()+"\n");
                    }
                }
                else {
                    mTextView.append("\nonPostExecute : "+ "Order is finished"+"\n");
                }

            }catch (Exception ex){
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTextView.append("\nonPostExecute : "+ex.getMessage()+"\n");
                    }
                });
            }
        }
    }
    private class InventoryAsyncTask extends AsyncTask<Void, Void, List<Item>> {

        @Override
        protected final List<Item> doInBackground(Void... params) {
            try {
                //Get inventory item
                return mInventoryConnector.getItems();

            } catch (RemoteException | ClientException | ServiceException | BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected final void onPostExecute(List<Item> items) {
            if (items != null) {
                String text = "items count = " +items.size()+"\n";
                for (int i= 0 ; i<items.size(); i++){
                    text+= items.get(i).getName() + "\n";
                }
                mTextView.setText(text);
            }
        }
    }


    private class MerchantDataAsyncTask extends AsyncTask<Void, Void, Response> {

        @Override
        protected final Response doInBackground(Void... params) {
            try {
                CloverAuth.AuthResult authResult = CloverAuth.authenticate(MainActivity.this, mAccount);
                //CustomHttpClient httpClient = CustomHttpClient.getHttpClient();
                OkHttpClient client = new OkHttpClient();

// First we get the base URL, merchant ID, and API token
                String baseUrl = authResult.authData.getString(CloverAccount.KEY_BASE_URL);
                String merchantId = authResult.merchantId;
                final String apiToken = authResult.authToken;

// Now we get merchant information using the merchant ID
                String merchantUri = "/v3/merchants/" + merchantId;
                String url = authResult.baseUrl + merchantUri + "?access_token=" + authResult.authToken;
                //result = httpClient.get(url);


                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("accept", "application/json")
                        .build();
                final Response response = client.newCall(request).execute();

                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected final void onPostExecute(Response response) {
            String text = "";
            try {
                if (response != null) {
                    text = "response Message : " + "\n";
                    text += response.message() + "\n\n" + response.body().toString();
                }
            }
            catch (Exception ex) {
                String msg = ex.getMessage();
                text += "\n"+ msg;
            }
            mTextView.setText(text);
            }
        }


    public void btnClicked(View view) {
        String mod = "24130287975021244042702223805688721202041521798556826651085672609155097623636349771918006235309701436638877260677191655500886975872679820355397440672922966114867081224266610192869324297514124544273216940817802300465149818663693299511097403105193694390420041695022375597863889602539348837984499566822859405785094021038882988619692110445776031330831112388738257159574572412058904373392173474311363017975036752132291687352767767085957596076340458420658040735725435536120045045686926201660857778184633632435163609220055250478625974096455522280609375267155256216043291335838965519403970406995613301546002859220118001163241";
        PublicKey pk = getPublicKey(new BigInteger(mod),
                 new BigInteger("415029"));
       String encryptedCard =  encryptPAN("00008099","4005578003333335", pk);

    }

    public static PublicKey getPublicKey(final BigInteger modulus, final BigInteger exponent) {
        try {
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            final PublicKey publicKey = factory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
            return publicKey;
        } catch (GeneralSecurityException e) {
            String msg = e.getMessage();
            return null;
        }
    }
    public static String encryptPAN(final String prefix, final String pan, PublicKey publicKey) {
        byte[] input = String.format("%s%s", prefix, pan).getBytes();
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherText = cipher.doFinal(input);
             return Base64.encodeToString(cipherText, Base64.DEFAULT);
        } catch (GeneralSecurityException ignore) {
            return null;
        }
    }
}
