package com.example.app.mogo.activity.motociclista;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.app.mogo.R;
import com.example.app.mogo.activity.CorridaActivity;
import com.example.app.mogo.adapter.RequisicoesAdapter;
import com.example.app.mogo.config.ConfiguracaoFirebase;
import com.example.app.mogo.helper.RecyclerItemClickListener;
import com.example.app.mogo.helper.UsuarioFirebase;
import com.example.app.mogo.model.Requisicao;
import com.example.app.mogo.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private DatabaseReference firebaseRef;

    private RecyclerView recyclerRequisicoes;
    private TextView textSemChamados;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motociclista;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        motociclista = UsuarioFirebase.getDadosUsuarioLogado();

        //Inicializando Componentes
        recyclerRequisicoes = findViewById(R.id.recyclerRequisicoes);
        textSemChamados = findViewById(R.id.textSemChamados);

        //Configurando o RecyclerView
        adapter = new RequisicoesAdapter(listaRequisicoes, getApplicationContext(), motociclista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerRequisicoes.setLayoutManager(layoutManager);
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(adapter);

        //Adiciona evento de clique no recycle
        recyclerRequisicoes.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerRequisicoes,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Requisicao requisicao = listaRequisicoes.get(position);
                                abrirTelaCorrida(requisicao.getId(), motociclista, false);

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                ));

        recuperarRequisicoes();

        //Recuperar Localização
        recuperarLocalizacaoUsuario();

    }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicoesPesquisa = requisicoes.orderByChild("motociclista/id")
                .equalTo(usuarioLogado.getId());

        requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for ( DataSnapshot ds: dataSnapshot.getChildren() ) {

                    Requisicao requisicao = ds.getValue(Requisicao.class);

                    if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO) || requisicao.getStatus().equals(Requisicao.STATUS_EM_VIAGEM) || requisicao.getStatus().equals(Requisicao.STATUS_EM_ESPERA)) {

                        motociclista = requisicao.getMotociclista();
                        abrirTelaCorrida(requisicao.getId(), motociclista, true);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void abrirTelaCorrida(String idRequisicao, Usuario motociclista, boolean requisicaoAtiva){
        Intent i = new Intent(RequisicoesActivity.this, CorridaActivity.class);
        i.putExtra("idRequisicao", idRequisicao);
        i.putExtra("motociclista", motociclista);
        i.putExtra("requisicaoAtiva", requisicaoAtiva);
        startActivity(i);
    }

    private void recuperarRequisicoes() {
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0){
                    textSemChamados.setVisibility(View.GONE);
                    recyclerRequisicoes.setVisibility(View.VISIBLE);
                } else {
                    textSemChamados.setVisibility(View.VISIBLE);
                    recyclerRequisicoes.setVisibility(View.GONE);
                }

                listaRequisicoes.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    listaRequisicoes.add(requisicao);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //Recuperar Latitude e Longitude
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                //Atualizar o GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(location.getLatitude(), location.getLongitude());

                if (!latitude.isEmpty() && !longitude.isEmpty()){
                    motociclista.setLongitude(longitude);
                    motociclista.setLatitude(latitude);
                    locationManager.removeUpdates(locationListener);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_secundario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sair) {
            autenticacao.signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
