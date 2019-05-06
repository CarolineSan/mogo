package com.example.app.mogo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.app.mogo.R;
import com.example.app.mogo.activity.motociclista.RequisicoesActivity;
import com.example.app.mogo.config.ConfiguracaoFirebase;
import com.example.app.mogo.helper.Local;
import com.example.app.mogo.helper.UsuarioFirebase;
import com.example.app.mogo.model.Destino;
import com.example.app.mogo.model.Requisicao;
import com.example.app.mogo.model.Usuario;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotociclista;
    private LatLng localPassageiro;
    private Usuario motociclista;
    private Usuario passageiro;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Button aceitarCorrida;
    private Marker marcadorMotociclista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private String statusRequisicao;
    private Boolean requisicaoAtiva;
    private FloatingActionButton fabRota;
    private Button cancelarCorrida;
    private Button finalizarCorrida;
    private Button iniciarCorrida;
    private Circle circulo;
    private Destino destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Inciar Corrida");

        //Inicializar componentes
        aceitarCorrida = findViewById(R.id.buttonAceitarCorrida);
        fabRota = findViewById(R.id.fabRota);
        cancelarCorrida = findViewById(R.id.buttonCancelarCorrida);
        finalizarCorrida = findViewById(R.id.buttonFinalizarCorrida);
        iniciarCorrida = findViewById(R.id.buttonIniciarCorrida);

        //Adiciona evento de clique no Fab Rota
        fabRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = statusRequisicao;
                if (status != null && !status.isEmpty()) {
                    String lat = "";
                    String lon = "";

                    switch (status) {
                        case Requisicao.STATUS_A_CAMINHO :
                            lat = String.valueOf(localPassageiro.latitude);
                            lon = String.valueOf(localPassageiro.longitude);
                            break;
                        case Requisicao.STATUS_EM_VIAGEM :
                            lat = destino.getLatitude();
                            lon = destino.getLongitude();
                            break;
                    }

                    //Abrir Rota
                    String latLong = lat + "," + lon;
                    Uri uri = Uri.parse("google.navigation:q=" + latLong + "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }

            }
        });

        //Configurações iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Inicializando Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Recupera dados do usuário
        if ( getIntent().getExtras().containsKey("idRequisicao")
                && getIntent().getExtras().containsKey("motociclista") ) {
            Bundle extras = getIntent().getExtras();
            motociclista = (Usuario) extras.getSerializable("motociclista");
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            verificaStatusRequisicao();
        }

    }

    private void verificaStatusRequisicao(){

        final DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Recuperar a requisição
                requisicao = dataSnapshot.getValue(Requisicao.class);
                if (requisicao != null) {

                    passageiro = requisicao.getPassageiro();
                    localPassageiro = new LatLng(
                            Double.parseDouble(passageiro.getLatitude()),
                            Double.parseDouble(passageiro.getLongitude())
                    );

                    statusRequisicao = requisicao.getStatus();
                    destino = requisicao.getDestino();
                    alteraInterfaceStatusRequisicao(statusRequisicao);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void requisicaoAguardando(){
        aceitarCorrida.setText("Aceitar Corrida");
        aceitarCorrida.setVisibility(View.VISIBLE);
        cancelarCorrida.setVisibility(View.GONE);
        finalizarCorrida.setVisibility(View.GONE);
        iniciarCorrida.setVisibility(View.GONE);
        fabRota.setVisibility(View.GONE);
        getSupportActionBar().setTitle("Iniciar Corrida");
    }

    private void requisicaoACaminho(){
        fabRota.setVisibility(View.VISIBLE);
        aceitarCorrida.setVisibility(View.GONE);
        finalizarCorrida.setVisibility(View.GONE);
        cancelarCorrida.setVisibility(View.VISIBLE);
        iniciarCorrida.setVisibility(View.GONE);
        String nomePassageiro = passageiro.getNome();
        getSupportActionBar().setTitle("Vá até " + nomePassageiro);

        //Inicia monitoramento do motociclista até o passageiro
        iniciarMonitoramento(passageiro, motociclista);

        //Adiciona área de proximidade do passageiro
        if (circulo == null) {
            circulo = mMap.addCircle(
                    new CircleOptions()
                            .center(localPassageiro)
                            .radius(50)
                            .fillColor(Color.argb(80,255,153,0))
                            .strokeColor(Color.argb(80, 255, 152, 0))
            );
        }

    }

    private void requisicaoEmViagem(){
        fabRota.setVisibility(View.VISIBLE);
        aceitarCorrida.setVisibility(View.GONE);
        cancelarCorrida.setVisibility(View.GONE);
        iniciarCorrida.setVisibility(View.GONE);
        finalizarCorrida.setVisibility(View.VISIBLE);

        //Exibir motociclista
        adicionaMarcadorMotociclista(localMotociclista, "Você");

        //Exibir Marcador de Destino
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

        getSupportActionBar().setTitle(distanciaFormatada + "até o Destino");

        adicionaMarcadorDestino(localDestino, destino.getRua());

        centralizarMarcadores(marcadorMotociclista, marcadorDestino);
        centralizarMarcadores(marcadorMotociclista, marcadorDestino);

        //Inicia monitoramento até o destino
        iniciarMonitoramentoCorrida(motociclista, localDestino);
    }

    private void requisicaoEmEspera(){

        fabRota.setVisibility(View.VISIBLE);
        aceitarCorrida.setVisibility(View.GONE);
        cancelarCorrida.setVisibility(View.GONE);
        iniciarCorrida.setVisibility(View.VISIBLE);
        finalizarCorrida.setVisibility(View.GONE);
        String nomePassageiro = passageiro.getNome();
        getSupportActionBar().setTitle("Encontre " + nomePassageiro);

    }

    private void iniciarMonitoramento(Usuario p, Usuario m){

        //Inicializa GeoFire
        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);


        final GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localPassageiro.latitude, localPassageiro.longitude),
                0.05 //em Km (0.05 = 50 metros)
        );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (key.equals(motociclista.getId())) {


                    requisicao.setStatus(Requisicao.STATUS_EM_ESPERA);
                    requisicao.atualizarStatus();

                    geoQuery.removeAllListeners();
                    circulo.remove();

                    /*
                    //Exibir motociclista
                    adicionaMarcadorMotociclista(localMotociclista, "Você");

                    //Exibir Marcador de Destino
                    LatLng localDestino = new LatLng(
                            Double.parseDouble(destino.getLatitude()),
                            Double.parseDouble(destino.getLongitude())
                    );

                    adicionaMarcadorDestino(localDestino, destino.getRua());

                    centralizarMarcadores(marcadorMotociclista, marcadorDestino);
                     */


                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void iniciarMonitoramentoCorrida(final Usuario uOrigem, LatLng localDestino){

        //Inicializa GeoFire
        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        final GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localDestino.latitude, localDestino.longitude),
                0.05 //em Km (0.1 = 100 metros)
        );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (key.equals(uOrigem.getId())) {
                    //Log.d("onKeyEntered", "onKeyEntered: motociclista está dentro da área");

                    geoQuery.removeAllListeners();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void alteraInterfaceStatusRequisicao(String status) {

        if (status != null && !status.isEmpty()) {
            switch (status) {
                case Requisicao.STATUS_AGUARDANDO:
                    requisicaoAguardando();
                    requisicaoAtiva = false;
                    break;
                case Requisicao.STATUS_A_CAMINHO:
                    requisicaoACaminho();
                    break;
                case Requisicao.STATUS_EM_ESPERA:
                    requisicaoEmEspera();
                    break;
                case Requisicao.STATUS_EM_VIAGEM:
                    requisicaoEmViagem();
                    break;
            }
        }

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
                localMotociclista = new LatLng(latitude, longitude);

                //Atualizar o GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                //Atualizar Localização motociclista no Firebase
                motociclista.setLatitudeAtual(String.valueOf(latitude));
                motociclista.setLongitudeAtual(String.valueOf(longitude));
                requisicao.setMotociclista(motociclista);
                requisicao.atualizarLocalizacaoMotociclista();

                alteraInterfaceStatusRequisicao(statusRequisicao);

                if ( !statusRequisicao.equals(Requisicao.STATUS_EM_VIAGEM) ) {


                    if (statusRequisicao.equals(Requisicao.STATUS_FINALIZADA) || statusRequisicao.equals(Requisicao.STATUS_ENCERRADA)){

                        //Exibir motociclista
                        adicionaMarcadorMotociclista(localMotociclista, "Você");

                        // Float só pode variar entre 2.0 a 21.0
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(localMotociclista, 17)
                        );
                    } else {
                        //Exibir motociclista
                        adicionaMarcadorMotociclista(localMotociclista, "Você");

                        //Exibir passageiro
                        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

                        //Centralizar marcadores
                        centralizarMarcadores(marcadorMotociclista, marcadorPassageiro);
                    }


                } else {
                    //Exibir motociclista
                    adicionaMarcadorMotociclista(localMotociclista, "Você");

                    //Exibir Marcador de Destino
                    LatLng localDestino = new LatLng(
                            Double.parseDouble(destino.getLatitude()),
                            Double.parseDouble(destino.getLongitude())
                    );

                    adicionaMarcadorDestino(localDestino, destino.getRua());

                    centralizarMarcadores(marcadorMotociclista, marcadorDestino);
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

    public void aceitarCorrida(View view) {

        //Configura requisição
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotociclista(motociclista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();

        //Adiciona área de proximidade do passageiro
        circulo = mMap.addCircle(
                new CircleOptions()
                        .center(localPassageiro)
                        .radius(50)
                        .fillColor(Color.argb(80,255,153,0))
                        .strokeColor(Color.argb(80, 255, 152, 0))
        );

        requisicaoAtiva = true;

    }

    public void cancelarCorrida(View view) {

        //Configura requisição
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.atualizarStatus();
        circulo.remove();
        requisicaoAtiva = false;

    }

    public void iniciarViagem(View view) {
        //Altera Status da Requisição
        requisicao.setStatus(Requisicao.STATUS_EM_VIAGEM);
        requisicao.atualizarStatus();
    }

    public void finalizarCorrida(View view) {

        //Calcular Distancia
        float distancia = Local.calcularDistancia(localPassageiro, localMotociclista);
        float valor = distancia * 4;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        final String resultado = decimalFormat.format(valor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Finalizar Viagem?")
                .setMessage("O valor da corrida é:\n" +
                        "R$ " + resultado)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Configura requisição
                        requisicao.setStatus(Requisicao.STATUS_FINALIZADA);
                        requisicao.atualizarStatus();

                        fabRota.setVisibility(View.GONE);
                        finalizarCorrida.setVisibility(View.GONE);

                        if (marcadorDestino != null) {
                            marcadorDestino.remove();
                        }

                        getSupportActionBar().setTitle("Viagem Finalizada - R$ " + resultado);
                        requisicaoAtiva = false;

                        requisicao.setValor(resultado);
                        requisicao.atualizarValor();

                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (requisicaoAtiva) {
            Toast.makeText(CorridaActivity.this,
                    "Você precisa encerrar a corrida atual!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent back = new Intent(this, RequisicoesActivity.class);
            startActivity(back);
        }

        if (statusRequisicao != null && !statusRequisicao.isEmpty() && !statusRequisicao.equals(Requisicao.STATUS_AGUARDANDO)) {
            requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
            requisicao.atualizarStatus();
        }
        return false;
    }
}
