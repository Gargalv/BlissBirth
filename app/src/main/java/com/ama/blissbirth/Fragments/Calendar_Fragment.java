package com.ama.blissbirth.Fragments;

import android.app.AlertDialog;
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


import com.ama.blissbirth.R;

import java.util.Calendar;
import android.Manifest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Calendar_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Calendar_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private DatePicker datePicker;
    private Button addBirthdayButton;

    // Constante para identificar la solicitud de permisos
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 123;


    public Calendar_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Calendar_Fragment.
     */
    // TODO: Rename and change types and number of parameters
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
    }

    // Dentro de tu fragmento de calendario

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
                    guardarCumpleaños();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    guardarCumpleaños();
                } else {
                    Toast.makeText(getActivity(), "Permiso denegado para escribir en el calendario", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void guardarCumpleaños() {
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
                guardarCumpleañosEnCalendario(getActivity(), year, month, dayOfMonth, name);
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

    private long getDateTimeMillis(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0);
        return calendar.getTimeInMillis();
    }
}