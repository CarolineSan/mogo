package com.corp.app.mogo.model;

import com.corp.app.mogo.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Requisicao {

    private String id;
    private String status;
    private String valor;
    private Usuario passageiro;
    private Usuario motociclista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "a_caminho";
    public static final String STATUS_EM_VIAGEM = "em_viagem";
    public static final String STATUS_FINALIZADA = "finalizada";
    public static final String STATUS_EM_ESPERA = "em_espera";
    public static final String STATUS_ENCERRADA = "encerrada";

    public Requisicao() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);

    }

    public void atualizar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());
        Map objeto = new HashMap();
        objeto.put("motociclista", getMotociclista());
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);

    }

    public void atualizarLocalizacaoMotociclista(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId())
                .child("motociclista");

        Map objeto = new HashMap();
        objeto.put("latitudeAtual", getMotociclista().getLatitudeAtual());
        objeto.put("longitudeAtual", getMotociclista().getLongitudeAtual());

        requisicao.updateChildren(objeto);

    }

    public void atualizarValor(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId());

        Map objeto = new HashMap();
        objeto.put("valor", getValor());

        requisicao.updateChildren(objeto);

    }


    public void atualizarStatus() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotociclista() {
        return motociclista;
    }

    public void setMotociclista(Usuario motociclista) {
        this.motociclista = motociclista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
