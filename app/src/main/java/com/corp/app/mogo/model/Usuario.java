package com.corp.app.mogo.model;

import com.corp.app.mogo.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;
    private  String caminhoFoto;

    private String latitude;
    private String longitude;

    private String latitudeAtual;
    private String longitudeAtual;

    //Construtor
    public Usuario() {
    }

    //Método para salvar usuário
    public void salvar() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuarios = firebaseRef.child("usuarios").child( getId() );

        usuarios.setValue(this);

    }

    //Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitudeAtual() {
        return latitudeAtual;
    }

    public void setLatitudeAtual(String latitudeAtual) {
        this.latitudeAtual = latitudeAtual;
    }

    public String getLongitudeAtual() {
        return longitudeAtual;
    }

    public void setLongitudeAtual(String longitudeAtual) {
        this.longitudeAtual = longitudeAtual;
    }

    @Exclude
    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
