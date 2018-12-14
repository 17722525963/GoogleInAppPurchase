package com.zsrun.googleinapppurchase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zsrun.googleinapppurchase.util.IabHelper;
import com.zsrun.googleinapppurchase.util.IabResult;
import com.zsrun.googleinapppurchase.util.Purchase;

import static com.zsrun.googleinapppurchase.util.IabHelper.RESPONSE_INAPP_PURCHASE_DATA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "???";

    private IabHelper mIabHelper;

    private String base64EncodedPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        purchase();

        handexception();
    }

    private void handexception() {
        findViewById(R.id.in_purchase_exception).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AnotherActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkKvIIE8bBrn2QOPaAWrLk988avxgy2yscf/uY/vDYEZ5N5Bzj6lCyQha5CZg3sTZ8ho5a99i0k+f5IkkNlhorYnrLk3H9xQE1fBROdxhfFzxlzv2qW5I7sicsQcER7Ql6Bnm6kYtQPP6lgl012pM5XTJziEcTY6v8rd5UFunRV72jmXXAywiJ4tTpimIN2OA63SxcKP7aJuYVNGfs3Fq/X5/9vfTfm7EOf4dZFXOjsE4IXovz8zse1PeGD+K05s+vVfyXjfFFAWqeG10H9uDtPW4NSzdPdQMl0cyLoNje1vtbaHT3W4xezDEt2sj517WLwF5d3IZGjn1Isl6Lda6kwIDAQAB";
        mIabHelper = new IabHelper(this, base64EncodedPublicKey);

        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    Log.i(TAG, "onIabSetupFinished: 初始化成功" + result.toString());
                    Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "" + result.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if (mIabHelper == null) {
                    Log.i(TAG, "onIabSetupFinished: " + mIabHelper);
                }
            }
        });
    }

    private void purchase() {
        final String produvtID = "com.zsrun.latiao";

        findViewById(R.id.in_purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mIabHelper.launchPurchaseFlow(MainActivity.this, produvtID, 111, new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            Log.i(TAG, "onIabPurchaseFinished: " + result);
                            if (result.isSuccess()) {
                                Toast.makeText(MainActivity.this, "您购买的辣条已成功，正在进行消耗，消耗不成功不能再次购买~", Toast.LENGTH_SHORT).show();
                                consumeByPurchase(info);
                            }
                            if (result.getResponse() == 7) {
                                Log.i(TAG, "onIabPurchaseFinished: 已购买，未进行消耗，无法再次购买~" + Thread.currentThread().getName());
                                Toast.makeText(MainActivity.this, "已购买，未进行消耗，无法再次购买~", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, produvtID);//透传参数（传什么，google返回什么）
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    /**
     * 消耗商品
     *
     * @param purchase 商品
     */
    private void consumeByPurchase(Purchase purchase) {
        if (purchase != null) {
            try {
                mIabHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        Log.i(TAG, "onConsumeFinished: " + result);
                        Log.i(TAG, "onConsumeFinished: " + purchase.toString());
                        Toast.makeText(MainActivity.this, purchase.getSku() + "消耗成功", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                Toast.makeText(MainActivity.this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未购买当前商品不能消耗", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIabHelper != null) {
            try {
                mIabHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mIabHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mIabHelper.handleActivityResult(requestCode, resultCode, data);
        String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);


        Log.i(TAG, "onActivityResult: " + purchaseData);

        Log.i(TAG, "account: " + data.getStringExtra("com.google.android.finsky.analytics.LoggingContext.KEY_ACCOUNT"));

        for (String key : data.getExtras().keySet()) {
            Log.i(TAG, "onActivityResult: " + key);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
