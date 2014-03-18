////////////////////////////////////////////////////////////////////////////////
// shamelessly copied from the official checkstyle repository
//
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2013 Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
////////////////////////////////////////////////////////////////////////////////
package edu.kit.checkstyle;

import edu.kit.checkstyle.listeners.QualifiedListener;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Utils;

import com.google.common.collect.Lists;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

/**
* Wrapper command line program for the Checker.
* @author Oliver Burn
**/
public final class Main
{
    /** the options to the command line */
    private static final Options OPTS = new Options();

    /** Stop instances being created. */
    private Main()
    {
    }

    /**
     * Loops over the files specified checking them for errors. The exit code
     * is the number of errors found in all the files.
     * @param aArgs the command line arguments
     **/
    public static void main(String[] aArgs)
    {
        // parse the parameters
        final CommandLineParser clp = new PosixParser();
        CommandLine line = null;
        try {
            line = clp.parse(OPTS, aArgs);
        }
        catch (final ParseException e) {
            e.printStackTrace();
            usage();
        }
        assert line != null;

        // setup the properties
        final Properties properties = System.getProperties();

        final Configuration config = loadConfig(properties);

        AuditListener listener = new QualifiedListener();
        final List<File> files = getFilesToProcess(line);
        final Checker checker = createChecker(config);
        checker.addListener(listener);
        checker.process(files);
        checker.destroy();
        System.exit(0);
    }

    /**
     * Creates the Checker object.
     *
     * @param aConfig the configuration to use
     * @return a nice new fresh Checker
     */
    private static Checker createChecker(Configuration aConfig)
    {
        Checker checker = null;
        try {
            checker = new Checker();

            final ClassLoader moduleClassLoader =
                Checker.class.getClassLoader();
            checker.setModuleClassLoader(moduleClassLoader);
            checker.configure(aConfig);
        }
        catch (final Exception e) {
            System.out.println("Unable to create Checker: "
                               + e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        }
        return checker;
    }

    /**
     * Determines the files to process.
     *
     * @param aLine the command line options specifying what files to process
     * @return list of files to process
     */
    private static List<File> getFilesToProcess(CommandLine aLine)
    {
        final List<File> files = Lists.newLinkedList();
        if (aLine.hasOption("r")) {
            final String[] values = aLine.getOptionValues("r");
            for (String element : values) {
                traverse(new File(element), files);
            }
        }

        final String[] remainingArgs = aLine.getArgs();
        for (String element : remainingArgs) {
            files.add(new File(element));
        }

        if (files.isEmpty() && !aLine.hasOption("r")) {
            System.out.println("Must specify files to process");
            usage();
        }
        return files;
    }

    /**
     * Loads the configuration file. Will exit if unable to load.
     *
     * @param aLine specifies the location of the configuration
     * @param aProps the properties to resolve with the configuration
     * @return a fresh new configuration
     */
    private static Configuration loadConfig(Properties properties)
    {
        try {
            return ConfigurationLoader.loadConfiguration(
                    new InputSource(System.in), new PropertiesExpander(properties), true);
        }
        catch (final CheckstyleException e) {
            System.out.println("Error loading configuration file");
            e.printStackTrace(System.out);
            System.exit(1);
            return null; // can never get here
        }
    }

    /** Prints the usage information. **/
    private static void usage()
    {
        final HelpFormatter hf = new HelpFormatter();
        hf.printHelp(
            "java "
                + Main.class.getName()
                + " [options] -c <config.xml> file...",
            OPTS);
        System.exit(1);
    }

    /**
     * Traverses a specified node looking for files to check. Found
     * files are added to a specified list. Subdirectories are also
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
     * Loads properties from a File.
     * @param aFile the properties file
     * @return the properties in aFile
     */
    private static Properties loadProperties(File aFile)
    {
        final Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(aFile);
            properties.load(fis);
        }
        catch (final IOException ex) {
            System.out.println("Unable to load properties from file: "
                + aFile.getAbsolutePath());
            ex.printStackTrace(System.out);
            System.exit(1);
        }
        finally {
            Utils.closeQuietly(fis);
        }

        return properties;
    }
}
