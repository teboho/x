package me.teboho.tweetjava.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.teboho.tweetjava.MainActivity;
import me.teboho.tweetjava.R;
import me.teboho.tweetjava.databinding.FragmentHomeBinding;
import me.teboho.tweetjava.util.Utility;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private static final OkHttpClient client = new OkHttpClient();
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
        String consumerKey = Utility.consumerKey;
        String consumerSecret = Utility.consumerSecret;
        String tokenKey = Utility.tokenKey;
        String tokenSecret = Utility.tokenSecret;
        long longTimeStamp = new Date().getTime() / 1000;
        String timestamp = Long.toString(longTimeStamp);
        String nonce = Utility.genNonce(11);
        
        // parameter string
        String parameter_string="";
        try {
            parameter_string = Utility.genParamaterString(nonce, timestamp, consumerKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // creating signature base string with METHOD and URL and parameters string
        String signature_base_string=null;
        try {
            signature_base_string = method + "&" + URLEncoder.encode(url, "UTF-8") + "&"
                    + URLEncoder.encode(parameter_string, "UTF-8");
        } catch (UnsupportedEncodingException e3) {
            e3.printStackTrace();
        }

        String signature=null;
        try {
            signature = Utility.prepareSignature(signature_base_string, consumerSecret, tokenSecret);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }

        String authHeaderValue = Utility.genOAuthHeader(nonce, timestamp, signature, consumerKey, tokenKey);
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
                        .addHeader("Authorization", authHeaderValue) // oAuth header value comes here
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d("RESP", response.body().string());
                    System.out.println();
                    if (response.code() < 300) {
                        requireActivity().runOnUiThread(() -> {
                            Toast toast = Toast.makeText(requireActivity(), "Post made :)", Toast.LENGTH_SHORT);
                            toast.show();
                        });
                    }
                } catch (IOException e) {
                    Log.e("RESP", "Sth aint right");
                    requireActivity().runOnUiThread(() -> {
                        Toast toast = Toast.makeText(requireActivity(), "Sth went wrong :(", Toast.LENGTH_SHORT);
                        toast.show();
                    });
                    throw new RuntimeException(e);
                }
            });
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Utility.tokenSecret.equals("")) {
            ((MainActivity)requireActivity()).getNavController().navigate(R.id.action_navigation_home_to_navigation_login);
        }
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