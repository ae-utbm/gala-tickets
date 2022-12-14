package djamelfel.gala;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class Login extends ActionBarActivity implements View.OnClickListener {
    private EditText _ipAddress;
    private EditText _portNumber;
    private ArrayList<Key_List> key_list;
    private ArrayList<String> ban_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi

        _ipAddress = (EditText)findViewById(R.id.text_ipAddress);
        _portNumber = (EditText)findViewById(R.id.text_port);
        Button validateIPAddress = (Button) findViewById(R.id.button_ipAddress);
        validateIPAddress.setOnClickListener(this);

        key_list = new ArrayList<Key_List>();
        ban_list = new ArrayList<String>();

    }

    @Override
    public void onClick(View vue) {
        final Button button = (Button)findViewById(R.id.button_ipAddress);
        button.setEnabled(false);
        button.setText(getText(R.string.connecting));

        String myIpString = _ipAddress.getText().toString();
        String myPortString = _portNumber.getText().toString();
        final String server = "http://" + myIpString + ":" + myPortString;

        // check if ip address is right
        if (!InetAddressUtils.isIPv4Address(myIpString)) {
            display(getString(R.string.errorIP), false);
            button.setEnabled(true);
            button.setText(getText(R.string.connect));
        }
        else if (myPortString.isEmpty()) {
            display(getString(R.string.errorPORT), false);
            button.setEnabled(true);
            button.setText(getText(R.string.connect));
        }
        else {
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(5000);
            client.get(server + "/keys", new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    JSONObject element;
                    try {
                        for (int index = 0; index < response.length(); index++) {
                            element = response.getJSONObject(index);
                            Key_List key = new Key_List(element.getInt("id"), element.getString
                                    ("key"), element.getBoolean("is_child"));
                            key_list.add(key);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Send key_list to next activity
                    //Intent intent = new Intent(Login.this, Read_QR_Code.class);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }

            });
            client.get(server + "/banlist", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    try {
                        for (int index = 0; index < response.length(); index++){
                            ban_list.add(response.getString(index));
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    //Send banlist to next activity
                    Intent intent = new Intent(Login.this, Read_QR_Code.class);
                    intent.putParcelableArrayListExtra("key_list", key_list);
                    intent.putStringArrayListExtra("ban_list", ban_list);
                    intent.putExtra("server", server);
                    startActivity(intent);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    display(getString(R.string.serverError), false);
                    button.setEnabled(true);
                    button.setText(getText(R.string.connect));
                }
            });
        }
    }

    /**
     *
     * @param msg
     * @param success
     * @info print on screen a message in red if success in false or green if it's true
     */
    public void display(String msg, boolean success) {
        LayoutInflater inflater = getLayoutInflater();
        View layout;
        if(success) {
            layout = inflater.inflate(R.layout.toast_success,
                    (ViewGroup) findViewById(R.id.toast_success));
        }
        else {
            layout = inflater.inflate(R.layout.toast_failure,
                    (ViewGroup) findViewById(R.id.toast_failure));
        }
        TextView text = (TextView)layout.findViewById(R.id.text);
        text.setTextSize(20);
        text.setText(msg.toUpperCase());

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}