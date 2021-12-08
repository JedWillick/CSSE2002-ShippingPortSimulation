package portsim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import portsim.display.View;
import portsim.display.ViewModel;
import portsim.util.BadEncodingException;

import java.io.IOException;


/**
 * Entry point for the GUI of the Port Simulation.
 *
 * @given
 */
public class Launcher extends Application {
    /**
     * <b>Note</b>: you do not need to write this constructor, it is generated automatically and
     * cannot be removed from the Javadoc.
     */
    public Launcher() {
    }

    /**
     * Launches the GUI.
     * <p>
     * Usage: {@code port_file}
     * <p>
     * Where
     * <ul>
     * <li>{@code port_file} is the path to the file containing the port</li>
     * </ul>
     *
     * @param args command line arguments
     * @given
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: port_file\n");
            System.err.println("You did not specify the names of the required save file"
                    + " from which to load.");
            System.err.println("To do this, you need to add the command line "
                    + "argument to your "
                    + "program in IntelliJ.");
            System.err.println("Go to \"Run > Edit Configurations > Launcher > Program Arguments\" "
                    + "and add the paths to your file to the text box.\n");
            System.err.println("Example: saves/port_default.txt");
            System.exit(1);
        }
        Application.launch(Launcher.class, args);
    }

    /**
     * {@inheritDoc}
     *
     * @given
     */
    @Override
    public void start(Stage stage) {
        View view;
        try {
            view = new View(stage, new ViewModel(getParameters().getRaw().get(0)));
        } catch (BadEncodingException | IOException e) {
            System.err.println("Error loading from file. Stack trace below:");
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }

        view.run();
    }
}
