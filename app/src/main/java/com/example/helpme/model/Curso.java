package com.example.helpme.model;

public class Curso {
    private String id;
    private String numero;

    public static final String NUMERO = "numero";

    public Curso(String id, String numero) {
        this.id = id;
        this.numero = numero;
    }

    public Curso(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

}
