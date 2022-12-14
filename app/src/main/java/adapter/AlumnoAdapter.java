package adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpme.FriendProfileActivity;
import com.example.helpme.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dto.AlumnoDto;
import dto.AsignaturaDto;

public class AlumnoAdapter extends RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder> {

    private List<AlumnoDto> alumnos = new ArrayList<>();

    public AlumnoAdapter(List<AlumnoDto> alumnos) {
        this.alumnos = alumnos;
    }

    @NonNull
    @Override
    public AlumnoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.linea_recycler_view_alumno, parent, false);
        return new AlumnoViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull AlumnoViewHolder holder, int position) {
        AlumnoDto alumno = alumnos.get(position);

        holder.nombreAlumno.setText(alumno.nombre);

        holder.bindAlumno(alumno);

        holder.botonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.context, FriendProfileActivity.class);

                String info = alumno.nombre + "---" + alumno.uo + "---"
                        + alumno.urlFoto + "---" + alumno.email + "---" +
                        alumno.asignaturasDominadas;

                intent.putExtra("alumno_seleccionado", info);

                holder.context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return alumnos.size();
    }




    protected class AlumnoViewHolder extends RecyclerView.ViewHolder {

        private TextView nombreAlumno;
        private TextView asiganturas;
        private ImageView imagen_alumno;
        private Button botonPerfil;
        private Context context;

        public AlumnoViewHolder(@NonNull View itemView) {
            super(itemView);
            context=itemView.getContext();
            nombreAlumno = itemView.findViewById(R.id.nombreAlumno);
            asiganturas = itemView.findViewById(R.id.textViewAsignaturasDominadas);
            imagen_alumno = itemView.findViewById(R.id.imageViewPerfil);
            botonPerfil = itemView.findViewById(R.id.buttonPerfil);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bindAlumno(final AlumnoDto alumno) {
            nombreAlumno.setText(alumno.nombre);
            String cadena = "";
            Object[] asigs = alumno.asignaturasDominadas.values().toArray();
            int contador = 0;

            for (Object nombre : asigs) {
                contador++;
                AsignaturaDto a = new AsignaturaDto();
                String linea = nombre.toString();
                Map<Object, Object> prueba = (Map<Object, Object>) nombre;
                Map<Object, String> materia = (Map<Object, String>) prueba.get("materia");
                String abreviatura = materia.get("abreviatura");
                if (contador == 5) {
                    cadena += "...";
                    break;
                }
                cadena += abreviatura + " ";
            }
            if (cadena.length() == 0) {
                asiganturas.setText(R.string.sin_asignaturas);
            } else {
                asiganturas.setText(cadena);
            }

            Picasso.get().load(alumno.urlFoto).into(imagen_alumno);

        }
    }
}