package mygl;

import java.awt.*;
import java.util.function.Supplier;

public class ButtonElement extends Element {
    private final Color OVERLAY_HOVERING = new Color(0, 0, 0, 64);
    private final Color OVERLAY_CLICKING = new Color(0, 0, 0, 128);
    private final double FILL_PROPORTION = 2d / 3;
    private boolean clicking = false;
    private boolean hovering = false;
    private ClickHandler clickHandler = null;
    private Color backgroundColor;
    private Color borderColor;
    private Color textColor;
    private Font textFont;
    private String text;
    private Supplier<Boolean> isLmbDown;
    private Supplier<Point> mousePosition;
    
    public ButtonElement(Graphics graphics) {
        super(graphics.getGraphics2D());
        text = "";
        backgroundColor = new Color(255, 255, 255, 255);
        borderColor = new Color(0, 0, 0, 255);
        textColor = new Color(0, 0, 0, 255);
        isLmbDown = () -> graphics.getFrame().buttonDown(1);
        mousePosition = () -> graphics.getFrame().getMousePosition();
    }
    
    public void draw() {
        graphics2D.setColor(backgroundColor);
        graphics2D.fillRect(screenX + 1, screenY + 1, sizeX - 2, sizeY - 2);
        graphics2D.setColor(borderColor);
        graphics2D.drawRect(screenX, screenY, sizeX, sizeY);
        graphics2D.setColor(textColor);
        DrawingMacros.drawCenteredText(graphics2D, textFont, text, screenX + sizeX / 2, screenY + sizeY / 2, true);
        // Check mouse state and update
        Color overlay = null;
        if (inBounds(mousePosition.get())) {
            hovering = true;
            if (isLmbDown.get()) {
                clicking = true;
                overlay = OVERLAY_CLICKING;
            } else {
                overlay = OVERLAY_HOVERING;
                if (clicking) {
                    clicking = false;
                    if (clickHandler != null) {
                        clickHandler.execute();
                    }
                }
            }
        }
        graphics2D.setColor(overlay);
        graphics2D.fillRect(screenX, screenY, sizeX, sizeY);
    }
    
    public void resize(int sizeX, int sizeY) {
        super.resize(sizeX, sizeY);
        resizeFont();
    }
    
    private void resizeFont() {
        if (text.equals("")) {
            return;
        }
        int textWidth = 0;
        for (float fontSize = 1; textWidth < FILL_PROPORTION * sizeX; fontSize++) {
            textFont = textFont.deriveFont(fontSize);
            FontMetrics fontMetrics = graphics2D.getFontMetrics(textFont);
            textWidth = fontMetrics.stringWidth(text);
        }
        if (textFont.getSize() > sizeY * FILL_PROPORTION) {
            textFont = textFont.deriveFont((float) (FILL_PROPORTION * sizeY));
        }
    }
    
    public void setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
