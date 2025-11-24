/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author ANDREI/DANNER
 */

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import models.DatabaseConnection;

public class ReportHeaderConfig {
    private String companyName;
    private String companyAddress;
    private String userName;
    private Image logo;
    private int userId;

    public ReportHeaderConfig(int userId) {
        this.userId = userId;
        this.companyName = "NOMBRE DE EMPRESA";
        this.companyAddress = "DIRECCIÓN DE LA EMPRESA";
        this.userName = "";
        loadFromDatabase();
    }

    // Getters y Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName != null ? companyName : "";
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress != null ? companyAddress : "";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName != null ? userName : "";
    }

    public Image getLogo() {
        return logo;
    }

    public void setLogo(Image logo) {
        this.logo = logo;
    }
    
    public boolean hasLogo() {
        return logo != null;
    }
    
    public BufferedImage getLogoAsBufferedImage() {
        if (!hasLogo()) {
            return null;
        }
        
        BufferedImage bufferedImage = new BufferedImage(
            logo.getWidth(null), 
            logo.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(logo, 0, 0, null);
        g2d.dispose();
        
        return bufferedImage;
    }
    
    private void loadFromDatabase() {
        String query = "SELECT cr.nombre_empresa, cr.direccion_empresa, cr.logo, u.nombre AS usuario " +
                      "FROM ConfiguracionReportes cr " +
                      "JOIN Usuarios u ON cr.id_usuario = u.id_usuario " +
                      "WHERE cr.id_usuario = ? " +
                      "ORDER BY cr.fecha_actualizacion DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                this.companyName = rs.getString("nombre_empresa");
                this.companyAddress = rs.getString("direccion_empresa");
                this.userName = rs.getString("usuario");
                
                // Cargar logo si existe
                Blob logoBlob = rs.getBlob("logo");
                if (logoBlob != null) {
                    byte[] logoBytes = logoBlob.getBytes(1, (int)logoBlob.length());
                    this.logo = new ImageIcon(logoBytes).getImage();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar configuración de reportes: " + e.getMessage());
        }
    }
    
    public void saveToDatabase() {
        String query = "INSERT INTO ConfiguracionReportes (nombre_empresa, direccion_empresa, logo, id_usuario) " +
                      "VALUES (?, ?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE " +
                      "nombre_empresa = VALUES(nombre_empresa), " +
                      "direccion_empresa = VALUES(direccion_empresa), " +
                      "logo = VALUES(logo)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, companyName);
            stmt.setString(2, companyAddress);
            
            if (logo != null) {
                BufferedImage bufferedLogo = getLogoAsBufferedImage();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedLogo, "png", baos);
                stmt.setBinaryStream(3, new ByteArrayInputStream(baos.toByteArray()), baos.size());
            } else {
                stmt.setNull(3, java.sql.Types.BLOB);
            }
            
            stmt.setInt(4, userId);
            stmt.executeUpdate();
            
        } catch (SQLException | IOException e) {
            System.err.println("Error al guardar configuración de reportes: " + e.getMessage());
        }
    }
}