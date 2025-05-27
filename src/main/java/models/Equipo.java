

/**
 *
 * @author Andrei
 */
// Nueva clase Equipo.java
package models;

import java.sql.Date;
import java.sql.Timestamp;

public class Equipo {
    private int idEquipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String procesador;
    private String ram;
    private String almacenamiento;
    private String so;
    private String estado;
    private Integer idLaboratorio;
    
    public Equipo() {
        this.estado = "Operativo"; // Valor por defecto
    }

    // Getters y Setters (solo los esenciales mostrados)
    public int getIdEquipo() { return idEquipo; }
    public void setIdEquipo(int idEquipo) { this.idEquipo = idEquipo; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getProcesador() { return procesador; }
    public void setProcesador(String procesador) { this.procesador = procesador; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getAlmacenamiento() { return almacenamiento; }
    public void setAlmacenamiento(String almacenamiento) { this.almacenamiento = almacenamiento; }

    public String getSo() { return so; }
    public void setSo(String so) { this.so = so; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(Integer idLaboratorio) { this.idLaboratorio = idLaboratorio; }
    
    @Override
    public String toString() {
        return marca + " " + modelo + " (" + numeroSerie + ")";
    }
}
