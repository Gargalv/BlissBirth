package com.ama.blissbirth.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.ama.blissbirth.BirthdayAlarmReceiver;
import com.ama.blissbirth.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Calendar_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Calendar_Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private DatePicker datePicker;
    private Button addBirthdayButton;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 123;
    private FirebaseFirestore db;

    public Calendar_Fragment() {
        // Required empty public constructor
    }

    public static Calendar_Fragment newInstance(String param1, String param2) {
        Calendar_Fragment fragment = new Calendar_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar_, container, false);

        datePicker = rootView.findViewById(R.id.datePicker);
        addBirthdayButton = rootView.findViewById(R.id.addBirthdayButton);

        addBirthdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                } else {
                    showBirthdayDialog();
                }
            }
        });

        return rootView;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_CALENDAR) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showBirthdayDialog();
            } else {
                Toast.makeText(getActivity(), "Permiso denegado para escribir en el calendario", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBirthdayDialog() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int dayOfMonth = datePicker.getDayOfMonth();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Añadir Cumpleaños");
        builder.setMessage("Ingrese el nombre del cumpleañero:");

        final EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    guardarCumpleañosEnCalendario(getActivity(), year, month, dayOfMonth, name);
                    guardarCumpleañosEnFirestore(name, year, month, dayOfMonth);
                } else {
                    Toast.makeText(getActivity(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void guardarCumpleañosEnCalendario(Context context, int year, int month, int dayOfMonth, String name) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, getDateTimeMillis(year, month, dayOfMonth));
        values.put(CalendarContract.Events.DTEND, getDateTimeMillis(year, month, dayOfMonth));
        values.put(CalendarContract.Events.TITLE, "Cumpleaños de " + name);
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC/GMT");

        try {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                long eventId = Long.parseLong(uri.getLastPathSegment());
                Toast.makeText(context, "Cumpleaños guardado en el calendario", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al guardar el cumpleaños", Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Log.e("Calendar_Fragment", "SecurityException: " + e.getMessage());
            Toast.makeText(context, "Permiso denegado para escribir en el calendario", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Calendar_Fragment", "Exception: " + e.getMessage());
            Toast.makeText(context, "Error al guardar el cumpleaños: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarCumpleañosEnFirestore(String name, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 8, 0);  // Establecer la hora de la alarma (8 AM en este caso)

        Date date = calendar.getTime();
        Timestamp timestamp = new Timestamp(date);

        String id = db.collection("birthdays").document().getId();

        Map<String, Object> birthday = new HashMap<>();
        birthday.put("id", id);
        birthday.put("name", name);
        birthday.put("date", timestamp);

        db.collection("birthdays").document(id)
                .set(birthday)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Cumpleaños guardado exitosamente en Firestore");
                        Toast.makeText(getActivity(), "Cumpleaños guardado en Firestore", Toast.LENGTH_SHORT).show();
                        programarNotificacion(name, calendar.getTimeInMillis());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error al guardar cumpleaños en Firestore", e);
                        Toast.makeText(getActivity(), "Error al guardar el cumpleaños en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void programarNotificacion(String name, long triggerAtMillis) {
        Log.d("Calendar_Fragment", "Programando notificación para: " + name + " en " + triggerAtMillis + " milisegundos");

        // Crear un calendario con la fecha y hora seleccionadas
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(triggerAtMillis);

        // Establecer la hora de la alarma a las 00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Verificar si la fecha seleccionada es anterior a la fecha actual
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            // Si la fecha es anterior a la actual, programar la notificación para el próximo año
            calendar.add(Calendar.YEAR, 1);
        }

        // Obtener el tiempo de disparo corregido
        long correctedTriggerAtMillis = calendar.getTimeInMillis();

        Intent intent = new Intent(getActivity(), BirthdayAlarmReceiver.class);
        intent.putExtra("name", name);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, correctedTriggerAtMillis, pendingIntent);

        Log.d("Calendar_Fragment", "Notificación programada para: " + name + " a las 00:00");
    }




    private long getDateTimeMillis(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0);
        return calendar.getTimeInMillis();
    }
}
