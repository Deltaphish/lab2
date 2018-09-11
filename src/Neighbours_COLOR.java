import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours_COLOR extends Application {


    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Color[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatifsfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.75f;

        List<int[]> vacant = new ArrayList<>();
        List<int[]> seeking = new ArrayList<>();

        int worldSize = world.length;

        // for each block in world check if satisfied

        for (int i = 0; i < worldSize; i++) {
            for (int j = 0; j < worldSize; j++) {
                if (world[i][j] == Color.WHITE) {
                    vacant.add(new int[]{i, j});
                } else {
                    State st = isActorSatisfied(i, j, threshold, worldSize);
                    if (st == State.UNSATISFIED) {
                        seeking.add(new int[]{i, j});
                    }
                }
            }
        }


        dispRatio(seeking, worldSize);

        changingPlaces(vacant, seeking);
    }

    private void changingPlaces(List<int[]> vacant, List<int[]> seeking) {
        Random rng = new Random();

        for (int i = 0; i != seeking.size() && vacant.size() > 0; i++) {
            int index = rng.nextInt(vacant.size());
            int[] vacantLot = vacant.remove(index);
            world[vacantLot[0]][vacantLot[1]] = world[seeking.get(i)[0]][seeking.get(i)[1]];
            world[seeking.get(i)[0]][seeking.get(i)[1]] = Color.WHITE;
        }
    }

    private void dispRatio(List<int[]> seeking, int worldSize) {
        int nUNSATISFIED = seeking.size();
        double satisficationRatio = 1 - (nUNSATISFIED / (worldSize * worldSize / 2.0f));
        out.println("Ratio: " + satisficationRatio);
    }


    // This method initializes the world variable with a random distribution of Colors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!
        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.2, 0.2, 0.1, 0.50};
        Color[] distC = {Color.RED, Color.BLUE, Color.GREEN, Color.WHITE};
        ArrayList<Color> population;
        // Number of locations (places) in world (square)

        int nLocations = 900;

        population = initPopulation(dist, distC, nLocations);

        //Collections.shuffle(population);

        int worldSize = (int)Math.sqrt(nLocations);

        world = new Color[worldSize][worldSize];



        Random rng = new Random();
        for(int i = 0; i != worldSize;i++){
            for (int j = 0; j != worldSize; j++) {
                world[i][j] = population.remove(rng.nextInt(population.size()));
            }
        }

        // Should be last
        fixScreenSize(nLocations);
    }

    private ArrayList<Color> initPopulation(double[] dist, Color[] distC, int nLocations) {
        ArrayList<Color>  result = new ArrayList<>();

        for (int i = 0; i < dist.length; i++) {
            for (int j = 0; j < nLocations * dist[i]; j++) {
                result.add(distC[i]);
            }
        }
        return result;
    }


    // ------- Methods ------------------

    // TODO write the methods here, implement/test bottom up

    State isActorSatisfied(int x,int y, double threshold, int worldSize){
        Color a = world[x][y];

        if(a == Color.WHITE){
            return State.NA;
        }

        int good = -1;
        int bad = 0;

        for (int i = -1; i != 2; i++) {
            for (int j = -1; j != 2; j++) {
                if (x + i >= 0 && x + i < worldSize && y+j >= 0 && y+j < worldSize) {

                    if (world[x + i][y + j] == a) {
                        good++;
                    } else if (world[x + i][y + j] != Color.WHITE) {
                        bad++;
                    }
                }
            }
        }
        if(bad+good != 0) {
            float ratio = (float) good / (float) (bad + good);
            //out.println(good + " / (" + bad + " + " + good + ") = " + ratio);
            if (ratio < (1.0f - threshold)) {
                return State.UNSATISFIED;
            }
        }
        return State.SATISFIED;
    }






    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    /*void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing
        int size = testWorld.length;

        // TODO test methods

        exit(0);
    }*/

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // *****   NOTHING to do below this row, it's JavaFX stuff  ******

    double width = 400;   // Size for window
    double height = 400;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Color[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                g.setFill(world[row][col]);
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
