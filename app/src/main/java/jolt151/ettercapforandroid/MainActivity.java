package jolt151.ettercapforandroid;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView textView1;
    ExecuteTask executeTask;
    EditText editTextInterface;
    EditText editTextTargets;
    EditText editTextOutput;
    CheckBox checkBox1;
    Button buttonQuit;
    Button buttonCustom;
    Button button1;
    Button buttonKill;
    EditText editTextCustom;


    public DataOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showDialog(0);


        //copyAssetFolder(assetManager,"file://android_asset", toPath);
        copyFromAssetsToInternalStorage("EttercapForAndroid.zip");
        unpackZip(getApplicationInfo().dataDir + "/files/","EttercapForAndroid.zip");
//        unZipFile("EttercapForAndroid.zip");

        File file = new File("/data/data/" + getPackageName() + "/files/bin/ettercap");
        file.setExecutable(true);


        button1 = (Button) findViewById(R.id.button1);
        buttonQuit = (Button) findViewById(R.id.buttonQuit);
        buttonCustom = (Button) findViewById(R.id.buttonCustom);
        buttonKill = (Button) findViewById(R.id.buttonKill);
        editTextCustom = (EditText) findViewById(R.id.editTextCustom);
        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        editText = (EditText) findViewById(R.id.editText);
        textView1 = (TextView) findViewById(R.id.textView1);
        editTextInterface = (EditText) findViewById(R.id.editTextInterface);
        editTextTargets = (EditText) findViewById(R.id.editTextTargets);
        editTextOutput = (EditText) findViewById(R.id.editTextOutput);
        textView1.setMovementMethod(new ScrollingMovementMethod());
        textView1.setText("output");
        //@TODO see later: evaluate if we can run multiple times without this button.
        buttonKill.setEnabled(false);
        buttonQuit.setEnabled(false);

        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultArgs = sharedPrefs.getString("default_args",null);
        String defaultInterface = sharedPrefs.getString("default_interface", null);
        String defaultTargets = sharedPrefs.getString("default_targets", null);

        editText.setText(defaultArgs);
        editTextInterface.setText(defaultInterface);
        editTextTargets.setText(defaultTargets);

        checkBox1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            if(!editText.getText().toString().equals("")){
                    buttonQuit.setEnabled(true);
                    Log.d("EfA",editText.getText().toString());

                    File root = new File(Environment.getExternalStorageDirectory(), "Ettercap For Android");

                    if (!root.exists()) {
                        root.mkdirs();
                    }

                    StringBuilder builder1 = new StringBuilder("");
                    if (checkBox1.isChecked()) {
                        builder1.append("cd " + Constants.FILES_DIR + "EttercapForAndroid/bin/" + " && pwd && su &&" + Constants.CHMOD + " && "+
                                Constants.FILES_DIR + "EttercapForAndroid/bin/ettercap "
                                + "-i " + editTextInterface.getText().toString() + " "
                                + editText.getText().toString() + " "
                                + "-w " + Environment.getExternalStorageDirectory() + "/Ettercap\\ for\\ Android/" + editTextOutput.getText().toString() + " "
                                + editTextTargets.getText().toString()
                        );

                        Toast.makeText(getApplicationContext(), "Saving to /sdcard/Ettercap for Android/" + editTextOutput.getText().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        builder1.append("cd " + Constants.FILES_DIR + "EttercapForAndroid/bin/" + " && pwd && su &&" + Constants.CHMOD + " && "+
                                Constants.FILES_DIR + "EttercapForAndroid/bin/ettercap "
                                + "-i " + editTextInterface.getText().toString() + " "
                                + editText.getText().toString() + " "
                                + editTextTargets.getText().toString()
                        );
                    }
                    String cmdline = builder1.toString();
                    executeTask = new ExecuteTask(getApplicationContext());
                    executeTask.execute(cmdline);

                    button1.setAlpha(.5f);
                    button1.setClickable(false);

                    //@TODO fix running multiple times, then see if we still need this button
                    //buttonKill.setEnabled(true);

                    editText.setEnabled(false);
                    editTextInterface.setEnabled(false);
                    editTextOutput.setEnabled(false);
                    editTextTargets.setEnabled(false);
                    checkBox1.setEnabled(false);

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No args!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
/*                StringBuilder builder2 = new StringBuilder("");
                builder2.append("q");
                String cmdline = builder2.toString();
                executeTask2 = new ExecuteTask(getApplicationContext());
                executeTask2.execute(cmdline);*/
                try {
                    outputStream.writeBytes("q");
                    outputStream.flush();
                    Log.d("EfA", "Quit");
                } catch (Throwable e) {
                }
                buttonKill.setEnabled(true);

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
        buttonKill.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                executeTask.cancelScan();

            }
        });

    }

    protected void copyFromAssetsToInternalStorage(String filename) {
        AssetManager assetManager = getAssets();

        try {
            InputStream input = assetManager.open(filename);
            OutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);

            copyFile(input, output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unZipFile(String filename) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(openFileInput(filename));
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                FileOutputStream zipOutputStream = openFileOutput(zipEntry.getName(), MODE_PRIVATE);

                int length;
                byte[] buffer = new byte[1024];

                while ((length = zipInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }

                zipOutputStream.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
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
                    Log.i("EfA", "Single Executing: " + single);
                    outputStream.writeBytes(single + "\n");
                    outputStream.flush();
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                while (((pstdout = inputStream.readLine()) != null)) {
                    if (isCancelled()) {
                        scanProcess.destroy();
                        break;
                    } else {
                        if (pstdout != null) {
                            pstdout = pstdout + "\n";
                            wholeoutput.append(pstdout);
                        }
                        Log.i("EfA", "Stdout: " + pstdout);
                        publishProgress(pstdout, null);
                        pstdout = null;
                    }
                    Log.d("EfA", "are we running?");
                }


                if (!isCancelled()) scanProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            return wholeoutput.toString();
        }

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

            if(mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            setSupportProgressBarIndeterminateVisibility(false);
            recreate();
            //startedScan=false;
            //scanButton.setText(getString(R.string.scanbtn));
            //scrollToBottom();

            //@TODO if we can get this running multiple times, then enable changing the settings. otherwise, keep everything disabled until restart.
/*            button1.setAlpha(1f);
            button1.setClickable(true);
            editText.setEnabled(true);
            editTextInterface.setEnabled(true);
            editTextTargets.setEnabled(true);
            editTextOutput.setEnabled(true);
            checkBox1.setEnabled(true);
            buttonKill.setEnabled(false);*/

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
            Log.d("EfA", "Canceled: " + String.valueOf(executeTask.isCancelled()));
            Thread cancelThread = new Thread() {
                @Override
                public void run() {
                    try {
                        String killstr = new String("/system/bin/kill -9 " + ProcessUtil.getppid(ProcessUtil.getpid(scanProcess), shellToRun));
                        Log.i("EfA", "Executing kill: " + killstr);
                        Runtime.getRuntime().exec(killstr);
                    } catch (IOException e) {
                        Log.e("EfA", "Error killing process");
                    }
                }

            };
        }

    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog alert;
        if (id == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("LEGAL DISCLAIMER: I AM NOT RESPONSIBLE FOR ANYTHING THAT MAY HAPPEN BECAUSE OF THIS APP. BY USING THIS APP, YOU AFFIRM THAT YOU HAVE" +
                    " PERMISSION TO USE ETTERCAP AND SIMILAR TOOLS ON THE NETWORK.\n" +
                    "This app is in beta. Ettercap is a big program. MOST of the features are untested, aside from the main ones. Expect frequent restarts.")
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
            alert = builder.create();
            //return alert;
        } else if (id == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("To use this tool, put in the proper arguments for ettercap, such as \"-Tq -M" +
                    " arp:remote\". The interface in most cases will be wlan0. You can set the targets like Ettercap: // // targets the whole network, while " +
                    "/192.168.1.101/ // will target that specific IP. The -i field is automatically added with the value in \"interface\", and the -w field is added" +
                    " if \"output file\" is checked. The file will be saved to the 'Ettercap for Android' folder on the SD card. In case of crashes, go to the " +
                    "app's settings and ensure that the permissions are enabled.\n" +
                    "Once ettercap is running, you can use the lower run command to input certain keystrokes. For example, 'h' will show the available options, and " +
                    "'q' will quit ettercap safely. The quit button is identical to running 'q'.\n" +
                    "This app is still in beta. Expect crashes, and expect to restart after using once. The 'Kill AsyncTask' button is supposed to refresh the app " +
                    "so it can be run again, but it still doesn't work, so it's disabled for now. To run ettercap again, restart the app." +
                    "\n I'd love to hear your feedback! Drop me a message at jolt0101@gmail.com, or at least until I find a better way of collecting feedback.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton("Ettercap Man Page", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://linux.die.net/man/8/ettercap"));
                    startActivity(i);
                }
            });
            alert = builder.create();
            //return alert;
        }
        else {return null;}
        return alert;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.disclaimer:
                showDialog(0);
                return true;
            case R.id.help:
                showDialog(1);
                return true;
            case R.id.settings:
                startActivity( new Intent(MainActivity.this, SettingsActivity.class));
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}

