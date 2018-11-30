/**
 * 
 */
package eu.europeana.pf.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf.TierClassifier;
import eu.europeana.pf.TierReport;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 17 Oct 2016
 */
public class PubFrameworkCmd
{
    private static Logger _log = Logger.getLogger(TierClassifier.class);

    public static void main(String[] args) { new PubFrameworkCmd().process(args); }

    protected Properties    _props     = new Properties();
    protected HelpFormatter _formatter = new HelpFormatter();
    protected PrintStream   _ps        = System.out;

    public PubFrameworkCmd() { loadProperties("/etc/cmd/pub-framework.cfg"); }

    public void process(String[] args)
    {
        //Build Options
        Options opts = buildOptions();

        //Create parser
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(opts, args);
            if(line.hasOption("help")) { printUsage(opts); return; }

            printHeader();
            try                { process(line); }
            catch(Throwable t) { printError(t); }
            printFooter();
        }
        catch(ParseException exp) { printUsage(opts, exp.getMessage()); }
        catch(Throwable      t)   { printError(t);                      }
    }

    protected void loadProperties(String cfg)
    {
        try {
            InputStream is = PubFrameworkCmd.class.getResourceAsStream(cfg);
            _props.loadFromXML(is);
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    protected String getProperty(String key) { return _props.getProperty(key); }

    protected Options buildOptions()
    {
        Options options = new Options();
        addDefaultOptions(options, _props);
        addMongoOptions(options, _props);
        addInputOptions(options, _props);
        addTargetOptions(options, _props);
        return options;
    }

    protected void process(CommandLine line) throws Throwable
    {
        checkLogging(line);

        File target = getTarget(line, _props);
        if ( target == null ) { return; }

        TierReport report = handleCmd(line, target);
        if ( report == null ) { return; }

        PrintStream ps = new PrintStream(target);
        try
        {
            report.printPerMedia(ps);
            ps.println();
            report.printPerCountry(ps);
            ps.flush();
        }
        finally { ps.close(); }

        Map<Integer,PrintStream> map = report.getTierRecords();
        if ( map != null )
        {
            for ( PrintStream psTier : map.values() )
            {
                try { psTier.flush(); } finally { psTier.close(); }
            }
        }
    }

    protected TierReport handleCmd(CommandLine line, File file)
              throws IOException
    {
        File   dir  = file.getParentFile();
        String name = file.getName();
        
        Map<Integer,PrintStream> map = null;
        if(!line.hasOption("summary"))
        {
            map = new HashMap();
            for ( int i = 0; i <= 4; i++ )
            {
                String nameTier = name.replace(".csv", ".T" + i + ".csv");
                map.put(i, new PrintStream(new File(dir, nameTier)));
            }
        }
        TierReport r = new TierReport(map);

        MongoClient cli = getMongoClient(line, _props);

        try
        {
            Collection<String> filter = getFilter(line, _props);
            TierClassifier c = new TierClassifier(getMongoDatabase(cli, line
                                                                 , _props)
                                                , getThreads(line, _props)
                                                , filter);
            if ( line.hasOption("all") ) { return c.classifyAll(r); }

            if ( line.hasOption("uris") )
            {
                return c.classifyRecord(r, getURIs(line));
            }

            if ( line.hasOption("datasets") )
            {
                return c.classifyDatasets(r, line.getOptionValues("datasets"));
            }
        }
        finally { cli.close(); }

        return null;
    }
    

    protected void printUsage(Options opts)
    {
        String name = getProperty("info.name");
        _formatter.printHelp(name, opts, true);
    }

    protected void printUsage(Options opts, String msg)
    {
        printHeader();
        _ps.println(msg);
        _ps.println();
        printUsage(opts);
    }

    protected void printHeader() { _ps.print(getProperty("layout.header")); }
    protected void printFooter() { _ps.print(getProperty("layout.footer")); }

    protected void printError(Throwable t)
    {
        _log.error("Unexpected error", t);
        _ps.println("Error: " + t.getMessage());
        _ps.println();
        t.printStackTrace(_ps);
    }

    protected void checkLogging(CommandLine line)
    {
        if ( !line.hasOption("silent") ) { return; }
        Logger.getLogger(TierClassifier.class.getName()).setLevel(Level.OFF);
    }

    private String[] getURIs(CommandLine line)
    {
        String[] uris = line.getOptionValues("uris");
        List<String> list = new ArrayList(uris.length);
        for ( String uri : uris )
        {
            if ( uri.trim().isEmpty() ) { continue; }
            list.add(uri);
        }
        return (String[])list.toArray();
    }

    public static MongoClient getMongoClient(CommandLine line
                                           , Properties prop)
    {
        String defPort = prop.getProperty("defaults.port");
        String host = line.getOptionValue("host");
        if ( isProduction(host) ) { return getProductionClient(prop); }

        int    port = Integer.parseInt(line.getOptionValue("port", defPort));
        return new MongoClient(host, port);
    }

    public static boolean isProduction(String host)
    {
        return "production".equals(host);
    }

    public static MongoClient getProductionClient(Properties prop)
    {
        return new MongoClient(
            new MongoClientURI("mongodb://mongo1.eanadev.org:27017"
                             + ",mongo2.eanadev.org:27017"
                             + ",mongo3.eanadev.org:27017"
                             + "/europeana_production_publish_1"
                             + "?readPreference=secondary"));
    }

    public static MongoDatabase getMongoDatabase(MongoClient c, CommandLine line
                                               , Properties prop)
    {
        String defDb = prop.getProperty("defaults.col");
        return c.getDatabase(line.getOptionValue("col", defDb));
    }

    public static File getTarget(CommandLine line, Properties prop)
    {
        File file = new File(line.getOptionValue("target"));
        return ( file.isDirectory() || !file.getName().endsWith(".csv") ? null
                                                                        : file);
    }

    public static Collection<String> getFilter(CommandLine line, Properties prop)
           throws IOException
    {
        return FileUtils.readLines(new File(line.getOptionValue("filter")));
    }

    public static int getThreads(CommandLine line, Properties prop)
           throws IOException
     {
         try
         {
             String def = prop.getProperty("defaults.threads");
             return Integer.parseInt(line.getOptionValue("threads", def));
         }
         catch (NumberFormatException e) { return 10; }
     }


    /***************************************************************************
     * Public Methods - Options
     **************************************************************************/

    public static Options addMongoOptions(Options opts, Properties prop)
    {
        return opts.addOption(OptionBuilder.withArgName("host")
                .hasArg()
                .withDescription(prop.getProperty("info.option.host"))
                .isRequired()
                .create("host"))
            .addOption(OptionBuilder.withArgName("port")
                .hasArg()
                .withDescription(prop.getProperty("info.option.port"))
                .withType(Integer.class)
                .create("port"))
            .addOption(OptionBuilder.withArgName("col")
                .hasArg()
                .withDescription(prop.getProperty("info.option.col"))
                .create("col"))
            .addOption(OptionBuilder.withArgName("filter")
                .hasArg()
                .withDescription(prop.getProperty("info.option.filter"))
                .create("filter"))
            .addOption(OptionBuilder.withArgName("threads")
                .hasArg()
                .withDescription(prop.getProperty("info.option.threads"))
                .create("threads"))
            .addOption(new Option("summary"
                                , prop.getProperty("info.option.summary")))
            ;
    }

    public static Options addDefaultOptions(Options opts, Properties props)
    {
        opts.addOption(new Option("silent"
                     , props.getProperty("info.option.silent")))
            .addOption(new Option("help"
                     , props.getProperty("info.option.help")));
        return opts;
    }

    public static Options addTargetOptions(Options opts, Properties props)
    {
        return opts.addOption(OptionBuilder.withArgName("file")
                .hasArg()
                .withDescription(props.getProperty("info.option.target"))
                .isRequired()
                .create("target"));
    }

    public static Options addInputOptions(Options opts, Properties prop)
    {
        OptionGroup group = new OptionGroup()
            .addOption(OptionBuilder
                .withDescription(prop.getProperty("info.option.all"))
                .create("all"))
            .addOption(OptionBuilder
                .withArgName("uris")
                .hasArgs()
                .withValueSeparator(',')
                .withDescription(prop.getProperty("info.option.uris"))
                .create("uris"))
/*            .addOption(OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription(prop.getProperty("info.option.file"))
                .create("file"))
            .addOption(OptionBuilder
                .withArgName("query")
                .hasArg()
                .withDescription(prop.getProperty("info.option.search"))
                .create("search"))
*/            .addOption(OptionBuilder
                .withArgName("datasets")
                .hasArg()
                .withType(Integer.class)
                .withDescription(prop.getProperty("info.option.datasets"))
                .create("datasets"));
        group.setRequired(true);
        opts.addOptionGroup(group);
        return opts;
    }
}
