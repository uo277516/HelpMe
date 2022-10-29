package controller;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.helpme.model.Alumno;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlumnoController {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Obtiene un alumno por su referencia.
     *
     * @param ref
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Optional<Alumno> findById(DocumentReference ref, AlumnoCallback callback) {
        Task<DocumentSnapshot> document = db.document(ref.getPath()).get();

        document.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = document.getResult();

                    Alumno alumno = getPayload(doc.getId(), doc.getString("UO"), doc.getString("NOMBRE"));

                    callback.callback(alumno);

                }
            }
        });

        return Optional.ofNullable(null);
    }

    private Alumno getPayload(String id, String uo, String nombre) {
        return new Alumno(id, uo, nombre);
    }

    interface AlumnoCallback{
        void callback(Alumno alumno);
    }
}
