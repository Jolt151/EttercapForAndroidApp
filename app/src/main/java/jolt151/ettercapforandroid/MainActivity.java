package jolt151.ettercapforandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView textView1;
    AssetManager assetManager;
    ExecuteTask executeTask;
    ExecuteTask executeTask2;
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
        //copyAssetFolder(assetManager,"file://android_asset", toPath);
        copyFromAssetsToInternalStorage("ettercap-android.zip");
        unZipFile("ettercap-android.zip");
        File file = new File("/data/data/" + getPackageName() + "/files/ettercap");
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
        buttonKill.setAlpha(.5f);
        buttonKill.setClickable(false);

        checkBox1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
/*                Process process = null;
                try {
                    process = new ProcessBuilder()
                            .command(editText.getText().toString())
                            .start();


                    OutputStream stdin = process.getOutputStream();
                    InputStream stderr = process.getErrorStream();
                    InputStream stdout = process.getInputStream();
                    //stdin.write("su".getBytes());
                    stdin.write(editText.getText().toString().getBytes());
                    //stdin.write("exit\n".getBytes());
                    stdin.flush();
                    stdin.close();


                    BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        Log.d("[Output]", line);
                    }
                    br.close();
                    br =
                            new BufferedReader(new InputStreamReader(stderr));
                    while ((line = br.readLine()) != null) {
                        Log.e("[Error]", line);
                    }
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) process.destroy();
                }
            */
/*                String filePath = Environment.getExternalStorageDirectory().toString()+"/Ettercap for Android";
                String fileName = editTextOutput.getText().toString();
                //textView1.setText("Output: \n \n " + runAsRoot());
                //String outp = runAsRoot("/data/data/jolt151.ettercapforandroid/files/ettercap "+ editText.getText().toString());
                //String outp = runAsRoot("ls");
                //textView1.setText(outp);
                //Log.d("Output", outp);
               *//* StringBuilder builder = new StringBuilder("");
                builder.append("su");
                String cmdline = builder.toString();
                executeTask = new ExecuteTask(getApplicationContext());
                executeTask.execute(cmdline);*//*             File file = new File(filePath, fileName);
                File file1 = new File()
                Log.d("EfA", file.toString());
                Log.d("EfA",String.valueOf(file.exists()));

                if (!file.exists() || file.isDirectory())
                {
                    file.mkdirs();
                }
                Log.d("EfA",String.valueOf(file.exists()));*/

                File root = new File(Environment.getExternalStorageDirectory(), "Ettercap For Android");

                if (!root.exists()) {
                    root.mkdirs();
                }

                StringBuilder builder1 = new StringBuilder("");
                if (checkBox1.isChecked())
                {
                    builder1.append("cd /data/data/" + getPackageName() +"/files && pwd && su &&" +"/data/data/jolt151.ettercapforandroid/files/ettercap "
                            + "-i " + editTextInterface.getText().toString() +" "
                            + editText.getText().toString() +" "
                            + "-w " + Environment.getExternalStorageDirectory() + "/Ettercap\\ for\\ Android/" + editTextOutput.getText().toString() +" "
                            + editTextTargets.getText().toString()
                    );

                    Toast.makeText(getApplicationContext(),"Saving to /sdcard/Ettercap for Android/" + editTextOutput.getText().toString(),Toast.LENGTH_SHORT);
                }
                else
                {
                    builder1.append(" cd /data/data/" + getPackageName() +"/files && pwd && whoami &&" +"/data/data/jolt151.ettercapforandroid/files/ettercap "
                            + "-i " + editTextInterface.getText().toString() +" "
                            + editText.getText().toString() +" "
                            + editTextTargets.getText().toString()
                    );
                }

                String cmdline = builder1.toString();
                executeTask = new ExecuteTask(getApplicationContext());
                //executeTask.execute("su");
                executeTask.execute(cmdline);

                button1.setAlpha(.5f);
                button1.setClickable(false);

                buttonKill.setAlpha(1f);
                Log.d("EfA", String.valueOf(buttonKill.getAlpha()));
                buttonKill.setClickable(true);
            }
        });
        buttonQuit.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0){
/*                StringBuilder builder2 = new StringBuilder("");
                builder2.append("q");
                String cmdline = builder2.toString();
                executeTask2 = new ExecuteTask(getApplicationContext());
                executeTask2.execute(cmdline);*/
                try {
                     outputStream.writeBytes("q");
                     outputStream.flush();
                     Log.d("EfA", "Quit");
                }
                 catch (Throwable e){}

                button1.setAlpha(1f);
                button1.setClickable(true);
            }
        });
        buttonCustom.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                try{
                    outputStream.writeBytes(editTextCustom.getText().toString());
                    outputStream.flush();

                }
                catch (Throwable e){}
            }
        });
        buttonKill.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                executeTask.cancel(true);
            }
        });

    }


    String toPath = "/data/data/" + "jolt151.ettercapforandroid";  // Your application path
/*    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }*/


    protected void copyFromAssetsToInternalStorage(String filename){
        AssetManager assetManager = getAssets();

        try {
            InputStream input = assetManager.open(filename);
            OutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);

            copyFile(input, output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unZipFile(String filename){
        try {
            ZipInputStream zipInputStream = new ZipInputStream(openFileInput(filename));
            ZipEntry zipEntry;

            while((zipEntry = zipInputStream.getNextEntry()) != null){
                FileOutputStream zipOutputStream = openFileOutput(zipEntry.getName(), MODE_PRIVATE);

                int length;
                byte[] buffer = new byte[1024];

                while((length = zipInputStream.read(buffer)) > 0){
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

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

/*    public String runAsRoot() {

        try {
            // Executes the command.

            Process process = Runtime.getRuntime().exec(*//*editText.getText().toString()*//*
                        "/data/data/jolt151.ettercapforandroid/files/ettercap " + editText.getText().toString());

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException("not found");
        } catch (InterruptedException e) {
            throw new RuntimeException("wtf");
        } */

/*    public void runAsRoot(){
        try {
            Process process = Runtime.getRuntime().exec("/data/data/jolt151.ettercapforandroid/files/ettercap " + editText.getText().toString());
            //Runtime.getRuntime().exec("")
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder result=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            textView1.setText(result.toString());
        }
        catch (IOException e) {}
    }*/
/*    public String runAsRoot(String command)
    {
        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;

    }*/
private class ExecuteTask extends AsyncTask<String,String,String> {
    final Context context;
    PowerManager.WakeLock mWakeLock;
    java.lang.Process scanProcess;
    String shellToRun = "sh";

    @Override
    protected String doInBackground(String... sParm) {
        String cmdline=sParm[0];
        String pstdout=null;
        StringBuilder wholeoutput = new StringBuilder("");
        String[] commands = { /*"cd /data/data/" + getPackageName() +"/files", "pwd","su",*/"su", cmdline };

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
                Log.i("EfA","Single Executing: "+single);
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
        if (progress[0]!=null) textView1.append(progress[0]);
        if (progress[1]!=null) textView1.append(progress[1]);
        //scrollToBottom();
    }

    public ExecuteTask(Context context) {
        this.context = context;
    }

    protected void cleanupOnEnd () {
        mWakeLock.release();
        setSupportProgressBarIndeterminateVisibility(false);
        //startedScan=false;
        //scanButton.setText(getString(R.string.scanbtn));
       //scrollToBottom();

    }

    @Override
    protected void onCancelled () {
        cleanupOnEnd();
        //cancelDialog.dismiss();
        //Toast.makeText(context,getString(R.string.toast_scan_canceled), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String result) {
        cleanupOnEnd();
        // For future: add scan to history scans
        // if (result!=null) outputView.append(result);
        //Toast.makeText(context,getString(R.string.toast_scan_finished), Toast.LENGTH_SHORT).show();
    }

}



}
