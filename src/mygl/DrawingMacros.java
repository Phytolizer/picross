package mygl;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DrawingMacros {
    public static void drawCenteredText(Graphics2D graphics2D, Font f, String s, int centerX, int centerY, boolean centerAlongYAxis) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics(f);
        Rectangle2D rect = fontMetrics.getStringBounds(s, graphics2D);
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        int drawX = centerX - width / 2;
        // Taking away 3/4 of the descent seems to create a better centered appearance
        int drawY = centerY + height / 2 - fontMetrics.getDescent() * 3 / 4;
        graphics2D.drawString(s, drawX, drawY);
    }
}
