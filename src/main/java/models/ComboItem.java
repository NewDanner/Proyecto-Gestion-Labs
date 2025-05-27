/**
 *
 * @author Andrei
 */
package models;

public class ComboItem {
    private final String label;
    private final int value;
    
    public ComboItem(String label, int value) {
        this.label = label;
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return label;
    }
}