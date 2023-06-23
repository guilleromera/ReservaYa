package com.example.reservaya;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Creación de atributos
    private Spinner sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Principal"); //Cambio el título principal de la pantalla

        sp=findViewById(R.id.sp); //conecto el spinner

        //método de llamada a configurar Spinner
        configurarSpinner();
    }

    private void configurarSpinner() {
        Spinner sp = findViewById(R.id.sp); // Conecto el spinner

        // Creo un Adapter al que le paso la lista de valores del array creado para los servicios
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opciones_spinner, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        sp.setAdapter(adapter);

        // Listener que espera a que una opción sea seleccionada
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int opc = sp.getSelectedItemPosition();
                if (opc == 1) { // Si la opción escogida es distinta a la que está por defecto, entra
                    Intent intent = new Intent(MainActivity.this, ContrataServicios.class); // Lo envío a ContratarServicios
                    startActivity(intent);
                }
                else if (opc == 2) { // Si la opción escogida es distinta a la que está por defecto, entra
                    Intent intent = new Intent(MainActivity.this, CancelaServicios.class); // Lo envío a ContratarServicios
                    startActivity(intent);
                }
                else if (opc == 3) { // Si la opción escogida es distinta a la que está por defecto, entra
                    Intent intent = new Intent(MainActivity.this, ModificaServicios.class); // Lo envío a ContratarServicios
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No se ha seleccionado ninguna opción
            }
        });
    }
}