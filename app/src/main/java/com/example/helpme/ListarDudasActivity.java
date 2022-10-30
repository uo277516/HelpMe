package com.example.helpme;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpme.model.Duda;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import controller.AlumnoController;
import controller.AsignaturaController;
import controller.DudaController;

public class ListarDudasActivity extends AppCompatActivity {

    private static final String DUDA_SELECCIONADA = "duda_seleccionada";

    //Modelo de datos
    private List<Duda> listaDuda = new ArrayList<Duda>();
    ;
    private Duda duda;
    private RecyclerView listaDudaView;
    private Object[] asignatura_data;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private AsignaturaController asignaturaController = new AsignaturaController();
    private AlumnoController alumnoController = new AlumnoController();
    private DudaController dudaController = new DudaController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_dudas);

        //Rellenar lista de dudas y en el adapter
        cargarDudas();

        // Recuperamos referencia y configuramos recyclerView con la lista de dudas
        listaDudaView = (RecyclerView) findViewById(R.id.reciclerView);
        listaDudaView.setHasFixedSize(true);

        /* Un RecyclerView necesita un Layout Manager para manejar el posicionamiento de los
        elementos en cada línea. Se podría definir un LayoutManager propio extendendiendo la clase
        RecyclerView.LayoutManager. Sin embargo, en la mayoría de los casos, simplemente se usa
        una de las subclases LayoutManager predefinidas: LinearLayoutManager, GridLayoutManager,
        StaggeredGridLayoutManager*/
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        listaDudaView.setLayoutManager(layoutManager);

        //Pasamos la lista de dudas al RecyclerView con el ListaDudaAdapter
        // Instanciamos el adapter con los datos de la petición y lo asignamos a RecyclerView
        // Generar el adaptador, le pasamos la lista de dudas
        // y el manejador para el evento click sobre un elemento

        ListaDudasAdapter lpAdapter = new ListaDudasAdapter(listaDuda,
                new ListaDudasAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Duda duda) {
                        clikonIntem(duda);
                    }
                });
        listaDudaView.setAdapter(lpAdapter);


    }

    //click del item del adapter
    private void clikonIntem(Duda duda) {
        Log.i("Click adapter", "Item Clicked " + duda.getTitulo());


        //Le paso la duda al MainActivity para que la muestre al picnchar en la duda
        Intent intent = new Intent(ListarDudasActivity.this, ActivityShowDuda.class);
        intent.putExtra(DUDA_SELECCIONADA, duda);

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void cargarDudas() {
        Duda d1 = new Duda("Duda 1", "Descripcion duda 1", "alumno1", "asignatura1", "materia1", false, "30/10/2022 12:35:24");
        Duda d2 = new Duda("Duda 2", "Descripcion duda 2", "alumno2", "asignatura2", "materia2", false, "03/01/2022 12:40:24");
        Duda d3 = new Duda("Duda 3", "Descripcion duda 3", "alumno3", "asignatura3", "materia3", false, "28/10/2022 14:35:24");
        Duda d4 = new Duda("Duda 4", "Descripcion duda 4", "alumno3", "asignatura4", "materia4", false, "31/10/2022 12:35:24");

        listaDuda.add(d1);
        listaDuda.add(d2);
        listaDuda.add(d3);
        listaDuda.add(d4);
    }

    private boolean getBoolean(String toString) {
        if (toString.equals("true")) return true;
        return false;
    }


}