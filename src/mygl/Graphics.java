package mygl;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

public class Graphics implements Runnable, WindowListener, WindowFocusListener {
    
    private boolean done = false;
    private boolean running = true;
    protected int frameHeight;
    protected int frameWidth;
    private final int refreshInterval = 10;
    protected ExtendedFrame frame;
    protected Graphics2D graphics2D;
    private Image imageBuffer;
    
    public Graphics(ExtendedFrame frame) {
        this.frame = frame;
        
        // center frame in display
        frameWidth = frame.getWidth();
        frameHeight = frame.getHeight();
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                     .getDefaultScreenDevice()
                                                     .getDisplayMode();
        int displayWidth = displayMode.getWidth();
        int displayHeight = displayMode.getHeight();
        frame.setLocation(displayWidth / 2 - frameWidth / 2,
                displayHeight / 2 - frameHeight / 2);
        
        // Window listeners
        frame.addWindowListener(this);
        frame.addWindowFocusListener(this);
        
        imageBuffer = frame.createImage(frameWidth, frameHeight);
        
        // Set rendering hints for first draw
        graphics2D = (Graphics2D) imageBuffer.getGraphics();
        // Anti-aliased lines/shapes
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Smoothness in image scaling/rotation
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 20pt Arial is default font if it is not set
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 20));
        graphics2D.setColor(Color.BLACK);
    }
    
    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowLostFocus(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowOpened(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowClosing(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowClosed(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowIconified(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowDeiconified(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowActivated(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
    
    }
    
    @Override
    public void run() {
        // Main loop
        while (running) {
            updateSizeVariables();
            handleMouse();
            AllElements.update();
            setupDraw();
            draw();
            AllElements.draw();
            applyDraw();
            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleMouse() {
        // To be implemented in subclass
    }
    
    private void updateSizeVariables() {
        frameWidth = frame.getWidth();
        frameHeight = frame.getHeight();
    }
    
    protected void setupDraw() {
        graphics2D = (Graphics2D) imageBuffer.getGraphics();
    }
    
    /**
     * Use this method for drawing primitives that do not count as Element objects.
     * Elements are drawn automatically if they are set to visible.
     */
    protected void draw() {
        // To be implemented in subclass
    }
    
    protected void applyDraw() {
        graphics2D = (Graphics2D) frame.getGraphics();
        if (graphics2D != null) {
            imageBuffer = imageBuffer.getScaledInstance(frameWidth, frameHeight, Image.SCALE_DEFAULT);
            graphics2D.drawImage(imageBuffer, 0, 0, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight, null);
            graphics2D.dispose();
        }
    }
    
    Graphics2D getGraphics2D() {
        return graphics2D;
    }
    
    public ExtendedFrame getFrame() {
        return frame;
    }
}
