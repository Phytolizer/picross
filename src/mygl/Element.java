package mygl;

import java.awt.*;

public class Element {
    protected boolean visible;
    protected Graphics2D graphics2D;
    protected int screenX;
    protected int screenY;
    protected int sizeX;
    protected int sizeY;
    protected Updater updater;
    
    public Element(Graphics2D graphics2D) {
        screenX = 0;
        screenY = 0;
        sizeX = 0;
        sizeY = 0;
        visible = false;
        updater = null;
        this.graphics2D = graphics2D;
        AllElements.add(this);
    }
    
    protected void move(int screenX, int screenY) {
        move(screenX, screenY, Alignment.LEFT, Alignment.TOP);
    }
    
    public void move(int alignedX, int alignedY, Alignment horizontalAlignment, Alignment verticalAlignment) {
        Coordinate screenCoords = Coordinate.convertToScreenCoordinates(alignedX, alignedY, sizeX, sizeY, horizontalAlignment, verticalAlignment);
        screenX = screenCoords.x;
        screenY = screenCoords.y;
    }
    
    public void resize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    
    public void resizeBy(int dx, int dy) {
        sizeX += dx;
        sizeY += dy;
    }
    
    protected boolean inBounds(int mouseX, int mouseY) {
        return mouseX >= screenX && mouseX < screenX + sizeX && mouseY >= screenY
               && mouseY < screenY + sizeY;
    }
    
    protected boolean inBounds(Point pt) {
        return pt.x >= screenX && pt.x < screenX + sizeX && pt.y >= screenY
               && pt.y < screenY + sizeY;
    }
    
    public void draw() {
    
    }
    
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            if (visible) {
                AllElements.startDrawing(this);
            } else {
                AllElements.stopDrawing(this);
            }
        }
    }
    
    protected void update() {
        // Elements only update when they are able to be drawn.
        if (updater != null && visible) {
            updater.update();
        }
    }
    
    public void setUpdater(Updater updater) {
        this.updater = updater;
    }
    
    public void disconnect() {
        AllElements.remove(this);
    }
}
