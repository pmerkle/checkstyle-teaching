package edu.kit.checkstyle;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import com.puppycrawl.tools.checkstyle.api.Configuration;


/**
* Wrapper command line program for listing packages, imports etc.
* @author Florian Merz
**/
public final class ListThings
{
    /** Stop instances being created. */
    private ListThings()
    {
    }


    /**
     * Loops over the files specified listing the objects in it.
     * The exit code is the number of errors found in all the files.
     * @param aArgs the command line arguments
     **/
    public static void main(String[] aArgs)
    {
        final Configuration config = defaultConfig();

        final List<File> files = getFilesToProcess(aArgs);
        final Checker checker = createChecker(config);
        checker.process(files);
        checker.destroy();
        System.exit(0);
    }


    /**
     * Creates the Checker object.
     *
     * @param aConfig the configuration to use
     * @param aNosy the sticky beak to track what happens
     * @return a nice new fresh Checker
     */
    private static Checker createChecker(Configuration aConfig)
    {
        Checker c = null;
        try {
            c = new Checker();

            final ClassLoader moduleClassLoader =
                Checker.class.getClassLoader();
            c.setModuleClassLoader(moduleClassLoader);
            c.configure(aConfig);
        }
        catch (final Exception e) {
            System.out.println("Unable to create Checker: "
                               + e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        }
        return c;
    }

    /**
     * Determines the files to process.
     *
     * @param aLine the command line options specifying what files to process
     * @return list of files to process
     */
    private static List<File> getFilesToProcess(String[] values)
    {
        final List<File> files = new ArrayList<File>();
        for (String element : values) {
            traverse(new File(element), files);
        }

        return files;
    }


    /**
     * Traverses a specified node looking for files to check.
     * Found files are added to a specified list. Subdirectories are also
     * traversed.
     *
     * @param aNode the node to process
     * @param aFiles list to add found files to
     */
    private static void traverse(File aNode, List<File> aFiles)
    {
        if (aNode.canRead()) {
            if (aNode.isDirectory()) {
                final File[] nodes = aNode.listFiles();
                for (File element : nodes) {
                    traverse(element, aFiles);
                }
            }
            else if (aNode.isFile()) {
                aFiles.add(aNode);
            }
        }
    }

    /**
     * Create the default configuration object.
     *
     * @return configuration object hardcoding the checks to run
     */
    private static Configuration defaultConfig()
    {
        DefaultConfiguration root = new DefaultConfiguration("com.puppycrawl.tools.checkstyle.Checker");
        DefaultConfiguration treeWalker = new DefaultConfiguration("com.puppycrawl.tools.checkstyle.TreeWalker");
        root.addChild(treeWalker);

        List<String> checks = new ArrayList<String>();
        checks.add("edu.kit.checkstyle.checks.lists.PackageList");
        checks.add("edu.kit.checkstyle.checks.lists.ImportList");
        checks.add("edu.kit.checkstyle.checks.lists.ClassList");
        checks.add("edu.kit.checkstyle.checks.lists.EnumList");
        checks.add("edu.kit.checkstyle.checks.lists.MethodList");
        for (String check : checks) {
            DefaultConfiguration config = new DefaultConfiguration(check);
            treeWalker.addChild(config);
        }

        root.addChild(new DefaultConfiguration("edu.kit.checkstyle.listeners.ListListener"));
        return root;
    }


	/**
	 * Print the configuration object.
	 *
	 * @param config the configuration to be printed
	 */
    private static void print(Configuration config)
    {
        print(config, new String(""));
    }


   	/**
	 * Print the configuration object.
	 *
	 * @param config the configuration to be printed
	 * @param indent the indentation string
	 */
    private static void print(Configuration config, String indent)
    {
        System.out.println(indent + config.getName());

        for (String name : config.getAttributeNames()) {
            try {
                System.out.println(" " + name + "->" + config.getAttribute(name));
            } catch (Exception e) {
            }
        }

        for (Configuration child : config.getChildren()) {
            print(child, indent + "  ");
        }
    }
}
