package com.corp.app.mogo.model;

import com.corp.app.mogo.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Passageiro extends Usuario implements Serializable {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String celular;
    private String tipo;
    private String caminhoFoto;
    private String dataNascimento;
    private String status;

    private String latitude;
    private String longitude;

    private String latitudeAtual;
    private String longitudeAtual;

    public static final String STATUS_FOTO = "status_foto";
    public static final String STATUS_A_VERIFICAR = "status_a_verificar";
    public static final String STATUS_VERIFICADO = "status_verificado";

    //Construtor
    public Passageiro() {
    }

    //Método para salvar usuário
    @Override
    public void salvar() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuarios = firebaseRef.child("usuarios").child( getId() );

        usuarios.setValue(this);

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getSenha() {
        return senha;
    }

    @Override
    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    @Override
    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    @Exclude
    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    @Exclude
    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @Override
    public String getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public String getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String getLatitudeAtual() {
        return latitudeAtual;
    }

    @Override
    public void setLatitudeAtual(String latitudeAtual) {
        this.latitudeAtual = latitudeAtual;
    }

    @Override
    public String getLongitudeAtual() {
        return longitudeAtual;
    }

    @Override
    public void setLongitudeAtual(String longitudeAtual) {
        this.longitudeAtual = longitudeAtual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
