package controller;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.helpme.model.Alumno;
import com.example.helpme.model.Asignatura;
import com.example.helpme.model.Duda;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import util.DateUtils;

public class DudaController {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AlumnoController alumnoController = new AlumnoController();

    /**
     * Listado de todas las dudas de la aplicación.
     *
     * @return
     */
    public List<Duda> findAll() {
        List<Duda> dudas = new ArrayList<>();

        Task<QuerySnapshot> collection = db.collection("DUDA").get();

        collection
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String, Object> dudaData = document.getData();

                                String titulo = dudaData.get("TITULO").toString();
                                String descripcion = dudaData.get("DESCRIPCION").toString();
                                LocalDateTime fecha = DateUtils.convertTimeStampToLocalDateTime((Timestamp) dudaData.get("FECHA"));
                                DocumentReference alumnoId = (DocumentReference) dudaData.get("PERSONA_DUDA");

                                Optional<Alumno> a1 = alumnoController.findById(alumnoId,
                                        new AlumnoController.AlumnoCallback() {
                                            @Override
                                            public void callback(Alumno alumno) {
                                                Log.i("CONTROLLER ALUMNO", alumno.getNombre() + " " + alumno.getId());


                                            }
                                        });

                                Log.i("DUDA CONTROLLER", a1.get().getNombre() + " " + a1.get().getId());

//                                if (a1.isPresent()) {
//                                    Log.d(TAG, a1.get().getNombre() + " " + a1.get().getUo());
//                                    Log.d(TAG, titulo + " ; " + descripcion + " ; " + fecha + " ; " + alumnoId);
//                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return dudas;
    }
}
