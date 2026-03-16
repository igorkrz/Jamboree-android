package com.example.jamboree.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.jamboree.R;
import com.example.jamboree.data.repository.AuthRepository;

public class LoginFragment extends Fragment {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loginProgressBar;
    private TextView loginErrorTextView;

    private AuthRepository authRepository;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            authRepository = new AuthRepository(requireContext());
        } catch (Exception e) {
            showError("Secure storage init failed: " + e.getMessage());
            return;
        }

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);
        loginErrorTextView = view.findViewById(R.id.loginErrorTextView);

        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Email and password are required.");
            return;
        }

        showLoading(true);

        new Thread(() -> {
            try {
                authRepository.login(email, password);

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError(null);

                    NavController navController = NavHostFragment.findNavController(this);
                    navController.popBackStack();
                });
            } catch (Exception e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Login failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showLoading(boolean loading) {
        loginProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            loginErrorTextView.setVisibility(View.GONE);
            loginErrorTextView.setText("");
        } else {
            loginErrorTextView.setVisibility(View.VISIBLE);
            loginErrorTextView.setText(message);
        }
    }
}
