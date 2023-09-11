package me.teboho.tweetjava.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;

import java.io.Console;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import me.teboho.tweetjava.OAuthSignature;
import me.teboho.tweetjava.databinding.FragmentHomeBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    public static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final Button button = binding.button;
        final TextInputLayout textInputLayout = binding.textInputLayout;

        // Using OAuth 1.0 :)
        // It's just a bunch of key value pairs on the authorization header
        String url = "https://api.twitter.com/2/tweets";
        String method = "POST";
        String consumerKey = "SBWB41Ee7BudqHpDPEZ0etgFk";
        String consumerSecret = "M42YrEdLnYmozwLbc6Gp5S4DCHP1d1n0EPLAfasHrP0wyUUWwF";
        String tokenKey = "1542207689870254080-2TRDnGU2AhrtIHdufULcJ1zDZZXx3t";
        String tokenSecret = "JiYEyNXg2VjUVxSWIE5ZzAbKfwrNuJYpboUbv95ZKPURL";
        long longTimeStamp = new Date().getTime() / 1000;
        String timestamp = Long.toString(longTimeStamp);
        String nonce = genNonce(11);
        
        // parameter string
        String parameter_string=null;
        try {
            parameter_string = URLEncoder.encode("oauth_consumer_key", "UTF-8") + "="
                    + URLEncoder.encode(consumerKey, "UTF-8") + "&" + URLEncoder.encode("oauth_nonce", "UTF-8") + "="
                    + URLEncoder.encode(nonce, "UTF-8") + "&" + URLEncoder.encode("oauth_signature_method", "UTF-8")
                    + "=" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "&"
                    + URLEncoder.encode("oauth_timestamp", "UTF-8") + "=" + URLEncoder.encode(timestamp, "UTF-8") + "&"
                    + URLEncoder.encode("oauth_token", "UTF-8") + "=" + URLEncoder.encode(tokenKey, "UTF-8") + "&"
                    + URLEncoder.encode("oauth_version", "UTF-8") + "=" + URLEncoder.encode("1.0", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // creating signature
        String signature_base_string=null;
        try {
            signature_base_string = method + "&" + URLEncoder.encode(url, "UTF-8") + "&"
                    + URLEncoder.encode(parameter_string, "UTF-8");
        } catch (UnsupportedEncodingException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }

        String oauth_signature = "";
        oauth_signature = computeSignature(signature_base_string, consumerSecret, tokenSecret);
        String signature=null;
        try {
            signature = URLEncoder.encode(oauth_signature, "UTF-8");
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

//        String signature = OAuthSignature.generateSignature(method, url, consumerKey, consumerSecret, tokenKey, tokenSecret);
        String authHeaderValue = "OAuth " +
                "oauth_consumer_key=\""+consumerKey+"\"" + "," +
                "oauth_token=\""+tokenKey+"\"" + "," +
                "oauth_signature_method=\"HMAC-SHA1\"" + "," +
                "oauth_timestamp=\""+ timestamp + "\"" + "," +
                "oauth_nonce=\""+ nonce +"\"" + "," +
                "oauth_version=\"1.0\"" + "," +
                "oauth_signature=\""+signature+"\"";
        System.out.println(authHeaderValue);
        button.setOnClickListener(l -> {
            String tweetMessage = textInputLayout.getEditText().getText().toString();
            tweetMessage = complyJSON(tweetMessage);
            Log.d("TEXT", tweetMessage);
            if (tweetMessage.isEmpty()) {
                textInputLayout.setError("Type sth");
                return;
            }

            String message = "{" +
                    "\"text\" : \""+tweetMessage+"\"" +
                    "}";
            Log.d("MESSAGE", message);
            ExecutorService service = Executors.newSingleThreadExecutor();
            String finalMessage = message;
            service.execute(() -> {
                RequestBody body = RequestBody.create(finalMessage, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", authHeaderValue)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d("RESP", response.body().string());
                    System.out.println();
                } catch (IOException e) {
                    Log.e("RESP", "Sth aint right");
                    throw new RuntimeException(e);
                }
            });
        });

        return root;
    }

    public String genNonce(int len) {
        // Pick from some letters that won't be easily mistaken for each
        // other. So, for example, omit o O and 0, 1 l and L.
        String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ0123456789";

        String pw = "";
        for (int i = 0; i < len; i++) {
            int index = (int) (new Random().nextDouble() * letters.length());
            pw += letters.substring(index, index + 1);
        }
        return pw;
    }

    private String computeSignature(String signatureBaseStr, String oAuthConsumerSecret, String oAuthTokenSecret) {

        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec;
            if (null == oAuthTokenSecret) {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&';
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            } else {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&'
                        + URLEncoder.encode(oAuthTokenSecret, "UTF-8");
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            }
            mac.init(spec);
            byteHMAC = mac.doFinal(signatureBaseStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(byteHMAC));
    }

    /**
     * Make string json compliant
     * @param str the string to make json compliant
     * @return the json compliant string
     */
    public static String complyJSON(String str) {
        String escaped = str.replace("\"", "\\"+"\"");
        escaped = escaped.replace("\n", "\\" + "n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        escaped = escaped.replace("\b", "\\b");
        escaped = escaped.replace("\f", "\\f");
        escaped = escaped.replace("/", "\\/");

        return escaped;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}