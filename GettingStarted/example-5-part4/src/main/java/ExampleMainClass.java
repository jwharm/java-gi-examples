import org.javagi.base.GErrorException;
import org.gnome.gio.Resource;

public class ExampleMainClass {

  public static void main(String[] args) throws GErrorException {
    var resource = Resource.load("src/main/resources/exampleapp.gresource");
    resource.resourcesRegister();

    new ExampleApp().run(args);
  }
}
