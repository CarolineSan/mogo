package com.corp.app.mogo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.corp.app.mogo.helper.Permissoes;
import com.corp.app.mogo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Objeto responsável por gerenciar a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                mMap.clear();
                final LatLng localUsuario = new LatLng(latitude, longitude);


                //Adiciona marcador do endereço
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localUsuario)
                                .title("Minha Localização")
                                .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_YELLOW
                                        )
                                )
                );

                // Adicionando evento de clique
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.add(localUsuario);
                        polylineOptions.add(latLng);
                        polylineOptions.width(5);
                        mMap.addPolyline(polylineOptions);

                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(latLng)
                                        .title("Mototaxista")
                                        .snippet("XVN-1068")
                                        .icon(
                                                BitmapDescriptorFactory.fromResource(R.drawable.mototaxi_40px_edit)
                                        )
                        );
                    }
                });

                // Classifica o Zoom Inicial - Float só pode variar entre 2.0 a 21.0
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localUsuario, 16)
                );

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    //List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1);
                    String stringEndereco = "Rua Direta de Tancredo Neves - Tancredo Neves, Salvador - BA";
                    List<Address> listaEndereco = geocoder.getFromLocationName(stringEndereco, 1);
                    if (listaEndereco != null && listaEndereco.size() > 0) {
                        Address endereco = listaEndereco.get(0);

                        /* onLocationChanged:
                        Address[addressLines=[0:"Rua armando de souza, 02 - Tancredo Neves, Salvador - BA, 41205-410, Brazil"
                        feature=02,
                        admin=Bahia,
                        sub-admin=Salvador,
                        locality=Salvador,
                        thoroughfare=Rua armando de souza,
                        postalCode=41205-410,
                        countryCode=BR,
                        countryName=Brazil,
                        hasLatitude=true,latitude=-12.9422112,
                        hasLongitude=true,longitude=-38.4495556,
                        phone=null,
                        url=null,
                        extras=null]] */

                        Log.d("local", "onLocationChanged: " + endereco.getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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

        //Recuperando Localização do Usuário
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener

            );
        }


        /* CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(tancredo);
        circleOptions.radius(1000); //em metros
        circleOptions.fillColor(Color.argb(50, 255,153,0));
        circleOptions.strokeWidth(0);

        mMap.addCircle(circleOptions); */







    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta
                alertaValidacaoPermissao();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recuperando Localização do Usuário
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener

                    );
                }
            }
        }

    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
