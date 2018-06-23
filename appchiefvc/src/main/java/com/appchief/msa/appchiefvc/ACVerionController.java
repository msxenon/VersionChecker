package com.appchief.msa.appchiefvc;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static com.appchief.msa.appchiefvc.SensetiveData.token;
import static com.appchief.msa.appchiefvc.SensetiveData.urlChecker;

/**
 * Created by mac on 3/7/18.
 */

public class ACVerionController {
    private String PACKAGE_NAME;
    private Context context;
    private VersionCheckListener versionCheckListener;
    private int appVer;
    private String android_id;
    private String countryCode;
    private String countryName;
    private String carrierName;
    private String lang;
    public String getDefaultCountryCode() {
        String defaultCountryCode= "";
        if (defaultCountryCode.isEmpty()){
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                defaultCountryCode = tm != null ? tm.getNetworkCountryIso() : "";
            }catch (Exception s){s.printStackTrace();}
        }
        return defaultCountryCode;
    }
    public ACVerionController init(Context context) {

        this.context = context;
        this.PACKAGE_NAME = context.getPackageName();
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.appVer = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            android_id = getUniquePsuedoID();
            lang = Locale.getDefault().getLanguage();
            countryCode = getDefaultCountryCode();
             Locale loc = new Locale("",countryCode);
            countryName = loc.getDisplayCountry();
            Log.e("testc",countryCode+" "+countryName);
            TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = manager.getSimOperatorName();
            Log.e("testcarrier",carrierName+" hello");

        }catch (Exception s){s.printStackTrace();}
      return this;
    }
    /**
     * Return pseudo unique ID
     * @return ID
     */
    public static String getUniquePsuedoID() {
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

        // Thanks to @Roman SL!
        // https://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            // Go ahead and return the serial for api => 9
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial"; // some value
        }

        // Thanks @Joe!
        // https://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
    public void Check(VersionCheckListener versionCheckListener){
        if (context == null || PACKAGE_NAME == null )
            throw new Error("init() method didnt called");

        if (versionCheckListener == null)
            throw new Error("there is no version check listener attached");
        this.versionCheckListener = versionCheckListener;
        new CheckPersonalData().execute();

    }
    protected class CheckPersonalData extends AsyncTask<Void, Void, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(Void... voids) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(SensetiveData.jsonIP);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return new JSONObject(buffer.toString());


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                Log.e("Test",jsonObject.toString());
                 new CheckVersionTask().execute(jsonObject);

            }catch (Exception s){s.printStackTrace();}

        }
    }
    protected class CheckVersionTask extends AsyncTask<JSONObject, Void, JSONObject>
    {
        public  String GetToday(){
            Date presentTime_Date = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-d-M");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.format(presentTime_Date);
        }
        /** Returns the consumer friendly device name */

    public   String getDeviceName() {
         String model = Build.MODEL;
      return model;
    }
        TinyDB tinyDB;

        {
            tinyDB = new TinyDB(context);
        }

        @Override
        protected JSONObject doInBackground(JSONObject... paramX)
        {

             URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                String unixTime = GetToday();
                URL url = new URL(urlChecker);
                urlConn = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                httpConn.setRequestMethod("POST");
                String agent =System.getProperty("http.agent");
                urlConn.setRequestProperty("User-agent", agent);
                String md5 = md5(token+unixTime+agent);
                httpConn.setRequestProperty ("X-Authorization-Token", md5);
                httpConn.setRequestProperty ("X-Authorization-Time", unixTime);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("package_name", PACKAGE_NAME);
                params.put("platform", "2");
                params.put("unique_id",android_id);
                if (carrierName == null|| carrierName.isEmpty())
                    carrierName = "n";
                params.put("country_code",countryCode);

                params.put("carrier_name",carrierName);
                params.put("lib_ver",SensetiveData.lib_ver);
                params.put("device_model",getDeviceName());
                params.put("sdk_ver",Build.VERSION.SDK_INT);
                params.put("os_ver",Build.VERSION.RELEASE);
                params.put("country_name",countryName);
                params.put("lang",lang);

                     params.put("ip",paramX[0].getString("ip"));

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                httpConn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                httpConn.setDoOutput(true);
                httpConn.getOutputStream().write(postDataBytes);
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONObject(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                try {
                    return new JSONObject(tinyDB.getString("avc7911820"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        public String md5(String s) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                // Create Hex String
                StringBuffer hexString = new StringBuffer();
                for (int i=0; i<messageDigest.length; i++)
                    hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

                return hexString.toString();
            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }
        @Override
        protected void onPostExecute(JSONObject response)
        {
             if(response != null)
            {
                tinyDB.putString("avc7911820",response.toString());
                  Log.e("reponse ",response.toString());
                try {
                    String link = "";
                    link = response.getString("link");
                    if (link.isEmpty())
                        link = "https://play.google.com/store/apps/details?id="+PACKAGE_NAME;
                    String message = response.getString("msg");
                    if (response.getInt("mand_ver") > appVer){
                        versionCheckListener.CheckSuccess(CheckStatus.CHECK_NEWMANDATARY_UPDATE,message,link);
                    } else if (response.getInt("latest_ver") > appVer) {
                        versionCheckListener.CheckSuccess(CheckStatus.CHECK_NEWUPDATE_AVAILABLE,message,link);
                    } else {
                        versionCheckListener.CheckSuccess(CheckStatus.CHECK_NOUPDATE,"no new updates",link);
                    }
                } catch (JSONException ex) {
                    String msg = "";
                    if (response.has("api_message"))
                        try {
                            msg = response.getString("api_message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    versionCheckListener.CheckError(msg);

                    Log.e("App", "Failure", ex);
                }
            }else{
                versionCheckListener.CheckError("Response is null");
            }
        }
    }
}
