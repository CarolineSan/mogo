
package com.example.app.mogo.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app.mogo.R;
import com.example.app.mogo.config.ConfiguracaoFirebase;
import com.example.app.mogo.helper.UsuarioFirebase;
import com.example.app.mogo.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);

    }

    public void validarLoginUsuario(View view) {

        //Recuperar valor dos campos
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if ( !textoEmail.isEmpty() ) { //Verifica e-mail
            if ( !textoSenha.isEmpty() ) { //Verifica senha

                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario( usuario );

            } else {
                Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Prrencha o e-mail!", Toast.LENGTH_SHORT).show();
        }

    }

    public void logarUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if ( task.isSuccessful() ) {

                    UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);

                } else {

                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthInvalidUserException e ) {
                        excecao = "Não existe cadastro com o e-mail informado.";
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        excecao = "E-mail e/ou senha incorretos.";
                    } catch ( Exception e ) {
                        excecao = "Erro ao fazer login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

}
