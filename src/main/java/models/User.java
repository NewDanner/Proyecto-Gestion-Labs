/**
 *
 * @author Andrei
 */
package models;

public class User {
    private int id;
    private String nombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String turno;
    private String username;
    private String ci;
    private String role;
    private String sexo;
    private boolean estado;
    
    public User(int id, String nombre, String username, String ci, String role, String sexo) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.ci = ci;
        this.role = role;
        this.sexo = sexo;
    }
    
    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public String getPrimerApellido() { return primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getTurno() { return turno; }
    public String getUsername() { return username; }
    public String getCi() { return ci; }
    public String getRole() { return role; }
    public String getSexo() { return sexo; }
    public boolean isEstado() { return estado; }
    
    // Setters
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    public void setTurno(String turno) { this.turno = turno; }
    public void setEstado(boolean estado) { this.estado = estado; }
    
    public String getNombreCompleto() {
        return nombre + " " + (segundoNombre != null ? segundoNombre + " " : "") + 
               primerApellido + " " + (segundoApellido != null ? segundoApellido : "");
    }
}