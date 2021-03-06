package jolt151.ettercapforandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, RewardedVideoAdListener,
                                                                BillingProcessor.IBillingHandler{

    EditText editTextArgs;
    TextView textView1;
    ExecuteTask executeTask;
    EditText editTextInterface;
    EditText editTextTargets;
    EditText editTextOutput;
    CheckBox checkBox1;
    Button buttonQuit;
    Button buttonCustom;
    Button button1;
    EditText editTextCustom;

    AdView mAdview;
    InterstitialAd interstitialAd;
    RewardedVideoAd mRewardedVideoAd;
    boolean isRewarded;
    BillingProcessor billingProcessor;
    LinearLayout lp;

    String LOGTAG = "EttercapForAndroid";


    public DataOutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billingProcessor = new BillingProcessor(this, getString(R.string.license_key), this);



        showDialog(0);

        File file = new File("/data/data/" + getPackageName() + "/files/EttercapForAndroid-master/bin/ettercap");

        if (!file.exists()) {
            showDialog(2);
        }


        file.setExecutable(true);

        textView1 = findViewById(R.id.textView1);
        if (!billingProcessor.isPurchased("fullversion")){
            //prepare banner ad
            mAdview = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("002B541CB3F67C0FB7DDA97E41BDE7D2").build();
            mAdview.loadAd(adRequest);

            //Prepare interstitial ad
            interstitialAd = new InterstitialAd(MainActivity.this);
            interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            interstitialAd.loadAd(new AdRequest.Builder().addTestDevice("002B541CB3F67C0FB7DDA97E41BDE7D2").build());

            //prepare rewarded ad
            MobileAds.initialize(this);
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            mRewardedVideoAd.setRewardedVideoAdListener(this);

            mRewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id), new AdRequest.Builder().addTestDevice("002B541CB3F67C0FB7DDA97E41BDE7D2").build());
        } else{
            lp = new LinearLayout(this);
            lp.removeView(mAdview);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0,0,0,0);
            textView1.setLayoutParams(params);
        }


        button1 = findViewById(R.id.button1);
        buttonQuit = findViewById(R.id.buttonQuit);
        buttonCustom = findViewById(R.id.buttonCustom);
        editTextCustom = findViewById(R.id.editTextCustom);
        checkBox1 = findViewById(R.id.checkBox1);
        editTextArgs = findViewById(R.id.editTextArgs);
        editTextInterface = findViewById(R.id.editTextInterface);
        editTextTargets = findViewById(R.id.editTextTargets);
        editTextOutput = findViewById(R.id.editTextOutput);
        textView1.setMovementMethod(new ScrollingMovementMethod());

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        editTextOutput.setText(timeStamp + ".pcap");

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultArgs = sharedPrefs.getString("default_args", null);
        String defaultInterface = sharedPrefs.getString("default_interface", null);
        String defaultTargets = sharedPrefs.getString("default_targets", null);

        //@TODO THIS STOPS WORKING AFTER A BIT DUE TO GARBAGE COLLECTION (stack overflow)

        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(LOGTAG, "key changed: " + key);
                if (key.equals("default_args")){
                    editTextArgs.setText(sharedPreferences.getString(key, null));
                }
                else if (key.equals("default_interface")){
                    editTextInterface.setText(sharedPreferences.getString(key, null));
                }
                else if (key.equals("default_targets")){
                    editTextTargets.setText(sharedPreferences.getString(key, null));
                }
            }
        };
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        editTextArgs.setText(defaultArgs);
        editTextInterface.setText(defaultInterface);
        editTextTargets.setText(defaultTargets);

        checkBox1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (!billingProcessor.isPurchased("fullversion") && !isRewarded) {
                    checkBox1.setChecked(false);
                    showDialog(5);
                } else {
                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {

                    } else {
                        EasyPermissions.requestPermissions(MainActivity.this, "We need to be able to write to external storage to output the capture file.", 1, perms);
                    }
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                File output = new File(Constants.OUTPUT_DIR + editTextOutput.getText().toString());
                Log.d(LOGTAG, output.getName());
                if (output.exists()){
                    showDialog(4);
                } else if (chkStatus().equals("wifi")) {
                    if (!editTextArgs.getText().toString().equals("")) {
                        buttonQuit.setEnabled(true);
                        Log.d(LOGTAG, editTextArgs.getText().toString());

                        File root = new File(Environment.getExternalStorageDirectory(), "EttercapForAndroid");

                        if (!root.exists()) {
                            root.mkdirs();
                        }

                        StringBuilder builder1 = new StringBuilder("");
                        if (checkBox1.isChecked()) {
                            builder1.append("cd " + Constants.FILES_DIR + "EttercapForAndroid-master/bin/" + " && su &&" + Constants.CHMOD + " && " +
                                    Constants.FILES_DIR + "EttercapForAndroid-master/bin/ettercap "
                                    + "-i " + editTextInterface.getText().toString() + " "
                                    + editTextArgs.getText().toString() + " "
                                    + "-w " + Environment.getExternalStorageDirectory() + "/EttercapForAndroid/" + editTextOutput.getText().toString() + " "
                                    + editTextTargets.getText().toString()
                            );

                            Toast.makeText(getApplicationContext(), "Saving to /sdcard/EttercapForAndroid/" + editTextOutput.getText().toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            builder1.append("cd " + Constants.FILES_DIR + "EttercapForAndroid-master/bin/" + " && pwd && su &&" + Constants.CHMOD + " && " +
                                    Constants.FILES_DIR + "EttercapForAndroid-master/bin/ettercap "
                                    + "-i " + editTextInterface.getText().toString() + " "
                                    + editTextArgs.getText().toString() + " "
                                    + editTextTargets.getText().toString()
                            );
                        }
                        String cmdline = builder1.toString();
                        executeTask = new ExecuteTask(getApplicationContext());
                        executeTask.execute(cmdline);

                        button1.setEnabled(false);

                        editTextArgs.setEnabled(false);
                        editTextInterface.setEnabled(false);
                        editTextOutput.setEnabled(false);
                        editTextTargets.setEnabled(false);
                        checkBox1.setEnabled(false);

                    } else {
                        Toast.makeText(getApplicationContext(), "No args!", Toast.LENGTH_SHORT).show();
                    }
                } else if (!chkStatus().equals("wifi")){
                    Toast.makeText(getApplicationContext(), "Error: you must be connected to wifi to use Ettercap", Toast.LENGTH_SHORT).show();
                }

            }
        });
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                try {
                    outputStream.writeBytes("q");
                    outputStream.flush();
                    Log.d(LOGTAG, "Quit");
                } catch (Throwable e) {
                }

                Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if (msg.what == 1){
                            executeTask.cancelScan();
                        }
                    }
                };
                showDialog(3);
                //show interstitial ad after quitting
                if (!billingProcessor.isPurchased("fullversion")){
            /*        interstitialAd.loadAd(new AdRequest.Builder().build());
                    interstitialAd.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();

                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            super.onAdFailedToLoad(errorCode);

                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (interstitialAd.isLoaded()) {
                                interstitialAd.show();
                            }
                        }
                    });*/
                    if (interstitialAd.isLoaded()){
                        interstitialAd.show();
                    }
                }
                handler.sendEmptyMessageDelayed(1, 7000);



            }
        });
        buttonCustom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                try {
                    outputStream.writeBytes(editTextCustom.getText().toString());
                    outputStream.flush();

                } catch (Throwable e) {
                }
            }
        });
    }

    private class ExecuteTask extends AsyncTask<String, String, String> {
        final Context context;
        PowerManager.WakeLock mWakeLock;
        java.lang.Process scanProcess;
        String shellToRun = "sh";

        @Override
        protected String doInBackground(String... sParm) {
            String cmdline = sParm[0];
            String pstdout = null;
            StringBuilder wholeoutput = new StringBuilder("");
            String[] commands = { /*"cd /data/data/" + getPackageName() +"/files", "pwd","su",*/"su", cmdline};

            //DataOutputStream outputStream;
            BufferedReader inputStream;

//            Process scanProcess;

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(shellToRun);
                processBuilder.redirectErrorStream(true);
                scanProcess = processBuilder.start();

                outputStream = new DataOutputStream(scanProcess.getOutputStream());
                inputStream = new BufferedReader(new InputStreamReader(scanProcess.getInputStream()));

                for (String single : commands) {
                    Log.i(LOGTAG, "Single Executing: " + single);
                    outputStream.writeBytes(single + "\n");
                    outputStream.flush();
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                while (((pstdout = inputStream.readLine()) != null)) {
                    if (isCancelled()) {
                        showDialog(3);
                        scanProcess.destroy();
                        break;
                    } else {
                        if (pstdout != null) {
                            pstdout = pstdout + "\n";
                            wholeoutput.append(pstdout);
                        }
                        Log.i(LOGTAG, "Stdout: " + pstdout);
                        publishProgress(pstdout, null);
                        pstdout = null;
                    }
                }


                if (!isCancelled()) scanProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            return wholeoutput.toString();
        }

        @SuppressLint("WakelockTimeout")
        @Override
        protected void onPreExecute() {
            // super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            //Toast.makeText(context,"Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            // super.onProgressUpdate(progress);
            if (progress[0] != null) textView1.append(progress[0]);
            if (progress[1] != null) textView1.append(progress[1]);
            //scrollToBottom();
        }

        public ExecuteTask(Context context) {
            this.context = context;
        }

        protected void cleanupOnEnd() {

            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            setSupportProgressBarIndeterminateVisibility(false);
            recreate();
        }

        @Override
        protected void onCancelled() {
            cleanupOnEnd();
        }

        @Override
        protected void onPostExecute(String result) {
            cleanupOnEnd();
        }

        protected void cancelScan() {
            executeTask.cancel(true);
            onCancelled();
            Log.d(LOGTAG, "Canceled: " + String.valueOf(executeTask.isCancelled()));
            Thread cancelThread = new Thread() {
                @Override
                public void run() {
                    try {
                        String killstr = new String("/system/bin/kill -9 " + ProcessUtil.getppid(ProcessUtil.getpid(scanProcess), shellToRun));
                        Log.i(LOGTAG, "Executing kill: " + killstr);
                        Runtime.getRuntime().exec(killstr);
                    } catch (IOException e) {
                        Log.e(LOGTAG, "Error killing process");
                    }
                }
            };
            cancelThread.run();
        }

    }

    protected Dialog onCreateDialog(int id) {
        if (id == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("LEGAL DISCLAIMER: I AM NOT RESPONSIBLE FOR ANYTHING THAT MAY HAPPEN BECAUSE OF THIS APP. BY USING THIS APP, YOU AFFIRM THAT YOU HAVE" +
                    " PERMISSION TO USE ETTERCAP AND SIMILAR TOOLS ON THE NETWORK.\n" +
                    "This app is in alpha. Ettercap is a big program. MOST of the features are untested, aside from the main ones. Expect bugs.")
                    .setCancelable(false)
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    });
            return builder.create();
        } else if (id == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("To use this tool, put in the proper arguments for ettercap, such as \"-Tq -M" +
                    " arp:remote\". The interface in most cases will be wlan0. Set the targets like this: // // targets the whole network, while " +
                    "/192.168.1.101/ // will target that specific IP. Refer to the ettercap manual for more info." +
                    "\n\nIf you have enabled outputting to file, the file will be saved to the 'Ettercap for Android' folder on the SD card. " +
                    "\n\nOnce ettercap is running, you can use the lower run command to input certain keystrokes. For example, 'h' will show the available options." +
                    "\n\nTo safely quit ettercap, use the quit button so the program can safely re-ARP the targets. Force closing the app can have unintended side effects." +
                    "\n\nThis app is still in alpha, and many features are untested. I can only confirm that the main arguments work, which is enough for most people." +
                    "\n\nI'd love to hear your feedback! Drop me a message at michaellevi151@gmail.com, at least until I find a better way of collecting feedback.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNeutralButton("Ettercap Manual", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://linux.die.net/man/8/ettercap"));
                            startActivity(i);
                        }
                    });
            return builder.create();
        } else if (id == 2) {
            Log.d(LOGTAG, Log.getStackTraceString(new Exception()));
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            String message;
            File file = new File("/data/data/" + getPackageName() + "/files/EttercapForAndroid-master/bin/ettercap");
            if (file.exists()){
                message = "Would you like to redownload the ettercap binaries?";
            } else {
                message = "Ettercap binaries are not installed. Download now?";
            }
            builder.setMessage(message)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new DownloadFile().execute("https://github.com/Jolt151/EttercapForAndroid/archive/master.zip");
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            return builder.create();
        } else if (id == 3){
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Cancelling and giving time to re-ARP victims, this can take a while...");
            dialog.setIndeterminate(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
            return null;
        } else if(id == 4){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("There is already a file with that name! Pick a different file name to continue, or press the refresh button to automatically change the name.")
                    .setPositiveButton("Refresh name", new DialogInterface.OnClickListener() {
                        @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editTextOutput.setText(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".pcap");
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            return builder.create();
        } else if (id == 5){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Upgrade to the full version to remove ads and gain the ability to output captured packets to a file and change default settings!\n" +
                    "Alternatively, you can watch a short video to use this feature temporarily.")
                    .setNeutralButton("Watch Video", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mRewardedVideoAd.isLoaded()){
                                mRewardedVideoAd.show();
                            }
                        }
                    })
                    .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!BillingProcessor.isIabServiceAvailable(getApplicationContext())){
                                Toast.makeText(getApplicationContext(), "Purchasing has been disabled for your device", Toast.LENGTH_SHORT).show();
                                Log.d(LOGTAG, "purchasing disabled");
                            } else{
                                billingProcessor.purchase(MainActivity.this, "fullversion");
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
        else {
            return null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        if (billingProcessor.isPurchased("fullversion")){
            menu.findItem(R.id.upgrade).setVisible(false);
        }
        return true;
    }

    //Don't forget to return true or break after cases
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.disclaimer:
                showDialog(0);
                return true;
            case R.id.help:
                showDialog(1);
                return true;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.download:
                showDialog(2);
                return true;
            case R.id.upgrade:
                if (!billingProcessor.isPurchased("fullversion")){
                    showDialog(5);
                } else{
                    Toast.makeText(this, "Already purchased!", Toast.LENGTH_SHORT);
                }
                return true;
            case R.id.privacy:
                String url = "https://ettercapforandroid.firebaseapp.com/privacy.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Async Task to download file from URL
     */
    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(MainActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                //fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());
                fileName = "EttercapForAndroid.zip";

                //Append timestamp to file name
                //fileName = timestamp + "_" + fileName;

                //External directory path to save file
                folder = Constants.FILES_DIR;

                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(LOGTAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();

            Util.unpackZip(getApplicationInfo().dataDir + "/files/","EttercapForAndroid.zip");

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(LOGTAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        checkBox1.setChecked(false);

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    String chkStatus() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting ()) {
            //Toast.makeText(this, "Wifi", Toast.LENGTH_LONG).show();
            return "wifi";
        } else if (mobile.isConnectedOrConnecting ()) {
            //Toast.makeText(this, "Mobile 3G ", Toast.LENGTH_LONG).show();
            return "mobile";
        } else {
           // Toast.makeText(this, "No Network ", Toast.LENGTH_LONG).show();
            return "nonetwork";
        }
    }

    //IBillingHandler Implementation
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
        if (productId.equals("fullversion")){
            lp = new LinearLayout(this);
            lp.removeView(mAdview);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0,0,0,0);
            textView1.setLayoutParams(params);
        }
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

    @Override
    public void onRewarded(RewardItem reward) {
        Log.d(LOGTAG, "onrewarded: " + reward.getType() + " " + reward.getAmount());

        // Reward the user.
        isRewarded = true;


    }
    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Log.d(LOGTAG, "onRewardedVideoAdFailedToLoad");
        mRewardedVideoAd.destroy(this);

    }
    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d(LOGTAG, "onrewardedvideoadloaded");
    }
    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(LOGTAG, "onRewardedVideoAdOpened");

    }
    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(LOGTAG, "onRewardedVideoAdClosed");
        if (isRewarded){
            checkBox1.setChecked(true);
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {

            } else {
                EasyPermissions.requestPermissions(MainActivity.this, "We need to be able to write to external storage to output the capture file.", 1, perms);
            }
        }
        mRewardedVideoAd.destroy(this);
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id), new AdRequest.Builder().addTestDevice("002B541CB3F67C0FB7DDA97E41BDE7D2").build());

    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d(LOGTAG, "onRewardedVideoStarted");

    }
    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(LOGTAG, "onRewardedVideoAdLeftApplication");
        mRewardedVideoAd.pause(this);

    }

}

