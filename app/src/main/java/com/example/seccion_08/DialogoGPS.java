package com.example.seccion_08;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

public class DialogoGPS  extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

//LANZAMOS UN AVISO PARA PERSUDIR AL USUARIO
// DE OTRO MODO LE APARECE LA ACTIVIDAD DE AJUSTES A SACO
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Active el permiso de localizacion. La app lo necesita ")
                .setTitle("Ajustes Localización ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent viewIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);//la petición va destinada al grupo
                        //startActivity(viewIntent); //
                        getActivity().startActivityForResult(viewIntent, 500);// Settings.ACTION_LOCATION_SOURCE_SETTINGS no devuelve ningún resultcode de acorde la selección del usuario: solución: chequear de nuevo en OnResume si el gps está activo :S
                        dialog.cancel();
                        MapsActivity.peticion_gsp_contestada = true;//CASO ESPECIAL
                    }
                });
        return builder.create();
    }

    public void show(FragmentManager fm, String aviso) {
    }
}
