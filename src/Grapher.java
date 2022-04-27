import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Grapher {
    private static Expression expression;
    private static double threshold;
    private static boolean implicit;
    private static JFrame frame;
    private static JPanel canvas;
    private static Color MARKER = new Color(232,232,232);
    private static double offsetX = 0;
    private static double offsetY = 0;
    private static Point lastMousePos;
    private static double SCALE = 0.01;
    private static int samples = 250;
    private static final int sqSize = 10;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int UP = 3;

    public static void main(String[] args) {
        frame = new JFrame("Grapher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,600);
        JPanel gui = new JPanel();
        JTextField input = new JTextField("x^2", 20);
        JButton inputButton = new JButton("Graph it");
        inputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String raw = input.getText();
                if (raw == "") {
                    showError("Input cannot be blank!");
                    return;
                }
                try {
                    String[] split = new String[2];
                    if (raw.indexOf('=') != -1) {
                        split = raw.split("=");
                        expression = new ExpressionBuilder(split[0]).variable("x").variable("y").build();
                        threshold = Double.parseDouble(split[1]);
                        implicit = true;
                    }
                    else {
                        expression = new ExpressionBuilder(raw).variable("x").build();
                        implicit = false;
                    }
                } catch (Exception e) {
                    showError(e.getMessage());
                    return;
                }
                regraph();
            }
        });
        gui.add(input);
        gui.add(inputButton);
        frame.add(gui, BorderLayout.SOUTH);
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                lastMousePos = e.getPoint();
                //showError("" + pixToCart(e.getX(), e.getY())[0]);
            }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                offsetX += (lastMousePos.x - e.getX()) / (double) frame.getWidth() * 2 * frame.getWidth() * SCALE;
                offsetY += (e.getY() - lastMousePos.y) / (double) frame.getHeight() * 2 * frame.getHeight() * SCALE;
                lastMousePos = e.getPoint();
                regraph();
            }
        });
        frame.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                SCALE *= Math.pow(1.1,e.getWheelRotation());
                regraph();
            }
        });
        canvas = new JPanel();
        canvas.setBackground(Color.WHITE);
        frame.add(canvas);
        frame.setVisible(true);
    }
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null,message,"Error",JOptionPane.ERROR_MESSAGE);
    }
    private static void implicitGraph(){
        /*dimension [x][y]*/
        int xn = frame.getWidth()/sqSize;
        int yn = frame.getHeight()/sqSize;
        double[][] grid = new double[xn][yn];
        for (int i = 0; i < xn; i++) {
            for (int j = 0; j < yn; j++) {
                var temp = pixToCart(i * sqSize, j * sqSize);
                grid[i][j] = expression.setVariable("x", temp[0]).setVariable("y", temp[1]).evaluate();
            }
        }
        marchSquare(grid);
    }
    private static void graph(){
        double leftBound = offsetX - frame.getWidth() * SCALE;
        double rightBound = offsetX + frame.getWidth() * SCALE;
        double downBound = offsetY - frame.getHeight() * SCALE;
        double upBound = offsetY + frame.getHeight() * SCALE;

        double[] bounds = {leftBound, rightBound, downBound, upBound};
        double widthSize = rightBound - leftBound;
        double dx = widthSize / (samples + 1);
        Graphics graphics = canvas.getGraphics();
        graphics.setColor(Color.RED);
        for (int i = 0; i < samples + 2; i++) {
            double x = leftBound + (i * dx);
            double y = expression.setVariable("x",x).evaluate();
            if ((y >= downBound) && (y <= upBound)) {
                Point p = cartToPix(x, y, bounds);
                graphics.drawOval(p.x, p.y, 1, 1);
            }
        }
        canvas.paintComponents(graphics);
    }
    private static void clear() {
        Graphics graphics = canvas.getGraphics();
        //Color temp = graphics.getColor();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        //graphics.setColor(temp);
    }
    private static void regraph() {
        clear();
        drawAxis();
        if (implicit) {
            implicitGraph();
        }
        else {
            graph();
        }
    }
    private static Point cartToPix(double leftBound, double rightBound, double downBound, double upBound, double x, double y) {
        double widthSize = rightBound - leftBound;
        double heightSize = upBound - downBound;
        double yTemp = (y - downBound) / heightSize;
        yTemp *= frame.getHeight();
        double xTemp = (x - leftBound) / widthSize;
        xTemp *= frame.getWidth();

        return new Point((int) Math.round(xTemp), frame.getHeight() - (int) Math.round(yTemp));
    }
    private static Point cartToPix(double x, double y, double... bounds) {
        return cartToPix(bounds[0], bounds[1], bounds[2], bounds[3], x, y);
    }
    private static double[] pixToCart(int x, int y) {
        double leftBound = offsetX - frame.getWidth() * SCALE;
        double rightBound = offsetX + frame.getWidth() * SCALE;
        double downBound = offsetY - frame.getHeight() * SCALE;
        double upBound = offsetY + frame.getHeight() * SCALE;

        double widthSize = rightBound - leftBound;
        double heightSize = upBound - downBound;
        double xTemp = (x / (double) frame.getWidth()) * widthSize + leftBound;
        double yTemp = upBound - (y / (double) frame.getHeight()) * heightSize;
        return new double[]{xTemp, yTemp};
    }
    private static void drawAxis() {
        drawAxis(getBounds());
    }
    private static void drawAxis(double... bounds) {
        Graphics graphics = canvas.getGraphics();
        graphics.setColor(Color.BLACK);
        //draw vertical marker line
        Point center = cartToPix( 0, 0, bounds);
        if (bounds[LEFT] <= 0 && bounds[RIGHT] >= 0) {
            graphics.drawLine(center.x, 0, center.x, frame.getHeight());
        }
        if (bounds[DOWN] <= 0 && bounds[UP] >= 0) {
            graphics.drawLine(0, center.y, frame.getWidth(), center.y);
            graphics.fillPolygon(new int[]{0, 10, 10}, new int[]{center.y, center.y - 3, center.y + 3}, 3);
            graphics.fillPolygon(new int[]{frame.getWidth() - 10, frame.getWidth() - 20, frame.getWidth() - 20}, new int[]{center.y, center.y + 3, center.y - 3}, 3);
        }
    }
    private static void marchSquare(double[][] grid) {
        Graphics graphics = canvas.getGraphics();
        graphics.setColor(Color.BLUE);
        for (int i = 0; i < grid.length - 1; i++) {
            for (int j = 0; j < grid[i].length - 1; j++) {
                int count = 0;
                Point[] arr = new Point[4];
                if ((grid[i][j] > threshold) != (grid[i + 1][j] > threshold)) {
                    double a = grid[i][j];
                    double b = grid[i + 1][j];
                    double span = (a - b);
                    double lerp = Math.abs((threshold - a) / span);
                    arr[count] = new Point((int) Math.round(i * sqSize + lerp * sqSize), j * sqSize);
                    count++;
                }
                if ((grid[i + 1][j] > threshold) != (grid[i + 1][j + 1] > threshold)) {
                    double a = grid[i + 1][j];
                    double b = grid[i + 1][j + 1];
                    double span = (a - b);
                    double lerp = Math.abs((threshold - a) / span);
                    arr[count] = new Point((i + 1) * sqSize, (int) Math.round(j * sqSize + lerp * sqSize));
                    count++;
                }
                if ((grid[i + 1][j + 1] > threshold) != (grid[i][j + 1] > threshold)) {
                    double a = grid[i + 1][j + 1];
                    double b = grid[i][j + 1];
                    double span = (a - b);
                    double lerp = Math.abs((threshold - a) / span);
                    arr[count] = new Point((int) Math.round((i + 1) * sqSize - lerp * sqSize), (j + 1) * sqSize);
                    count++;
                }
                if ((grid[i][j + 1] > threshold) != (grid[i][j] > threshold)) {
                    double a = grid[i][j + 1];
                    double b = grid[i][j];
                    double span = (a - b);
                    double lerp = Math.abs((threshold - a) / span);
                    arr[count] = new Point(i * sqSize, (int) Math.round((j + 1) * sqSize - lerp * sqSize));
                    count++;
                }
                if (count == 2) {
                    graphics.drawLine(arr[0].x, arr[0].y, arr[1].x, arr[1].y);
                }
            }
        }
    }
    private static double[] getBounds() {
        double leftBound = offsetX - frame.getWidth() * SCALE;
        double rightBound = offsetX + frame.getWidth() * SCALE;
        double downBound = offsetY - frame.getHeight() * SCALE;
        double upBound = offsetY + frame.getHeight() * SCALE;

        return new double[] {leftBound, rightBound, downBound, upBound};
    }
}
