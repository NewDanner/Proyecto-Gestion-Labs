/**
 *
 * @author Andrei/DANNER
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
    // Cambio 25/05/25
    private String correo;
    // Cambio 27/05/25
    private String password;
    
    public User(int id, String nombre, String username, String ci, String role, String sexo, String correo, String password) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.ci = ci;
        this.role = role;
        this.sexo = sexo;
        // Cambio 25/05/25
        this.correo = correo;
        // ******************
        // Cambio 27/05/25
        this.password = password;
        // ******************
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
    // Cambio 25/05/25
    public String getCorreo() { return correo; }
    // *****************************************
    // Cambio 27/05/25
    public String getPassword() { return password; }
    // *****************************************
    
    // Setters
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    public void setTurno(String turno) { this.turno = turno; }
    public void setEstado(boolean estado) { this.estado = estado; }
    // Cambio 25/05/25
    public void setCorreo(String correo) { this.correo = correo; }
    // ***********************************************************
    // Cambio 27/05/25
    public void setPassword(String password) { this.password = password; }
    // ***********************************************************
    
    public String getNombreCompleto() {
        return nombre + " " + (segundoNombre != null ? segundoNombre + " " : "") + 
               primerApellido + " " + (segundoApellido != null ? segundoApellido : "");
    }
}