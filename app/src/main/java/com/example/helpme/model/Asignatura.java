package com.example.helpme.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Asignatura implements Parcelable {

    public static final String COLLECTION = "ASIGNATURA";
    public static final String NOMBRE = "nombre";
    public static final String MATERIA = "materia";
    public static final String CURSO = "curso";
    public static final String ID = "id";

    private String id;
    private String nombre;
    private String curso;
    private String materia;



    public Asignatura(String id, String nombre, String curso, String materia) {
        this.id = id;
        this.nombre = nombre;
        this.curso = curso;
        this.materia = materia;
    }

    public Asignatura() {

    }
    public Asignatura(String curso, String materia, String nombre) {

        this.nombre = nombre;
        this.curso = curso;
        this.materia = materia;
    }



    protected Asignatura(Parcel in) {
        id = in.readString();
        nombre = in.readString();
    }

    public static final Creator<Asignatura> CREATOR = new Creator<Asignatura>() {
        @Override
        public Asignatura createFromParcel(Parcel in) {
            return new Asignatura(in);
        }

        @Override
        public Asignatura[] newArray(int size) {
            return new Asignatura[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeString(materia);

    }

    @NonNull
    public String toString() {
        return nombre;
    }
}
