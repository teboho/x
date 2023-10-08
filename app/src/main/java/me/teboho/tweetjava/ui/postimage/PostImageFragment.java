package me.teboho.tweetjava.ui.postimage;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.content.CursorLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.teboho.tweetjava.MainActivity;
import me.teboho.tweetjava.R;
import me.teboho.tweetjava.databinding.FragmentPostimageBinding;
import me.teboho.tweetjava.ui.home.HomeFragment;
import me.teboho.tweetjava.util.Utility;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostImageFragment extends Fragment {
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    /** multipart/form-data */
    public static final MediaType FORMDATA
            = MediaType.get("multipart/form-data");
    private static final MediaType MEDIA_TYPE_JPG
            = MediaType.parse("image/jpg");

    private FragmentPostimageBinding binding;
    int SELECT_PICTURE = 200;
    private static Uri imageUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PostImageViewModel notificationsViewModel =
                new ViewModelProvider(this).get(PostImageViewModel.class);

        binding = FragmentPostimageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ImageView imageView = binding.imageView;
        final Button btnSelectImage = binding.btnSelectImage;
        final Button btnTweetImage = binding.btnTweetImage;

        btnSelectImage.setOnClickListener(l -> {
            // imageChooser();
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()
            );
        });
        btnTweetImage.setOnClickListener(l -> {
            try {
                tweetImage();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Utility.tokenSecret.equals("")) {
            ((MainActivity)requireActivity()).getNavController().navigate(R.id.action_navigation_postimage_to_navigation_login);
        }
    }

    private void tweetImage() throws URISyntaxException {
        if (imageUri == null) return;

        // Using OAuth 1.0 :)
        // It's just a bunch of key value pairs on the authorization header
        String url = "https://upload.twitter.com/1.1/media/upload.json";
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

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);

        Thread t = new Thread(() -> {
            RequestBody body = new MultipartBody.Builder()
                    .setType(FORMDATA)
                    .addFormDataPart("media_category", "tweet_image")
                    .addFormDataPart(
                            "media",
                            "tweet_image.jpg",
                            RequestBody.create(new File(picturePath), MEDIA_TYPE_JPG))
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeaderValue) // oAuth header value comes here
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String strResponse = response.body().string();
                Log.d("RESP", strResponse);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = null;
                jsonNode = objectMapper.readTree(strResponse);
                String mediaId = jsonNode.get("media_id").asText();

                System.out.println("MEDIAID ------> " + mediaId);
                if (response.code() < 300) {
                    finaliseMediaTweet(mediaId);

                    requireActivity().runOnUiThread(() -> {
                        Toast toast = Toast.makeText(requireActivity(), "Post made :)", Toast.LENGTH_SHORT);
                        toast.show();
                        binding.btnTweetImage.setVisibility(View.GONE);
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
        t.start();
    }

    private void finaliseMediaTweet(String mediaId) {
        final String url = "https://api.twitter.com/2/tweets";
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

        String text = binding.etCaption.getEditText().getText().toString();
        text = HomeFragment.complyJSON(text);
        String message = "{" +
                "\"text\" : \""+ text +"\"," +
                "\"media\": {\"media_ids\":[\""+mediaId+"\"]}" +
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
    }

    // Alt: not getting used any more
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(requireContext(), uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            return imagePath;
        }

        return null;
    }

    // Alt: not getting used
    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchImagePicker.launch(i);
    }

    // Alt: not getting used
    ActivityResultLauncher<Intent> launchImagePicker
            = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            binding.imageView.setImageURI(selectedImageUri);
                            binding.imageView.setVisibility(View.VISIBLE);
                            binding.btnTweetImage.setVisibility(View.VISIBLE);
                            binding.etCaption.setVisibility(View.VISIBLE);
                            imageUri = selectedImageUri;
                        }
                    }
                }
            }
    );

    // Registers a photo picker activity launcher in single-select mode
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the photo picker
                if (uri != null) {
                    Log.d("PhotoPicler", "Selected URI: " + uri);
                    binding.imageView.setImageURI(uri);
                    binding.imageView.setVisibility(View.VISIBLE);
                    binding.btnTweetImage.setVisibility(View.VISIBLE);
                    binding.etCaption.setVisibility(View.VISIBLE);
                    imageUri = uri;
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}