package fr.isep.ii3510.movieman.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import fr.isep.ii3510.movieman.databinding.FragmentProfileBinding;
import fr.isep.ii3510.movieman.ui.login.LoginViewModel;
import fr.isep.ii3510.movieman.ui.login.LoginViewModelFactory;


public class ProfileFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentProfileBinding mBinding;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Context context;

    public ProfileFragment(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = FragmentProfileBinding.inflate(inflater,container,false);


        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = mBinding.username;
        final EditText passwordEditText = mBinding.password;
        final EditText displayNameEditText = mBinding.displayName;
        final Button saveButton = mBinding.save;

        usernameEditText.setText(user.getEmail());
        usernameEditText.setEnabled(false);
        displayNameEditText.setText(user.getDisplayName());

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            saveButton.setEnabled(loginFormState.isDataValid() || (passwordEditText.getText().toString().equals("") && !displayNameEditText.getText().toString().equals("")));
//                boolean pswValid = passwordEditText.getText().toString().equals("") ||  loginFormState.isDataValid();
//                saveButton.setEnabled(pswValid || !displayNameEditText.getText().toString().equals(""));
            if (loginFormState.getPasswordError() != null && !passwordEditText.getText().toString().equals("")) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        passwordEditText.addTextChangedListener(afterTextChangedListener);
        displayNameEditText.addTextChangedListener(afterTextChangedListener);

        saveButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String displayName = displayNameEditText.getText().toString();



            if(!displayName.equals("")){
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(context.getApplicationContext(), "updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            if(!password.equals("")){
                user.updatePassword(password).addOnCompleteListener(task -> Toast.makeText(context.getApplicationContext(), "updated", Toast.LENGTH_SHORT).show());
            }



        });

    }


}
