package com.example.seccion_08;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.seccion_08.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private String proveedor_ubicacion;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    public static boolean peticion_gsp_realizada;
    public static boolean peticion_gsp_contestada;
    public Geocoder geocoder;//para saber la dirección de unas coordenadas


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //INICIO
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//Para manejar la ubcación del dispositivo
        proveedor_ubicacion = LocationManager.GPS_PROVIDER;//para decir que quiero usar el GPS, podría ser NETWORK
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        peticion_gsp_realizada = false;
        peticion_gsp_contestada = false;
        geocoder = new Geocoder(this, new Locale("es"));


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        Log.d("MIAPP", "VUELTA DE PEDIR PERMISOS");

        if (requestCode == 100) {
            //
            if ((grantResults != null) && (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                Log.d("MIAPP", "PERMISO DE LOCALIZACIÓN FINA CONCECIDO en ejecución");

                if (locationManager.isProviderEnabled(proveedor_ubicacion)) //tienes el GPS activado?
                { //sí
                    Log.d("MIAPP", "EL Acceso fino por GPS está habilitado");
                    mostrarUltimaUbicacionConocida();
                    programarPeticionesPeriodicas();

                } else {
                    Log.d("MIAPP", "Pidiendo que habilite el acceso");
                    peticion_gsp_realizada = true;
                    solicitarActivarLocalizacion();


                }

            } else {

                Log.d("MIAPP", "PERMISO DE LOCALIZACIÓN DENEGADO en ejecución");
                Toast.makeText(this, "CHAMPION, sin PERMISO localización no se puede seguir. BYE", Toast.LENGTH_LONG);
                finish();

            }
        }


    }

    private void mostrarUltimaUbicacionConocida() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d("MIAPP", "Location inicial");
                            mostrarLocalizacion(location);
                        }
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dalias = new LatLng(-2.869271, 36.821518);
        mMap.addMarker(new MarkerOptions().position(dalias).title("Hola desde Dalias"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dalias));
    }

    private void solicitarActivarLocalizacion() {

        FragmentManager fm = getSupportFragmentManager();
        DialogoGPS dialogo = new DialogoGPS();
        dialogo.show(fm, "Aviso");


    }


    private void programarPeticionesPeriodicas() {
        LocationRequest lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//precisión alta
        lr.setInterval(5000);//quiero informarme cada 5 segundos


        this.locationCallback = new LocationCallback() { //este este método será invocado cada vez que se actualice la ubcación
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {

                }
                for (Location location : locationResult.getLocations()) {
                    mostrarLocalizacion(location);
                    Log.d("MIAPP", "onLocationResult Actualizando");

                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(lr, this.locationCallback, null);
    }




    @Override
    protected void onResume() {
        super.onResume();

        Log.d("MIAPP", "La aplicación entra (o vuelve a entrar) a primer plano, ACTIVO");

        if (peticion_gsp_realizada && peticion_gsp_contestada)//CASO SÚPER ESPECIAL: para evitar que nos pregunte dos veces, tenemos que controlar que ya ha respondido. si no, entre que aparece el diálogo y lo contesta, se cuela por aquí y pregunta de nuevo
        {
            if (locationManager.isProviderEnabled(proveedor_ubicacion))
            {
                Log.d("MIAPP", "Programo peticiones periódicas");
                programarPeticionesPeriodicas();
            }
            else {

                Log.d("MIAPP", "Ya fue solicitada la activación, pero fue denegada, pido de nuevo");
                solicitarActivarLocalizacion();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("MIAPP", "La aplicación entra en pausa (deja de estar visible), paro de solicitar actualizaciones para optimizar");

        try {

        } catch (SecurityException se) {
            Log.e("MIAPP", "Sin permisos");
        }
    }


    public void mostrarLocalizacion(Location location) {
        double lat = 0;
        double lng = 0;
        double alt = 0;

        Log.d("MIAPP", "mostrarLocalizacion");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateformatter = new SimpleDateFormat("E dd/MM/yyyy 'a las' hh:mm:ss");


        if (null != location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            alt = location.getAltitude();
            LatLng posicion_nueva = new LatLng(location.getLatitude(), location.getLongitude());


            Log.d("MIAPP", "LATITUD = " + lat);
            Log.d("MIAPP", "LONGITUD = " + lng);
            Log.d("MIAPP", "ALTITUD = " + alt);
            Log.d("MIAPP", "Momento " + dateformatter.format(calendar.getTime()));

            Log.d("MIAPP", "Proveedor = " + location.getProvider());

            mMap.addMarker(new MarkerOptions().position(posicion_nueva).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion_nueva, 13));//nivel de zoom 13

            try{
                List<Address> direcciones = geocoder.getFromLocation(lat, lng, 1);
                if (direcciones!=null && direcciones.size()>0)
                {
                    Log.d("MIAPP", "Al menos tengo un resultado");
                    Address direccion = direcciones.get(0);
                    Log.d("MIAPP", "Dirección = " + direccion.getAddressLine(0) + " CP " + direccion.getPostalCode() + " Localidad " + direccion.getLocality());
                }


            }catch (Exception e)
            {
                Log.e("MIAPP", "Error obteniendo dirección", e);
            }


        } else {


            Log.d("MIAPP", "LOCALIZACIÓN null/desconocida ");
        }


    }
}