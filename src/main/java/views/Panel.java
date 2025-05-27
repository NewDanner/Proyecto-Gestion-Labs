
package views;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author frank
 */
public class Panel extends JPanel{
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = new Color(249,45,168); // Verde claro
            Color color2 = new Color(255, 209, 12);   // Verde oscuro
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
}
