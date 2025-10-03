package com.example.springboot.reactor.springboot.reactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {

    private List<String> comentarios;

    public Comentarios() {
        this.comentarios = new ArrayList<>();
    }

    public List<String> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<String> comentarios) {
        this.comentarios = comentarios;
    }

    public void addComentarios(String comentarios) {
        this.comentarios.add(comentarios);
    }

    @Override
    public String toString() {
        return "comentarios=" + comentarios ;
    }
}
