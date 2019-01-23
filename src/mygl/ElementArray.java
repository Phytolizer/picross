package mygl;

import java.awt.*;

public class ElementArray extends Element {
    private Element[] elements;
    private boolean useXAxis;
    private int maximumElementSize;
    private int maximumElementSpace;
    private final float DEFAULT_ELEMENT_SPACE_RATIO = 2;
    
    public ElementArray(Graphics2D graphics2D) {
        super(graphics2D);
        useXAxis = true;
        maximumElementSize = -1;
        maximumElementSpace = -1;
        setUpdater(this::centerAndSpaceElements);
    }
    
    public void setElements(Element[] elements) {
        this.elements = elements;
    }
    
    public void setMaximumElementSize(int maximumElementSize) {
        this.maximumElementSize = maximumElementSize;
    }
    
    public void setMaximumElementSpace(int maximumElementSpace) {
        this.maximumElementSpace = maximumElementSpace;
    }
    
    /**
     * Moves and resizes the Elements contained in <code>elements</code> so that they:
     *   a) fit in the space provided, i.e. the size of this ElementArray
     *   b) are spaced evenly and all the same size as each other
     *   c) if maximumElementSize is defined (not -1), elements cannot exceed that size
     *   d) if maximumElementSpace is defined (not -1), the space *between* elements cannot exceed that value
     *   e) if both of the above are defined, elements will be centered in excess space if it exists
     *
     * The ElementArray only considers sizes along the x or y axis (decided by the value
     * of useXAxis), and will never move/scale its elements along the other axis.
     *
     * This method takes no parameters, and need not be called manually; it is handled by the Updater.
     */
    private void centerAndSpaceElements() {
        int totalSpace;
        if (useXAxis) {
            totalSpace = sizeX - screenX;
        } else {
            totalSpace = sizeY - screenY;
        }
        int minSpaceForDefaults;
        if (maximumElementSize == -1) {
            if (maximumElementSpace == -1) {
                minSpaceForDefaults = totalSpace + 1;
            } else {
                minSpaceForDefaults = maximumElementSpace * (elements.length - 1);
            }
        } else {
            if (maximumElementSpace == -1) {
                minSpaceForDefaults = maximumElementSize * elements.length;
            } else {
                minSpaceForDefaults = maximumElementSize * elements.length + maximumElementSpace * (
                        elements.length - 1);
            }
        }
        boolean useDefaults = totalSpace >= minSpaceForDefaults;
        int startingPosition;
        if (useXAxis) {
            startingPosition = screenX;
        } else {
            startingPosition = screenY;
        }
        if (useDefaults) {
            startingPosition += (totalSpace - minSpaceForDefaults) / 2;
        }
        
    }
}
