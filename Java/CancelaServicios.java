package com.example.reservaya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CancelaServicios extends AppCompatActivity {

    //Variables
    private EditText Correo;
    private Button botonCancelarReserva;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Date fechaReserva;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancela_servicios);
        this.setTitle("Cancela reserva"); //Cambio el título de mi aplicación

        Correo=findViewById(R.id.editTextCorreo);
        botonCancelarReserva=findViewById(R.id.buttonCancelarReserva);

        //instancia de FirebaseAuth para realizar operaciones de autenticación de usuarios en Firebase
        mAuth = FirebaseAuth.getInstance();

        //Obtengo la referencia de la base de datos
        db = FirebaseFirestore.getInstance();

        // Llamar al método de inicio de sesión para entrar con mi usuario
        iniciarSesion("juillemellamo@gmail.com", "Tx_vnvmKJUZ:Q6x");

        //llamada al método iniciar sesión cuando se pulsa el botón
        botonCancelarReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verifica que no esté el campo vacío, si no lo está llama al método verificarCorreo()
                String correito=Correo.getText().toString();
                if (correito.isEmpty()) {
                    Toast.makeText(CancelaServicios.this, "Por favor, introduzca un email", Toast.LENGTH_SHORT).show();
                } else{
                    verificarCorreoExistente();
                }
            }
        });
    }

    //método para verificar el inicio de sesión solo con los usuarios creados para ello en firestore
    private void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            //si el inicio de sesión ha sido exitoso, llamo al siguiente método
                        } else {
                            // Error en el inicio de sesión
                            Toast.makeText(CancelaServicios.this, "Error en el inicio de sesión",
                                    Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                    }
                });
    }

    private void verificarCorreoExistente() {
        CollectionReference reservasRef = db.collection("Reservas");
        String correito= Correo.getText().toString().trim();


        Query query = reservasRef.whereEqualTo("email", correito);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) { //Verifica que el campo no está vacío
                        // El correo existe en la base de datos
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Obteniene el primer documento con esas características

                        //Obtengo los datos fecha y hora del documento en mi base de datos, están en formato String
                        String fecha= document.getString("fecha");
                        String hora= document.getString("hora");

                        //Creo los formatos de fecha y hora para convertir las cadenas en objetos Date
                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                        try {
                            // Parseo las cadenas de fecha y hora a objetos Date
                            Date fechaReserva = formato.parse(fecha);
                            Date horaReserva = timeFormat.parse(hora);

                            // Configuración del calendario de la reserva
                            Calendar reservaCalendar = Calendar.getInstance();
                            reservaCalendar.setTime(fechaReserva);
                            reservaCalendar.set(Calendar.HOUR_OF_DAY, horaReserva.getHours());
                            reservaCalendar.set(Calendar.MINUTE, horaReserva.getMinutes());

                            //Configuración del Calendario en el momento de realizar esta operación
                            Calendar actualCalendar = Calendar.getInstance();

                            //Calculo la diferencia de milisegundos entre la fecha de la reserva y la fecha actual al realizar esta operación
                            long diffMillis = reservaCalendar.getTimeInMillis() - actualCalendar.getTimeInMillis();

                            /*calcula las horas restantes dividiendo los milisegundos entre el número de milisegundos en una hora
                            teniendo en cuenta que hay 60 seg x min y 60 min x hora, de esta manera saco el número de horas restante y
                            multiplico también por 1000 ya que son milisegundo y no segundos*/
                            long horasRestantes = diffMillis / (60 * 60 * 1000); // Convertir de milisegundos a horas

                            if (horasRestantes >= 24) {
                                // Se puede cancelar la reserva
                                document.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //si no hay ningún prblema, borra la reserva de la base de datos
                                            Toast.makeText(CancelaServicios.this, "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CancelaServicios.this, MainActivity.class); // Lo envío a la pantalla principal
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(CancelaServicios.this, "Error al cancelar la reserva", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // No se puede cancelar la reserva
                                Toast.makeText(CancelaServicios.this, "No se puede cancelar la reserva, faltan menos de 24 horas", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            // Error al castear las cadenas a objetos Date
                            Toast.makeText(CancelaServicios.this, "Error al castear las fechas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // El correo no existe en la base de datos
                        Toast.makeText(CancelaServicios.this, "No existe ninguna reserva con ese correo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error al obtener los documentos
                    Toast.makeText(CancelaServicios.this, "Error al verificar el correo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}