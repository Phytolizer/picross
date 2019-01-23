package mygl;

import static mygl.Alignment.*;

public class Coordinate {
    int x;
    int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public static Coordinate convertToScreenCoordinates(int coordX, int coordY, int sizeX, int sizeY, Alignment horizontalAlignment, Alignment verticalAlignment) {
        if (horizontalAlignment == CENTER) {
            coordX -= sizeX / 2;
        } else if (horizontalAlignment == RIGHT) {
            coordX -= sizeX;
        }
        if (verticalAlignment == CENTER) {
            coordY -= sizeY / 2;
        } else if(verticalAlignment == BOTTOM) {
            coordY -= sizeY;
        }
        return new Coordinate(coordX, coordY);
    }
}
