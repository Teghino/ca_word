package com.example.demo;

import java.util.Map;

public class Persona {
    private String nome;
    private String classe;
    private String annoScolastico;
    private OreCorso[] oreCorso;
    public Persona(String nome, String classe, String annoScolastico, OreCorso[] oreCorso){
        this.nome = nome;
        this.classe = classe;
        this.annoScolastico = annoScolastico;
        this.oreCorso = oreCorso;
    }
}
