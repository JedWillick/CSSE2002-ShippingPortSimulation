package portsim.display;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import portsim.cargo.BulkCargo;
import portsim.port.Quay;
import portsim.port.ShipQueue;
import portsim.ship.BulkCarrier;
import portsim.ship.ContainerShip;
import portsim.ship.NauticalFlag;
import portsim.ship.Ship;

import java.util.*;

/**
 * Subclass of the JavaFX Canvas to represent the main elements of the port graphically.
 * <p>
 * <b>NOTE: </b> The contents of this file do not necessarily follow best practice
 *
 * @given
 */
public class PortCanvas extends Canvas {

    /**
     * Width of an aircraft when drawn on the canvas, in pixels
     */
    private static final double SHIP_WIDTH = 100;
    /**
     * Height of an aircraft when drawn on the canvas, in pixels
     */
    private static final double SHIP_HEIGHT = SHIP_WIDTH - 20;
    /**
     * Height of a container when drawn on the canvas, in pixels
     */
    private static final double CONTAINER_HEIGHT = 5;
    /**
     * Width of a container when drawn on the canvas, in pixels
     */
    private static final double CONTAINER_WIDTH = CONTAINER_HEIGHT * 5 / 2;
    /**
     * View model containing the main model of the application
     */
    private final ViewModel viewModel;
    /**
     * Mapping of clickable regions (rectangles) to ships drawn on the canvas
     */
    private final Map<ClickableRegion, Ship> drawnShip;
    /**
     * Random number generator (Utility)
     */
    private Random random = new Random();

    /**
     * Creates a new PortCanvas with the given dimensions.
     *
     * @param viewModel view model to use to render elements on the canvas
     * @param width     width of the canvas, in pixels
     * @param height    height of the canvas, in pixels
     * @given
     */
    public PortCanvas(ViewModel viewModel, double width, double height) {
        super(width, height);

        this.viewModel = viewModel;
        this.drawnShip = new HashMap<>();

        setOnMouseClicked(event -> {
            /* Discard any click that is not a primary (left mouse button) click */
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            double x = event.getX();
            double y = event.getY();
            Ship clickedShip = null;
            for (Map.Entry<ClickableRegion, Ship> entry : drawnShip.entrySet()) {
                if (entry.getKey().wasClicked(x, y)) {
                    clickedShip = entry.getValue();
                }
            }
            viewModel.getSelectedShip().set(clickedShip);
            viewModel.registerChange();


            /* Ensures the canvas gains focus when it is clicked */
            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> requestFocus());
        });
    }

    /**
     * Draws all the relevant elements of the port onto the canvas.
     *
     * @given
     */
    public void draw() {
        this.drawnShip.clear();

        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.DODGERBLUE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawPort();
        drawShipQueue(this.viewModel.getPort().getShipQueue());
        drawQuays();
    }

    /* Draws the port */
    private void drawPort() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.GOLDENROD);
        double[] xs = new double[]{0, getWidth() - SHIP_WIDTH * 4,
                getWidth() - SHIP_WIDTH * 4 - 40, getWidth() - SHIP_WIDTH * 4 - 80, 0};
        double[] ys = new double[]{0 + SHIP_HEIGHT * 3 / 2, 0 + SHIP_HEIGHT * 3 / 2,
                getHeight() - SHIP_HEIGHT * 3 / 2 - 50, getHeight() - SHIP_HEIGHT * 3 / 2,
                getHeight() - SHIP_HEIGHT * 3 / 2};
        gc.fillPolygon(xs, ys, xs.length);

        final double roadHeight = CONTAINER_HEIGHT * 8;
        final double lineLength = 30;
        final double roadWidth = getWidth() - SHIP_WIDTH * 4 - 60;
        double roadStartY = SHIP_HEIGHT * 3 / 2 + 60;

        gc.setFill(Color.gray(0.2));
        gc.setFill(Color.BLACK);
        gc.fillRect(0, roadStartY, roadWidth, roadHeight);

        random.setSeed(123);
        for (int i = 0; i < roadWidth / lineLength - 1; ++i) {
            gc.setStroke(Color.WHITE);
            final double lineY = roadStartY + (roadHeight / 2);
            gc.strokeLine(lineLength / 2 + (i * lineLength), lineY,
                    lineLength + (i * lineLength), lineY);
            if (random.nextInt(3) == 0) {
                // truck
                drawTruck(lineLength / 2 + (i * lineLength), lineY);
            }
        }

        gc.setStroke(Color.BLACK);
        gc.strokeRect(20, roadStartY + 50, 150, 200);
        gc.strokeRect(220, roadStartY + 50, 150, 200);
        gc.strokeRect(420, roadStartY + 50, 150, 200);

        // Loaders at cargo depot
        gc.setFill(Color.MIDNIGHTBLUE);
        gc.fillRect(180, roadStartY + 100, 30, 20);
        gc.fillRect(150, roadStartY + 110, 90, 5);
        gc.fillRect(380, roadStartY + 100, 30, 20);
        gc.fillRect(350, roadStartY + 110, 90, 5);

        gc.fillRect(180, roadStartY + 150, 30, 20);
        gc.fillRect(150, roadStartY + 160, 90, 5);
        gc.fillRect(380, roadStartY + 150, 30, 20);
        gc.fillRect(350, roadStartY + 160, 90, 5);

        gc.setFill(Color.FIREBRICK);
        int num = viewModel.getPort().getCargo().size();
        int cols = 9;
        for (int i = 0; i < num * 4; i++) {
            gc.fillRect(220 + 8 + (i % cols) * (CONTAINER_WIDTH + 2),
                    roadStartY + 50 + 8 + (double) (i / cols + 1) * (CONTAINER_HEIGHT + 2),
                    CONTAINER_WIDTH, CONTAINER_HEIGHT);
        }

        // generate static containers in stable random pattern.
        gc.setFill(Color.BLUEVIOLET);
        random.setSeed(123);
        for (int i = 0; i < 24 * cols; i++) {
            if (random.nextInt(5) == 1) {
                continue;
            }
            gc.fillRect(20 + 8 + (i % cols) * (CONTAINER_WIDTH + 2),
                    roadStartY + 50 + 8 + (double) (i / cols + 1) * (CONTAINER_HEIGHT + 2),
                    CONTAINER_WIDTH, CONTAINER_HEIGHT);
            if (random.nextInt(6) == 1) {
                continue;
            }
            gc.fillRect(420 + 8 + (i % cols) * (CONTAINER_WIDTH + 2),
                    roadStartY + 50 + 8 + (double) (i / cols + 1) * (CONTAINER_HEIGHT + 2),
                    CONTAINER_WIDTH, CONTAINER_HEIGHT);
        }

        roadStartY = getHeight() - SHIP_HEIGHT * 3 + 10;

        gc.setFill(Color.gray(0.2));
        gc.setFill(Color.BLACK);
        gc.fillRect(0, roadStartY, roadWidth, roadHeight);

        for (int i = 0; i < roadWidth / lineLength - 1; ++i) {
            gc.setStroke(Color.WHITE);
            final double lineY = roadStartY + (roadHeight / 2);
            gc.strokeLine(lineLength / 2 + (i * lineLength), lineY,
                    lineLength + (i * lineLength), lineY);
            if (random.nextInt(6) == 0) {
                // truck
                drawTruck(lineLength / 2 + (i * lineLength), lineY);
            }
        }
    }

    // draws a truck at the position x,y
    private void drawTruck(double x, double y) {
        // truck
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.CHOCOLATE);
        gc.fillRect(x, y, CONTAINER_WIDTH + 10,
                CONTAINER_HEIGHT * 2);
        // container
        gc.setFill(Color.MIDNIGHTBLUE);
        gc.fillRect(x + 2, y + CONTAINER_HEIGHT / 2,
                CONTAINER_WIDTH, CONTAINER_HEIGHT);
        gc.setFill(Color.GRAY);
        gc.fillOval(x, y + CONTAINER_HEIGHT * 2, 8, 8);
        gc.fillOval(x + CONTAINER_WIDTH + 2,
                y + CONTAINER_HEIGHT * 2, 8, 8);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 2, y + CONTAINER_HEIGHT * 2 + 2,
                4, 4);
        gc.fillOval(x + CONTAINER_WIDTH + 4,
                y + CONTAINER_HEIGHT * 2 + 2, 4, 4);
        gc.setFill(Color.LIGHTSLATEGRAY);
        gc.fillRect(x + CONTAINER_WIDTH + 4,
                y + 2, 6, CONTAINER_HEIGHT * 2 - 4);
    }

    /* Draws the shipQueue */
    private void drawShipQueue(ShipQueue sq) {
        final double x = getWidth() - SHIP_WIDTH * 2 + 10;
        final int capacity = 7;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.ROYALBLUE);
        gc.fillRect(getWidth() - SHIP_WIDTH * 2 + 12, 0, getWidth() - SHIP_WIDTH * 2 + 12,
                getHeight());
        // vertical line
        gc.setFill(Color.BLACK);
        gc.fillRect(getWidth() - SHIP_WIDTH * 2 + 10, 0, 2, getHeight());

        // markers
        final double lineLength = SHIP_HEIGHT + 12;
        final double pad = 10;
        for (int i = 0; i < ((getHeight()) - lineLength - SHIP_HEIGHT) / lineLength + 1; ++i) {
            // Horizontal Lines
            gc.setFill(Color.BLACK);
            gc.fillRect(x, pad + 5 + i * lineLength, SHIP_WIDTH * 2 + 10,
                    2);
            // Fill text numbers
            gc.setFill(Color.WHITE);
            gc.setTextBaseline(VPos.CENTER);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFont(Font.font("monospace", FontWeight.BOLD, 12));
            gc.fillText(String.valueOf(i + 1),
                    x + 10, // 10 px right pad
                    (i + 1) * lineLength);
        }
        gc.setFill(Color.BLACK);
        gc.fillRect(x, getHeight() - pad - 5, SHIP_WIDTH * 2 + 10,
                2);
        // draw ships
        for (int j = 0; j < capacity && j < sq.getShipQueue().size(); j++) {
            drawShip(sq.getShipQueue().get(j), x + SHIP_WIDTH / 3,
                    pad + 12 + j * lineLength, true);
        }
    }

    /*
     * Draws a ship at the given position on the canvas.
     *
     * @param ship ship to draw
     * @param x x-coord of top left corner
     * @param y y-coord of top left corner
     */
    private void drawShip(Ship ship, double x, double y, boolean name) {
        Color textColor = Color.BLACK;

        this.drawnShip.put(new ClickableRegion(x + 10, y, SHIP_WIDTH * 4 / 3, SHIP_HEIGHT),
                ship);

        // hazardous cargo
        if (ship.getFlag() == NauticalFlag.BRAVO) {
            textColor = Color.RED;
        }

        // Is selected
        FontWeight fontWeight = FontWeight.NORMAL;
        if (Objects.equals(ship, viewModel.getSelectedShip().get())) {
            fontWeight = FontWeight.BOLD;
        }

        GraphicsContext gc = getGraphicsContext2D();
        // draw boat
        if (ship instanceof ContainerShip) {
            gc.setFill(Color.LIGHTSTEELBLUE);
        } else {
            gc.setFill(Color.AZURE);
        }
        // hull
        gc.fillOval(x + 10, y, SHIP_WIDTH, SHIP_HEIGHT);
        gc.fillRect(x + SHIP_WIDTH / 3 + 20, y, SHIP_WIDTH - 20, SHIP_HEIGHT);
        // Bridge
        gc.setFill(Color.GRAY);
        gc.fillRect(x + SHIP_WIDTH / 3 + SHIP_WIDTH - 20, y + 10, 10, SHIP_HEIGHT - 30);

        // contents
        if (ship instanceof ContainerShip) {
            gc.setFill(Color.FIREBRICK);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++) {
                    gc.fillRect(x + SHIP_WIDTH / 3 + 8 + i * (CONTAINER_WIDTH + 2),
                            y + 11 + j * (CONTAINER_HEIGHT + 2),
                            CONTAINER_WIDTH, CONTAINER_HEIGHT);
                }
            }
        } else if (ship instanceof BulkCarrier) {
            gc.setFill(Color.BLACK);
            BulkCargo c = ((BulkCarrier) ship).getCargo();
            if (c != null) {
                switch (c.getType()) {
                    case GRAIN:
                        gc.setFill(Color.KHAKI);
                        break;
                    case MINERALS:
                        gc.setFill(Color.LAVENDER);
                        break;
                    case COAL:
                        gc.setFill(Color.DARKSLATEGREY);
                        break;
                    case OIL:
                        gc.setFill(Color.INDIGO);
                        break;
                    case OTHER:
                        gc.setFill(Color.LIGHTGREEN);
                        break;
                }
            }
            gc.fillRect(x + SHIP_WIDTH / 3 + 10, y + 10, SHIP_WIDTH - 40, SHIP_HEIGHT - 30);
        }
        // Text
        gc.setFill(textColor);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("monospace", fontWeight, 12));

        if (name) {
            gc.fillText(ship.getName(),
                    x + SHIP_WIDTH / 3 + SHIP_WIDTH - 10,
                    y + SHIP_HEIGHT);
        }
    }

    /* Draws the quays and their ships */
    private void drawQuays() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.MIDNIGHTBLUE);
        List<Quay> quays = this.viewModel.getPort().getQuays();

        final double quayWidth = SHIP_WIDTH * 5 / 3;
        final double marginLeft = 5;
        for (int i = 0; i < quays.size(); i++) {
            Quay q = quays.get(i);
            if (i < 4) {
                double quayStartX = marginLeft + quayWidth * i;
                double quayStartY = SHIP_HEIGHT / 3;
                if (!q.isEmpty()) {
                    drawShip(q.getShip(), quayStartX, quayStartY, true);
                }
                for (int j = 0; j < 2; j++) {
                    gc.setFill(Color.MIDNIGHTBLUE);
                    double x = quayStartX + j * SHIP_WIDTH / 3 + SHIP_WIDTH / 3 + 20;
                    double y = quayStartY + SHIP_HEIGHT + 5;
                    gc.fillRect(x, y, 20, 30);
                    gc.fillRect(x + 6, y - 20, 4, 22);
                }
                gc.setFill(Color.BLACK);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                gc.fillText(String.format("%s: %d", q.getClass().getSimpleName(), q.getId()),
                        quayStartX + 2 * SHIP_WIDTH / 3 + 10,
                        quayStartY + SHIP_HEIGHT + 50);
            } else {
                double quayStartX = marginLeft + quayWidth * (i - 4);
                double quayStartY = getHeight() - SHIP_HEIGHT * 3 / 2;
                if (!q.isEmpty()) {
                    drawShip(q.getShip(), quayStartX, getHeight() - SHIP_HEIGHT * 4 / 3, true);
                }
                for (int j = 0; j < 2; j++) {
                    gc.setFill(Color.MIDNIGHTBLUE);
                    double x = quayStartX + j * SHIP_WIDTH / 3 + SHIP_WIDTH / 3 + 20;
                    double y = quayStartY - 25;
                    gc.fillRect(x, y, 20, 30);
                    gc.fillRect(x + 6, y + 25, 4, 22);
                }
                gc.setFill(Color.BLACK);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                gc.fillText(String.format("Quay: %d", q.getId()),
                        quayStartX + 2 * SHIP_WIDTH / 3 + 10,
                        quayStartY - 30);
            }

        }
    }

    /**
     * A class to represent a rectangular region on the canvas that responds to click events
     */
    private static class ClickableRegion {

        /**
         * X-coordinate of the region (top left)
         */
        private final double xcoord;
        /**
         * Y-coordinate of the region (top left)
         */
        private final double ycoord;
        /**
         * Width of the region, in pixels
         */
        private final double width;
        /**
         * Height of the region, in pixels
         */
        private final double height;

        /**
         * Creates a new clickable region with the given coordinates and dimensions
         *
         * @given
         */
        public ClickableRegion(double x, double y, double width, double height) {
            this.xcoord = x;
            this.ycoord = y;
            this.width = width;
            this.height = height;
        }

        /**
         * Returns whether the given click event's coordinates fall within this clickable
         * region
         *
         * @given
         */
        public boolean wasClicked(double clickX, double clickY) {
            return clickX >= this.xcoord && clickX <= this.xcoord + this.width
                    && clickY >= this.ycoord && clickY <= this.ycoord + this.height;
        }
    }

}
