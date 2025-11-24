
/**
 *
 * @author Andrei
 */
package models;

import java.sql.Date;
import java.sql.Timestamp;

public class MaterialAdicional {
    private int nObjeto;
    private String nombreObjeto;
    private String categoria;
    private int cantidad;
    private int cantidadMinima;
    private boolean extravio;
    private boolean dano;
    private String observaciones;
    private int idLaboratorio;
    private Timestamp fechaReporte;
    private int reportadoPor;
    private String proveedor;
    private Date fechaCompra;
    private Double costo;

    public MaterialAdicional() {}

    public MaterialAdicional(int nObjeto, String nombreObjeto, String categoria, int cantidad, 
                           int cantidadMinima, boolean extravio, boolean dano, String observaciones, 
                           int idLaboratorio, Timestamp fechaReporte, int reportadoPor) {
        this.nObjeto = nObjeto;
        this.nombreObjeto = nombreObjeto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.cantidadMinima = cantidadMinima;
        this.extravio = extravio;
        this.dano = dano;
        this.observaciones = observaciones;
        this.idLaboratorio = idLaboratorio;
        this.fechaReporte = fechaReporte;
        this.reportadoPor = reportadoPor;
    }

    // Getters y Setters
    public int getNObjeto() { return nObjeto; }
    public void setNObjeto(int nObjeto) { this.nObjeto = nObjeto; }

    public String getNombreObjeto() { return nombreObjeto; }
    public void setNombreObjeto(String nombreObjeto) { this.nombreObjeto = nombreObjeto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(int cantidadMinima) { this.cantidadMinima = cantidadMinima; }

    public boolean isExtravio() { return extravio; }
    public void setExtravio(boolean extravio) { this.extravio = extravio; }

    public boolean isDano() { return dano; }
    public void setDano(boolean dano) { this.dano = dano; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public int getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(int idLaboratorio) { this.idLaboratorio = idLaboratorio; }

    public Timestamp getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(Timestamp fechaReporte) { this.fechaReporte = fechaReporte; }

    public int getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(int reportadoPor) { this.reportadoPor = reportadoPor; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Date getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(Date fechaCompra) { this.fechaCompra = fechaCompra; }

    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }
}