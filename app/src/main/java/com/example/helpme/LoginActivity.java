package com.example.helpme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import auth.Authentication;
import chat.AlumnoStatus;
import chat.ChatService;
import network.NetworkStatusChecker;
import network.NetworkStatusHandler;
import util.FormValidator;

public class LoginActivity extends AppCompatActivity implements NetworkStatusHandler {

    public static final String TAG = "LOGIN_ACTIVITY";

    public static final String USER_IN_SESSION = "login_usuario_sesion";

    private TextInputEditText txEmail;
    private TextInputEditText txPassword;
    private Button btLogin;
    private Button btCreateAnAccount;

    private final FirebaseUser userInSession = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.iniciar_sesion);

        initFields();

        btCreateAnAccount.setOnClickListener(view -> redirectToCreateAnAccountView());

        btLogin.setOnClickListener(view -> {
            if (validateFields()) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
    }

    /**
     * Inicio de sesión.
     */
    private void signIn() {
        String email = Objects.requireNonNull(txEmail.getText()).toString().trim().toLowerCase();
        String pass = Objects.requireNonNull(txPassword.getText()).toString().trim();
        Authentication.getInstance().signIn(email, pass, new LoginCallback() {
            @Override
            public void onSuccess() {
                /* Cambiar estado a ONLINE */
                ChatService.getInstance().changeCurrentUserStatus(AlumnoStatus.ONLINE.toString().toLowerCase(), () -> redirectToHomeView());
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), R.string.credenciales_incorrectas, Toast.LENGTH_SHORT).show();
                txEmail.setText("");
                txPassword.setText("");
                txEmail.requestFocus();
            }
        });
    }

    /**
     * Redirecciona a la vista de home con los datos del usuario logeado.
     */
    public void redirectToHomeView() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra(USER_IN_SESSION, userInSession);
        startActivity(intent);
        finish();
    }

    /**
     * Validacion de los campos del formulario de inicio de sesión.
     *
     * @return true si todos los campos son válidos y false en caso contrario.
     */
    private boolean validateFields() {
        /* Email no vacío */
        if (!FormValidator.isNotEmpty(Objects.requireNonNull(txEmail.getText()).toString().trim())) {
            txEmail.setError(getText(R.string.email_empty));
            return false;
        }

        /* Contraseña no vacía */
        if (!FormValidator.isNotEmpty(Objects.requireNonNull(txPassword.getText()).toString())) {
            txPassword.setError(getText(R.string.password_empty));
            return false;
        }

        /* Email válido */
        if (!FormValidator.isEmailValid(txEmail.getText().toString().trim())) {
            txEmail.setError(getText(R.string.email_invalid));
            return false;
        }

        return true;
    }

    /**
     * Redireccionamiento a la página de crear una cuenta.
     */
    private void redirectToCreateAnAccountView() {
        startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
    }

    /**
     * Inicialización de los campos del formulario de login.
     */
    private void initFields() {
        btLogin = (Button) findViewById(R.id.button_login_login);
        txEmail = (TextInputEditText) findViewById(R.id.text_email_login);
        txPassword = (TextInputEditText) findViewById(R.id.text_password_login);
        btCreateAnAccount = (Button) findViewById(R.id.button_create_account_login);

        /* Iniciar foco en el campo Email */
        txEmail.requestFocus();
    }

    @Override
    public void checkConnection() {
        NetworkStatusChecker.getInstance().handleConnection(getApplicationContext(), this::handleConnection);
    }

    @Override
    public void handleConnection(boolean isConnected) {
        if (!isConnected) {
            startActivity(new Intent(LoginActivity.this, NoWifiConnectionActivity.class));
            finish();

        } else if (userInSession != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }

    public interface LoginCallback {
        void onSuccess();

        void onFailure();
    }
}