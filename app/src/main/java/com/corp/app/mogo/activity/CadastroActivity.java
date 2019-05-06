package com.corp.app.mogo.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.corp.app.mogo.R;
import com.corp.app.mogo.activity.motociclista.RequisicoesActivity;
import com.corp.app.mogo.activity.passageiro.PassageiroActivity;
import com.corp.app.mogo.activity.passageiro.cadastro.FotoActivity;
import com.corp.app.mogo.config.ConfiguracaoFirebase;
import com.corp.app.mogo.helper.UsuarioFirebase;
import com.corp.app.mogo.model.Motociclista;
import com.corp.app.mogo.model.Passageiro;
import com.corp.app.mogo.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Switch switchTipoUsuario;
    private FirebaseAuth autenticacao;
    private final int TIRAR_FOTO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);

    }

    public void validarCadastroUsuario(View view) {

        //Recuperar valor dos campos
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty()) { //Verifica nome
            if (!textoEmail.isEmpty()) { //Verifica e-mail
                if (!textoSenha.isEmpty()) { //Verifica senha

                    if (verificaTipoUsuario().equals("P")) {
                        Passageiro usuario = new Passageiro();
                        usuario.setNome( textoNome );
                        usuario.setEmail( textoEmail );
                        usuario.setSenha( textoSenha );
                        usuario.setTipo( verificaTipoUsuario() );
                        usuario.setStatus(Passageiro.STATUS_FOTO);

                        cadastrarPassageiro(usuario);
                    } else {
                        Motociclista usuario = new Motociclista();
                        usuario.setNome( textoNome );
                        usuario.setEmail( textoEmail );
                        usuario.setSenha( textoSenha );
                        usuario.setTipo( verificaTipoUsuario() );
                        usuario.setStatus(Motociclista.STATUS_INFORMACOES);

                        cadastrarMotoiclista(usuario);
                    }

                } else {
                    Toast.makeText(CadastroActivity.this, "Crie uma senha!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }

    }

    public String verificaTipoUsuario() {
        return switchTipoUsuario.isChecked() ? "M" : "P";
    }

    public void cadastrarPassageiro(final Passageiro usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId( idUsuario );
                        usuario.salvar();

                        //Salvar dados no profile do Firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        //INICIO CONTORNO BUG DISPLAY NAME FIREBASE
                        //Logoff
                        autenticacao.signOut();
                        //Loga novamente com o usuário cadastrado
                        autenticacao.signInWithEmailAndPassword(
                                usuario.getEmail(), usuario.getSenha()
                        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    startActivity(new Intent(CadastroActivity.this, FotoActivity.class));
                                    finish();

                                    Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                                } else {

                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                        //FIM CONTORNO BUG DISPLAY NAME FIREBASE

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {

                    String excecao;
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthWeakPasswordException e ) {
                        excecao = "Digite uma senha mais forte!";
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        excecao = "Por favor, digite um e-mail válido";
                    } catch ( FirebaseAuthUserCollisionException e ) {
                        excecao = "Já existe uma conta com esse e-mail";
                    } catch ( Exception e ) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    public void cadastrarMotoiclista(final Motociclista usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId( idUsuario );
                        usuario.salvar();

                        //Salvar dados no profile do Firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());


                        startActivity(new Intent(CadastroActivity.this, FotoActivity.class));
                        finish();

                        Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {

                    String excecao;
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthWeakPasswordException e ) {
                        excecao = "Digite uma senha mais forte!";
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        excecao = "Por favor, digite um e-mail válido";
                    } catch ( FirebaseAuthUserCollisionException e ) {
                        excecao = "Já existe uma conta com esse e-mail";
                    } catch ( Exception e ) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    public void cadastrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
          usuario.getEmail(),
          usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId( idUsuario );
                        usuario.salvar();

                        //Salvar dados no profile do Firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        /*
                        autenticacao.signOut();
                        autenticacao.signInWithEmailAndPassword(
                                usuario.getEmail(), usuario.getSenha()
                        );
                         */


                        //Redireciona o usuário com base no seu tipo
                        // Se o usuário for menu_secundario chama a activity maps
                        // Se não chama a activity requisições
                        if ( verificaTipoUsuario() == "P" ) {



                            startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();


                        } else {

                            startActivity(new Intent(CadastroActivity.this, RequisicoesActivity.class));
                            finish();

                            Toast.makeText(CadastroActivity.this, "Parabéns! Você agora é nosso parceiro!", Toast.LENGTH_SHORT).show();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {

                    String excecao;
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthWeakPasswordException e ) {
                        excecao = "Digite uma senha mais forte!";
                    } catch ( FirebaseAuthInvalidCredentialsException e ) {
                        excecao = "Por favor, digite um e-mail válido";
                    } catch ( FirebaseAuthUserCollisionException e ) {
                        excecao = "Já existe uma conta com esse e-mail";
                    } catch ( Exception e ) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

}
