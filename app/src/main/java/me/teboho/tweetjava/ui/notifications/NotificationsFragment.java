package me.teboho.tweetjava.ui.notifications;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.teboho.tweetjava.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    int SELECT_PICTURE = 200;
    private static Uri imageUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textNotifications;
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final ImageView imageView = binding.imageView;
        final Button btnSelectImage = binding.btnSelectImage;
        final Button btnTweetImage = binding.btnTweetImage;

        btnSelectImage.setOnClickListener(l -> {
            imageChooser();
        });
        btnTweetImage.setOnClickListener(l -> tweetImage());

        return root;
    }

    private void tweetImage() {
        // First get the image
        if (imageUri != null) {
            //...
        }
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchImagePicker.launch(i);
//        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == SELECT_PICTURE) {
//                Uri selectedImageUri = data.getData();
//                if (selectedImageUri != null) {
//                    binding.imageView.setImageURI(selectedImageUri);
//                    binding.imageView.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    }

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
                            imageUri = selectedImageUri;
                        }
                    }
                }
            }
    );
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}