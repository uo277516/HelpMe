package com.example.helpme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import auth.Authentication;
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


    private FirebaseUser userInSession = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Iniciar sesión");

        initFields();


        btCreateAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToCreateAnAccountView();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    signIn();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }

    /**
     * Inicio de sesión.
     */
    private void signIn() {
        String email = txEmail.getText().toString();
        String pass = txPassword.getText().toString();
        Authentication.getInstance().signIn(email, pass, new LoginCallback() {
            @Override
            public void callback() {
                redirectToHomeView();
            }
        }, new LoginCallback() {
            @Override
            public void callback() {
                // Failure
                Toast.makeText(getApplicationContext(), "Las credenciales no son correctas", Toast.LENGTH_SHORT).show();
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
    }

    /**
     * Validacion de los campos del formulario de inicio de sesión.
     *
     * @return true si todos los campos son válidos y false en caso contrario.
     */
    private boolean validateFields() {
        boolean isValid = true;

        if (!FormValidator.isNotEmpty(txEmail.getText().toString())) {
            txEmail.setError(getText(R.string.email_empty));
            isValid = false;
        }

        if (!FormValidator.isNotEmpty(txPassword.getText().toString())) {
            txPassword.setError(getText(R.string.password_empty));
            isValid = false;
        }

        if (!FormValidator.isEmailValid(txEmail.getText().toString())) {
            txEmail.setError(getText(R.string.email_invalid));
            isValid = false;
        }

/*        if (!FormValidator.isPasswordValid(txPassword.getText().toString())) {
            txPassword.setError(getText(R.string.password_invalid));
            isValid = false;
        }*/

        return isValid;
    }

    /**
     * Redireccionamiento a la página de crear una cuenta.
     */
    private void redirectToCreateAnAccountView() {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);

        startActivity(intent);
    }

    /**
     * Inicialización de los campos del formulario de login.
     */
    private void initFields() {
        btLogin = (Button) findViewById(R.id.button_login_login);
        txEmail = (TextInputEditText) findViewById(R.id.text_email_login);
        txPassword = (TextInputEditText) findViewById(R.id.text_password_login);
        btCreateAnAccount = (Button) findViewById(R.id.button_create_account_login);
    }

    @Override
    public void checkConnection() {
        NetworkStatusChecker.getInstance().handleConnection(getApplicationContext(), new NetworkStatusChecker.ConnectionCallback() {
            @Override
            public void callback(boolean isConnected) {
                handleConnection(isConnected);
            }
        });
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
        void callback();
    }
}