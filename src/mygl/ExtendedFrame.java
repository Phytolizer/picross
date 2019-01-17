package mygl;

import java.awt.*;
import java.awt.event.*;

public class ExtendedFrame extends Frame implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    private boolean[] mouseButtonState;
    private KeyListener keyListener;
    
    public ExtendedFrame(String windowTitle) {
        super(windowTitle);
        mouseButtonState = new boolean[4];
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
        addKeyListener(keyListener);
    }
    
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    
    }
    
    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        mouseButtonState[mouseEvent.getButton()] = true;
    }
    
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mouseButtonState[mouseEvent.getButton()] = false;
    }
    
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    
    }
    
    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    
    }
    
    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    
    }
    
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
    
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    
    }
    
    public boolean buttonDown(int mouseButton) {
        if (mouseButton <= 0 || mouseButton > 3) {
            return false;
        }
        return mouseButtonState[mouseButton];
    }
}
