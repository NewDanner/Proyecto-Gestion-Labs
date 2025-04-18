/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Andrei
 */
package models;

public class User {
    private int id;
    private String username;
    private String ci;
    private String role; // "Administrador" o "Usuario"
    private String sexo; // Nuevo atributo de registro
    
    public User(int id, String username, String ci, String role) {
        this.id = id;
        this.username = username;
        this.ci = ci;
        this.role = role;
        this.sexo = sexo;
    }

    public User(int id, String username, String ci, String role, String sexo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCi() { return ci; }
    public String getRole() { return role; }
    public String getSexo() { return sexo; }

    public String getRol() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
