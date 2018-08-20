package jolt151.ettercapforandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
                                                                    BillingProcessor.IBillingHandler {

    BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        billingProcessor = new BillingProcessor(this, getString(R.string.license_key), this);


        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_args));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_interface));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_targets));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (!billingProcessor.isPurchased("fullversion")){
            findPreference("default_args").setEnabled(false);
            findPreference("default_interface").setEnabled(false);
            findPreference("default_targets").setEnabled(false);

            showDialog(0);
        }


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 0){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SettingsActivity.this);
            builder.setMessage("Upgrade to the full version to remove ads and gain the ability to output captured packets to a file and change default settings!")
                    .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!BillingProcessor.isIabServiceAvailable(getApplicationContext())){
                                Toast.makeText(getApplicationContext(), "Purchasing has been disabled for your device", Toast.LENGTH_SHORT);
                            } else{
                                billingProcessor.purchase(SettingsActivity.this, "fullversion");
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            return builder.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(sharedPreferences.getString(key,null));
        }
    }

    //BillingHandler Implementation
    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}