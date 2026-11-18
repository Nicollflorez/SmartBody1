package com.smartbody.planes.entity;

/** Momentos del dia en los que se reparte el plan nutricional. */
public enum MomentoComida {
    DESAYUNO("Desayuno"),
    ALMUERZO("Almuerzo"),
    CENA("Cena"),
    SNACK("Snack");

    private final String nombre;

    MomentoComida(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
