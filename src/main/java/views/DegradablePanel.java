
package views;

/**
 *
 * @author frank
 */
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DegradablePanel extends JPanel {
    protected Color color1 = new Color(249, 45, 168); // Rosa
    protected Color color2 = new Color(255, 209, 12); // Amarillo
    
    public DegradablePanel() {
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Fondo degradado
        GradientPaint gradient = new GradientPaint(
            0, 0, color1, 
            0, getHeight(), color2);
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    protected JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Fondo del botón con esquinas redondeadas
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 180)); // Blanco semi-transparente
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Borde del botón
                g2d.setColor(new Color(70, 130, 180)); // Azul acero
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                // Texto del botón
                g2d.setColor(Color.BLACK);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 40));
        
        return button;
    }
}
