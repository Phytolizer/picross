package mygl;

import java.util.ArrayList;
import java.util.List;

public class AllElements {
    private static List<Element> elementList = new ArrayList<>();
    private static List<Element> drawingList = new ArrayList<>();

    public static void update() {
        for (Element element : elementList) {
            element.update();
        }
    }

    public static void draw() {
        for (Element element : drawingList) {
            element.draw();
        }
    }

    static void add(Element element) {
        elementList.add(element);
        if (element.visible) {
            drawingList.add(element);
        }
    }

    static void remove(Element element) {
        elementList.remove(element);
        if (element.visible) {
            drawingList.remove(element);
        }
    }

    static void startDrawing(Element element) {
        drawingList.add(element);
    }

    static void stopDrawing(Element element) {
        drawingList.remove(element);
    }

    public static void auditDrawingList() {
        for (Element element : drawingList) {
            if (!element.visible) {
                System.out.println("WARNING: Non-visible element is being drawn! Removing.");
                drawingList.remove(element);
            }
        }
    }
}
