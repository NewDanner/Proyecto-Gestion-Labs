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
    private String role;
    
    public User(int id, String username, String ci, String role) {
        this.id = id;
        this.username = username;
        this.ci = ci;
        this.role = role;
    }
    
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCi() { return ci; }
    public String getRole() { return role; }
}
