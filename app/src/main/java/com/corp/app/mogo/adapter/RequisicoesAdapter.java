package com.corp.app.mogo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.corp.app.mogo.R;
import com.corp.app.mogo.helper.Local;
import com.corp.app.mogo.model.Requisicao;
import com.corp.app.mogo.model.Usuario;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario motociclista;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario motociclista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.motociclista = motociclista;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_requisicoes, viewGroup, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Requisicao requisicao = requisicoes.get(i);
        Usuario passageiro = requisicao.getPassageiro();

        myViewHolder.nome.setText(passageiro.getNome());


        if ((motociclista.getLatitude() != null) && (motociclista.getLongitude() != null)) {

            LatLng localPassageiro = new LatLng(
                    Double.parseDouble(passageiro.getLatitude()),
                    Double.parseDouble(passageiro.getLongitude())
            );

            LatLng localMotociclista = new LatLng(
                    Double.parseDouble(motociclista.getLatitude()),
                    Double.parseDouble(motociclista.getLongitude())
            );

            float distancia = Local.calcularDistancia(localMotociclista, localPassageiro);
            String distanciaFormatada = Local.formatarDistancia(distancia);
            myViewHolder.distancia.setText(distanciaFormatada + "aproximadamente.");
        } else {
            myViewHolder.distancia.setText("Recebendo localização...");
        }

    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, distancia;

        public MyViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }

    }

}
