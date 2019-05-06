package com.corp.app.mogo.activity.passageiro;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.corp.app.mogo.R;
import com.corp.app.mogo.config.ConfiguracaoFirebase;
import com.corp.app.mogo.helper.Local;
import com.corp.app.mogo.helper.UsuarioFirebase;
import com.corp.app.mogo.model.Destino;
import com.corp.app.mogo.model.Requisicao;
import com.corp.app.mogo.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    /*
    Lat/lon destino: -12.946035, -38.445240 (R. Pernambuco, 254 - Tancredo Neves, 41205-140)
    -12.961948, -38.404653
    -12.962240, -38.403398
    -12.960024, -38.401874
    -12.958811, -38.401606
    Encerramento intermediário: -12.945879, -38.445508
    Lat/lon passageiro: -12.942633, -38.449493
    Lat/lon Motociclista (a caminho);
        inicial: -12.942936, -38.456247
        intermediário: -12.940209, -38.451104
        final: -12.942503, -38.449576
     */

    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private EditText editDestino;
    private LatLng localUsuario;
    NavigationView viewHeaderBar;
    private TextView nomeUsuario;
    private TextView emailUsuario;
    private LinearLayout linearLayoutDestino;
    private FloatingActionButton chamarMogoButton;
    private LinearLayout linearLayoutLoad;
    private LinearLayout linearLayoutStatus;
    private Button cancelarButton;
    private TextView aguardandoText;
    private ImageView loading;
    AnimationDrawable animationLoading;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;
    private Usuario passageiro;
    private String statusRequisicao;
    private Destino destino;
    private Marker marcadorMotociclista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private Usuario motociclista;
    private LatLng localMotociclista;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chamarMogo();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Inicializando Componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        linearLayoutLoad = findViewById(R.id.linearLayoutLoad);
        linearLayoutStatus = findViewById(R.id.linearLayoutStatus);
        chamarMogoButton = findViewById(R.id.fab);
        loading = findViewById(R.id.loadingView);
        cancelarButton = findViewById(R.id.cancelarButton);
        aguardandoText = findViewById(R.id.aguardandoText);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Configurado Dados Usuário Logado
        Usuario usuarioAtual = UsuarioFirebase.getDadosUsuarioLogado();
        String nome = usuarioAtual.getNome();
        String email = usuarioAtual.getEmail();
        viewHeaderBar = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        nomeUsuario = headerView.findViewById(R.id.nomeMenuId);
        nomeUsuario.setText(nome);
        emailUsuario = headerView.findViewById(R.id.emailMenuId);
        emailUsuario.setText(email);


        //Inicializando Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Adiciona listener para status da requisição
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuarioLogado.getId());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    lista.add(ds.getValue(Requisicao.class));
                }
                Collections.reverse(lista);
                if (lista != null && lista.size() > 0) {
                    requisicao = lista.get(0);

                    if (requisicao != null) {
                        if (!requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADA)) {

                            passageiro = requisicao.getPassageiro();
                        localUsuario = new LatLng(
                                Double.parseDouble(passageiro.getLatitude()),
                                Double.parseDouble(passageiro.getLongitude())
                        );

                        if (requisicao.getMotociclista() != null) {
                            motociclista = requisicao.getMotociclista();
                            localMotociclista = new LatLng(
                                    Double.parseDouble(motociclista.getLatitudeAtual()),
                                    Double.parseDouble(motociclista.getLongitudeAtual())
                            );
                        }

                        statusRequisicao = requisicao.getStatus();
                        destino = requisicao.getDestino();
                        alteraInterfaceStatusRequisicao(statusRequisicao);
                       }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void alteraInterfaceStatusRequisicao(String status){

        if (status != null && !status.isEmpty()) {
            switch (status) {
                case Requisicao.STATUS_AGUARDANDO:
                    requisicaoAguardando();
                    break;
                case Requisicao.STATUS_A_CAMINHO :
                    requisicaoACaminho();
                    break;
                case Requisicao.STATUS_EM_ESPERA :
                    requisicaoEmEspera();
                    break;
                case Requisicao.STATUS_EM_VIAGEM :
                    requisicaoEmViagem();
                    break;
                case Requisicao.STATUS_FINALIZADA :
                    requisicaoFinalizada();
                    break;
            }
        }
    }

    private void requisicaoAguardando(){
        getSupportActionBar().setTitle("");
        chamarMogoButton.hide();
        linearLayoutDestino.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.VISIBLE);
        linearLayoutStatus.setVisibility(View.VISIBLE);
        aguardandoText.setText("Chamando Mogo...");
        animationLoading = (AnimationDrawable)loading.getDrawable();
        animationLoading.start();

        if (marcadorMotociclista != null)
            marcadorMotociclista.remove();

        adicionaMarcadorPassageiro(localUsuario, "Você");

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localUsuario, 17)
        );
    }

    private void requisicaoACaminho(){
        LatLng localAtual = new LatLng(
                Double.parseDouble(motociclista.getLatitudeAtual()),
                Double.parseDouble(motociclista.getLongitudeAtual())
        );

        float distancia = Local.calcularDistancia(localAtual, localUsuario);
        String distanciaFormatada = Local.formatarDistancia(distancia);
        getSupportActionBar().setTitle(distanciaFormatada + "de distância");
        chamarMogoButton.hide();
        linearLayoutDestino.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.GONE);
        linearLayoutStatus.setVisibility(View.VISIBLE);
        String nomeMotociclista = motociclista.getNome();
        String textoACaminho = nomeMotociclista + " está a caminho";
        aguardandoText.setText(textoACaminho);

        adicionaMarcadorPassageiro(localUsuario, "Você");
        adicionaMarcadorMotociclista(localMotociclista, motociclista.getNome());
        centralizarMarcadores(marcadorMotociclista, marcadorPassageiro);
    }

    private void requisicaoEmEspera(){
        getSupportActionBar().setTitle("Seu Mogo chegou!");
        chamarMogoButton.hide();
        linearLayoutDestino.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.GONE);
        linearLayoutStatus.setVisibility(View.VISIBLE);
        String nomeMotociclista = motociclista.getNome();
        aguardandoText.setText("Encontre " + nomeMotociclista);

        adicionaMarcadorPassageiro(localUsuario, "Você");
        adicionaMarcadorMotociclista(localMotociclista, motociclista.getNome());
        centralizarMarcadores(marcadorMotociclista, marcadorPassageiro);
    }


    private void requisicaoEmViagem(){

        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );

        LatLng localAtual = new LatLng(
                Double.parseDouble(motociclista.getLatitudeAtual()),
                Double.parseDouble(motociclista.getLongitudeAtual())
        );

        float distancia = Local.calcularDistancia(localAtual, localDestino);
        String distanciaFormatada = Local.formatarDistancia(distancia);
        getSupportActionBar().setTitle("Em Viagem");
        chamarMogoButton.hide();
        linearLayoutDestino.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.GONE);
        linearLayoutStatus.setVisibility(View.VISIBLE);
        aguardandoText.setText("A caminho do Destino - " + distanciaFormatada);

        adicionaMarcadorMotociclista(localMotociclista, "Você");
        adicionaMarcadorDestino(localDestino, destino.getRua());
        centralizarMarcadores(marcadorMotociclista, marcadorDestino);
    }

    private void requisicaoFinalizada(){
        //Calcular Distancia
        float distancia = Local.calcularDistancia(localUsuario, localMotociclista);
        float valor = distancia * 4;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        final String resultado = decimalFormat.format(valor);

        getSupportActionBar().setTitle("Chegou ao Destino");
        chamarMogoButton.hide();
        linearLayoutDestino.setVisibility(View.GONE);
        linearLayoutLoad.setVisibility(View.GONE);
        linearLayoutStatus.setVisibility(View.VISIBLE);
        aguardandoText.setText("Viagem Finalizada - Valor: R$ " + resultado);

        if (marcadorDestino != null)
            marcadorDestino.remove();

        adicionaMarcadorPassageiro(localUsuario, "Você");

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localUsuario, 17)
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Total da Corrida")
                .setMessage("Sua viagem ficou: R$ " + resultado)
                .setCancelable(false)
                .setNegativeButton("Encerrar Viagem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                        requisicao.atualizarStatus();

                        finish();
                        startActivity(new Intent(getIntent()));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void adicionaMarcadorMotociclista(LatLng localizacao, String titulo){

        if (marcadorMotociclista != null)
            marcadorMotociclista.remove();

        marcadorMotociclista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.mototaxi_40px_edit)
                        )
        );

    }

    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo){

        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_YELLOW
                                )
                        )
        );
    }

    private void adicionaMarcadorDestino(LatLng localizacao, String titulo){

        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();

        if (marcadorDestino != null)
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.location)
                        )
        );
    }

    private void centralizarMarcadores(Marker marcadorM, Marker marcadorP){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marcadorM.getPosition());
        builder.include(marcadorP.getPosition());

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacointerno = (int) (largura * 0.30);


        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacointerno)
        );

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
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
    } */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_conta) {
            startActivity(new Intent(this, ContaActivity.class));
        } else if (id == R.id.nav_fav) {

        } else if (id == R.id.nav_pagamento) {

        } else if (id == R.id.nav_corridas) {
            startActivity(new Intent(this, HistoricoActivity.class));
        } else if (id == R.id.nav_faq) {
            startActivity(new Intent(this, AjudaActivity.class));
        } else if (id == R.id.nav_termos) {
            startActivity(new Intent(this, RegulamentoActivity.class));
        } else if (id == R.id.nav_out) {
            autenticacao.signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Recuperar Localização
        recuperarLocalizacaoUsuario();

    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //Recuperar Latitude e Longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                localUsuario = new LatLng(latitude, longitude);

                //Atualizar o GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);


                //Adiciona marcador do endereço
                adicionaMarcadorPassageiro(localUsuario, "Você");

                // Classifica o Zoom Inicial - Float só pode variar entre 2.0 a 21.0
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localUsuario, 17)
                );

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
                    10000,
                    10,
                    locationListener
            );
        }

    }

    public void chamarMogo(){

        //Início
        String enderecoDestino = editDestino.getText().toString();
        if (!enderecoDestino.equals("") || enderecoDestino != null) {

            Address addressDestino = recuperarEndereco(enderecoDestino);
            if (addressDestino != null) {

                final Destino destino = new Destino();
                destino.setEstado(addressDestino.getAdminArea());
                destino.setCep(addressDestino.getPostalCode());
                destino.setBairro(addressDestino.getSubLocality());
                destino.setRua(addressDestino.getThoroughfare());
                destino.setNumero(addressDestino.getFeatureName());
                destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                StringBuilder mensagem = new StringBuilder();
                mensagem.append(destino.getRua());
                mensagem.append(", " + destino.getBairro());
                mensagem.append(", " + destino.getNumero());
                mensagem.append(", " + destino.getEstado());
                mensagem.append(", " + destino.getCep());

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Confirme seu destino:")
                        .setMessage(mensagem)
                        .setPositiveButton("Está correto!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Salvar Requisição
                                salvarRequisicao(destino);

                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }

        } else {
            Toast.makeText(this, "Informe o endereço de destino", Toast.LENGTH_SHORT).show();
        }
        //Fim
    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if (listaEnderecos != null && listaEnderecos.size() > 0) {
                Address address = listaEnderecos.get(0);

                return address;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioPassageiro.setLatitude(String.valueOf(localUsuario.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localUsuario.longitude));

        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

    }

    public void cancelarMogo(View view) {

        chamarMogoButton.show();
        linearLayoutDestino.setVisibility(View.VISIBLE);
        linearLayoutLoad.setVisibility(View.GONE);
        linearLayoutStatus.setVisibility(View.GONE);
        animationLoading.stop();
        getSupportActionBar().setTitle("Iniciar uma viagem");

    }

}
