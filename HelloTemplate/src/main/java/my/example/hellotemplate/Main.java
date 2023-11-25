package my.example.hellotemplate;

import io.github.jwharm.javagi.base.GErrorException;
import org.gnome.gio.Resource;

/**
 * The Main class registers the compiled gresource bundle and
 * runs a new HelloApplication instance.
 */
public class Main {

    /**
     * Run the HelloTemplate example
     * @param args passed to AdwApplication.run()
     * @throws GErrorException thrown while loading and registering the compiled resource bundle
     */
    public static void main(String[] args) throws GErrorException {
        var resource = Resource.load("src/main/resources/helloworld.gresource");
        resource.resourcesRegister();

        var app = HelloApplication.create();
        app.run(args);
    }
}
