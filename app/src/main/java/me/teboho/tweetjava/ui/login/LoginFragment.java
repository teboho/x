package me.teboho.tweetjava.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.OAuthProvider;

import java.util.Map;
import java.util.function.Consumer;

import me.teboho.tweetjava.MainActivity;
import me.teboho.tweetjava.R;
import me.teboho.tweetjava.databinding.FragmentLoginBinding;
import me.teboho.tweetjava.util.Utility;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LoginViewModel dashboardViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnLogin.setOnClickListener(l -> {
            Log.d("CLICK", "Login clicked");
            OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
            if (pendingResultTask != null) {
                // The user is already signed in...
                pendingResultTask
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // Getting the OAuth tokens
                                String accToken = ((OAuthCredential)authResult.getCredential()).getAccessToken().toString();
                                String secToken = ((OAuthCredential)authResult.getCredential()).getSecret().toString();

                                Utility.tokenKey = accToken;
                                Utility.tokenSecret = secToken;

                                Log.d("Access", accToken);
                                Log.d("Secret", secToken);

                                // Additional provider information ..
                                String username = authResult.getAdditionalUserInfo().getUsername();
                                Log.d("Username", username);
                                var profile = authResult.getAdditionalUserInfo().getProfile();
                                profile.entrySet().forEach(new Consumer<Map.Entry<String, Object>>() {
                                    @Override
                                    public void accept(Map.Entry<String, Object> stringObjectEntry) {
                                        Log.d("Entry, Key:", stringObjectEntry.getKey());
                                        Log.d("Entry, Value:\t", stringObjectEntry.getValue() + "");
                                    }
                                });

                                Navigation.findNavController(getView()).navigate(R.id.action_navigation_login_to_navigation_home);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                            }
                        })
                ;
            } else {
                // There's no pending result so we need to start the sign-in flow.
                firebaseAuth
                        .startActivityForSignInWithProvider(requireActivity(), provider.build())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                String accToken = ((OAuthCredential)authResult.getCredential()).getAccessToken().toString();
                                String secToken = ((OAuthCredential)authResult.getCredential()).getSecret().toString();

                                // authResult.getCredential().
                                Utility.tokenKey = accToken;
                                Utility.tokenSecret = secToken;

                                Log.d("Access", accToken);
                                Log.d("Secret", secToken);


                                String username = authResult.getAdditionalUserInfo().getUsername();
                                Log.d("Username", username);
                                var profile = authResult.getAdditionalUserInfo().getProfile();
                                profile.entrySet().forEach(new Consumer<Map.Entry<String, Object>>() {
                                    @Override
                                    public void accept(Map.Entry<String, Object> stringObjectEntry) {
                                        Log.d("Entry, Key:", stringObjectEntry.getKey());
                                        Log.d("Entry, Value:\t", stringObjectEntry.getValue() + "");
                                    }
                                });

                                Navigation.findNavController(getView()).navigate(R.id.action_navigation_login_to_navigation_home);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure.
                            }
                        })
                ;
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}