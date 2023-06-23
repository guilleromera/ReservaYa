package com.example.reservaya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ContrataServicios extends AppCompatActivity {


    //Creación de mis variables
    private EditText editTextNombre,editTextCorreo,editTextFecha,editTextHora;
    private Button botonGuardar;
    private Spinner spinnerNumComensales, spinnerPrimerPlato,spinnerSegundoPlato,spinnerPostre;
    private FirebaseFirestore db;

    //utilizo estas variables para guardar el número de comensales, primer plato, segundo y postre
    private int numComensales;
    private String Primero,Segundo,Postre;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrata_servicios);
        this.setTitle("Contrata Servicios");

        //Asocia las variables del XML a mi archivo Java
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextCorreo = findViewById(R.id.editTextCorreo);
        editTextFecha = findViewById(R.id.editTextFecha);
        editTextHora = findViewById(R.id.editTextHora);
        botonGuardar = findViewById(R.id.buttonRealizarReserva);

        //Configuración de todos los Spinner
        spinnerNumComensales = findViewById(R.id.spinnerNumComensales);
        spinnerPrimerPlato=findViewById(R.id.spinnerPrimerPlato);
        spinnerSegundoPlato=findViewById(R.id.spinnerSegundoPlato);
        spinnerPostre=findViewById(R.id.spinnerPostre);

        //instancia de FirebaseAuth para realizar operaciones de autenticación de usuarios en Firebase
        mAuth = FirebaseAuth.getInstance();

        // Método para el escoger nº de comensales en la reserva
        configurarSpinnerNumComensales();

        // Método para el escoger el Primer Plato
        configurarSpinnerPrimerPlato();

        //método para escoger el segundo plato
        configurarSpinnerSegundoPlato();

        //método para escoger el postre
        configurarSpinnerPostre();

        //Obtengo la referencia de la base de datos
        db = FirebaseFirestore.getInstance();

        // Configura el OnClickListener para mostrar el DatePicker cuando se haga clic en el EditText de fecha
        editTextFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });

        // Configura el OnClickListener para mostrar el TimePicker cuando se haga clic en el EditText de la hora
        editTextHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarTimePicker();
            }
        });

        // Llamada al método que se acciona cuando pulso el botón
        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al método de inicio de sesión
                iniciarSesion("juillemellamo@gmail.com", "Tx_vnvmKJUZ:Q6x");

                //obtenerTotalComensalesParaFecha();
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
                            obtenerTotalComensalesParaFecha();
                        } else {
                            // Error en el inicio de sesión
                            Toast.makeText(ContrataServicios.this, "Error en el inicio de sesión",
                                    Toast.LENGTH_SHORT).show();
                            finish(); //finalizo la activity o quizá lo mande a Activity de inicio
                        }
                    }
                });
    }

    private void mostrarDatePicker() {
        // Obtiene la fecha actual para establecerla como fecha predeterminada en el DatePicker
        Calendar calendar = Calendar.getInstance();
        int año = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        // Crea un DatePickerDialog con la fecha actual
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int año, int mes, int dia) {
                // Actualiza el texto del EditText con la fecha seleccionada
                String fechaSeleccionada = dia + "/" + (mes + 1) + "/" + año;
                editTextFecha.setText(fechaSeleccionada);
            }
        }, año, mes, dia);

        // Muestra el DatePickerDialog
        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        // Obtengo la hora actual para establecerla predeterminada
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);

        // Crea un TimePickerDialog con la hora actual
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int horita, int minute) {
                // Actualiza el texto del EditText de la hora con la hora seleccionada
                String horaSeleccionada = horita + ":" + minute;
                editTextHora.setText(horaSeleccionada);
            }
        }, hora, minuto, true); // El último parámetro hace referencia al formato el formato de 24 horas

        // Muestra el TimePickerDialog
        timePickerDialog.show();
    }

    //método que configura el spinner para el número de comensales, MIN 1 y MAX 15 por reserva
    private void configurarSpinnerNumComensales() {
        // Crea un ArrayAdapter utilizando el array definido en strings.xml
        ArrayAdapter<CharSequence> numComensalesList = ArrayAdapter.createFromResource(this, R.array.num_comensales_array, android.R.layout.simple_spinner_dropdown_item);

        // Asigna el ArrayAdapter al Spinner
        spinnerNumComensales.setAdapter(numComensalesList);

        // Configura el valor máximo seleccionable
        spinnerNumComensales.setSelection(0); //el valor es 1 por defecto
        spinnerNumComensales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene el número de comensales seleccionado
                numComensales = Integer.parseInt(numComensalesList.getItem(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó ningún elemento
            }
        });
    }

    //método que configura la elección del primer plato para la reserva
    private void configurarSpinnerPrimerPlato() {
        // Crea un ArrayAdapter utilizando el array definido en strings.xml
        ArrayAdapter<CharSequence> PrimerPlato = ArrayAdapter.createFromResource(this, R.array.primeros_platos_array, android.R.layout.simple_spinner_dropdown_item);

        // Asigna el ArrayAdapter al Spinner
        spinnerPrimerPlato.setAdapter(PrimerPlato);

        spinnerPrimerPlato.setSelection(0); //el valor es 1 por defecto
        spinnerPrimerPlato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene el número de comensales seleccionado
                Primero = PrimerPlato.getItem(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó ningún elemento
            }
        });
    }

    //método que configura la elección del segundo plato para la reserva
    private void configurarSpinnerSegundoPlato() {
        // Crea un ArrayAdapter utilizando el array definido en strings.xml
        ArrayAdapter<CharSequence> SegundoPlato = ArrayAdapter.createFromResource(this, R.array.menu_segundo_plato, android.R.layout.simple_spinner_dropdown_item);

        // Asigna el ArrayAdapter al Spinner
        spinnerSegundoPlato.setAdapter(SegundoPlato);

        spinnerSegundoPlato.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene el número de comensales seleccionado
                Segundo = SegundoPlato.getItem(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó ningún elemento
            }
        });
    }

    //método que configura la elección del postre para la reserva
    private void configurarSpinnerPostre() {
        // Crea un ArrayAdapter utilizando el array definido en strings.xml
        ArrayAdapter<CharSequence> Postrecito = ArrayAdapter.createFromResource(this, R.array.postres_array, android.R.layout.simple_spinner_dropdown_item);

        // Asigna el ArrayAdapter al Spinner
        spinnerPostre.setAdapter(Postrecito);

        spinnerPostre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtiene el número de comensales seleccionado
                Postre = Postrecito.getItem(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó ningún elemento
            }
        });
    }

    /*método que comprueba cuantas reservas hay para ese día, si hay 30 ya no deja hacer más reservas
    tampoco deja hacer más reservas si solo quedan X comensales máximos para llegar a 30 y queremos hacer
    una reserva de más de ese número X de comensales*/
    private void obtenerTotalComensalesParaFecha() {
        String fechita = editTextFecha.getText().toString();
        // Obtiene una referencia a la colección "Reservas" y realiza una consulta filtrando por fecha
        db.collection("Reservas")
                .whereEqualTo("fecha", fechita)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int totalComensales = 0;

                        // Recorre todos los documentos de la consulta y suma los comensales para obtener el total de comensales en ese día
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.contains("comensales")) {
                                totalComensales += Integer.parseInt(document.getString("comensales"));
                            }
                        }
                        // Calcula la cantidad máxima de comensales que se pueden agregar adicionalmente
                        int comensalesRestantes = 30 - totalComensales;

                        // Verifica si se ha alcanzado el límite de comensales para la fecha seleccionada
                        if (totalComensales >= 30) {
                            // No hay disponibilidad para reservar esa fecha
                            Toast.makeText(getApplicationContext(), "Ya no hay disponibilidad para reservar esa fecha", Toast.LENGTH_SHORT).show();
                        } else {
                            // Obtiene el número de comensales para la nueva reserva
                            String comensalesStr = numComensales+"";
                            int comensales = Integer.parseInt(comensalesStr);

                            // Verifica si se supera la cantidad máxima de comensales que se pueden agregar
                            if (comensales > comensalesRestantes) {
                                Toast.makeText(getApplicationContext(), "No se pueden agregar más de " + comensalesRestantes + " comensales para esa fecha", Toast.LENGTH_SHORT).show();
                            } else {
                                // guardar la reserva
                                guardarReservayComprobarCorreo();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al obtener el número de comensales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //método para insertar en la bd los datos de cada cliente y comprobar si el correo existe
    private void guardarReservayComprobarCorreo() {
        String nombre = editTextNombre.getText().toString().trim();
        String correo = editTextCorreo.getText().toString().trim();
        String fecha = editTextFecha.getText().toString().trim();
        String hora = editTextHora.getText().toString().trim();
        String numero_comensales= numComensales+"";
        String primerito=Primero+"";
        String segundito=Segundo+"";
        String postrecito=Postre+"";

        // Si alguno de los campos está vacío, me avisa un Toast
        if (nombre.isEmpty() || correo.isEmpty() || fecha.isEmpty() || hora.isEmpty()||numero_comensales.isEmpty()||
                primerito.isEmpty()||segundito.isEmpty()||postrecito.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        //a partir de aqui compruebo si el correo que voy a insertar ya existe en la bd
        String correito=editTextCorreo.getText().toString().trim();
        db.collection("Reservas")
                .whereEqualTo("email", correito)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            // Verificar si el correo existe en alguna reserva
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Si existe, mostrar un Toast
                                Toast.makeText(getApplicationContext(), "No se pueden hacer 2 reservas con el mismo correo", Toast.LENGTH_SHORT).show();
                            } else {
                                //Si no existe el correo, Creación de HashMap para guardar los datos en formato clave-valor
                                Map<String, Object> reserva = new HashMap<>();
                                reserva.put("nombre", nombre);
                                reserva.put("email", correo);
                                reserva.put("fecha", fecha);
                                reserva.put("hora", hora);
                                reserva.put("comensales", numero_comensales);
                                reserva.put("primero",primerito);
                                reserva.put("segundo",segundito);
                                reserva.put("postre",postrecito);

                                // método que guarda la información correspondiente de las reservas
                                db.collection("Reservas").add(reserva).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(getApplicationContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                                                //finish();
                                                Intent intent = new Intent(ContrataServicios.this, MainActivity.class); // Lo envío a la pantalla principal
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Error al guardar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // Error al realizar la consulta, mostrar un Toast
                            Toast.makeText(getApplicationContext(), "Error al verificar el correo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}