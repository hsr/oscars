package net.es.oscars.utils.config;


import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public abstract class PropertyLoader
{
    /**
     * Looks up a resource named 'name' in the classpath. The resource must map
     * to a file with .properties extention. The name is assumed to be absolute
     * and can use either "/" or "." for package segment separation with an
     * optional leading "/" and optional ".properties" suffix. Thus, the
     * following names refer to the same resource:
     * <pre>
     * some.pkg.Resource
     * some.pkg.Resource.properties
     * some/pkg/Resource
     * some/pkg/Resource.properties
     * /some/pkg/Resource
     * /some/pkg/Resource.properties
     * </pre>
     *
     * @param name classpath resource name [may not be null]
     * @param loader classloader through which to load the resource [null
     * is equivalent to the application loader]
     *
     * @return resource converted to java.util.Properties [may be null if the
     * resource was not found and THROW_ON_LOAD_FAILURE is false]
     * @throws IllegalArgumentException if the resource was not found and
     * THROW_ON_LOAD_FAILURE is true
     */
    public static Properties loadProperties (String name, ClassLoader loader, String groupName, boolean stripPrefix)
    {
        if (name == null)
            throw new IllegalArgumentException ("null input: name");

        if (name.startsWith ("/"))
            name = name.substring (1);

        if (name.endsWith (SUFFIX))
            name = name.substring (0, name.length () - SUFFIX.length ());

        Properties tempProps = null;

        InputStream in = null;
        try {
            if (loader == null) loader = ClassLoader.getSystemClassLoader ();

            if (LOAD_AS_RESOURCE_BUNDLE) {
                name = name.replace ('/', '.');
                // Throws MissingResourceException on lookup failures:
                final ResourceBundle rb = ResourceBundle.getBundle (name,
                    Locale.getDefault (), loader);

                tempProps = new Properties ();
                for (Enumeration keys = rb.getKeys (); keys.hasMoreElements ();) {
                    final String key = (String) keys.nextElement ();
                    final String value = rb.getString (key);

                    tempProps.put (key, value);
                }
            } else {
                name = name.replace ('.', '/');

                if (! name.endsWith (SUFFIX))
                    name = name.concat (SUFFIX);

                // Returns null on lookup failures:
                System.out.println("name is " + name);
                in = loader.getResourceAsStream (name);
                if (in != null) {
                    tempProps = new Properties ();
                    tempProps.load (in); // Can throw IOException
                }
            }
        } catch (Throwable e) {
            System.out.println(e.toString());
            tempProps = null;
        } finally {
            if (in != null) try { in.close (); } catch (Throwable ignore) {}
        }

        if (THROW_ON_LOAD_FAILURE && (tempProps == null)) {
            throw new IllegalArgumentException ("could not load [" + name + "]"+
                " as " + (LOAD_AS_RESOURCE_BUNDLE
                ? "a resource bundle"
                : "a classloader resource"));
        }

        Properties result = new Properties();
        Enumeration e = tempProps.propertyNames();
        while (e.hasMoreElements()) {
            String propertyName = null;
            String elem = ( String )e.nextElement();
            if (elem.startsWith(groupName)) {
                if (stripPrefix) {
                    // get rid of period as well
                    propertyName = elem.substring(groupName.length()+1);
                } else {
                    propertyName = elem;
                }
                String propertyValue = tempProps.getProperty(elem);
                result.setProperty(propertyName, propertyValue);
            }
        }

        return result;
    }

    /**
     * A convenience overload of {@link #loadProperties(String, ClassLoader)}
     * that uses the current thread's context classloader.
     */
    public static Properties loadProperties (final String name, String groupName, boolean stripPrefix) {
        return loadProperties (name, Thread.currentThread().getContextClassLoader(), groupName, stripPrefix);
    }

    private static final boolean THROW_ON_LOAD_FAILURE = true;
    private static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
    private static final String SUFFIX = ".properties";
}