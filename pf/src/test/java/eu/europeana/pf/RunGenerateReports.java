/**
 * 
 */
package eu.europeana.pf;

import static eu.europeana.pf2.alg.AlgorithmUtils.METADATA_DIMENSIONS;
import static eu.europeana.pf2.alg.AlgorithmUtils.METADATA_TIER;
import static eu.europeana.pf2.alg.AlgorithmUtils.METADATA_TIER_MAX;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mongodb.MongoClient;

import eu.europeana.pf2.report.TierLogGenerator;
import eu.europeana.pf2.report.TierReportGenerator;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 21 Nov 2018
 */
public class RunGenerateReports
{
    public static final void main(String[] args) throws IOException
    {
        Collection<String> datasets = IOUtils.readLines(RunGenerateReports.class.getResourceAsStream("datasets.csv"));
        PrintStream ps = null;

        File dir = new File("D:\\work\\incoming\\tiers\\metadata\\testing");
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            TierReportGenerator report = new TierReportGenerator(cli, "pf2", "tiers");
            for ( String ds : datasets)
            {
                ps = new PrintStream(new File(dir, ds + ".report.csv"));
                try
                {
                    report.generateForDataset(new CSVPrinter(ps, CSVFormat.EXCEL), ds);
                }
                finally { ps.close(); }
            }

            ps = new PrintStream(new File(dir, "all.report.csv"));
            try 
            {
                report.generateForAll(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }

            ps = new PrintStream(new File(dir, "providers.report.csv"));
            try 
            {
                report.generateForAllProviders(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }
            

            /*
            TierLogGenerator log = new TierLogGenerator(cli, "pf2", "tiers");        
            for ( int i = 0; i <= METADATA_TIER_MAX; i++)
            {
                for ( String d : METADATA_DIMENSIONS )
                { 
                    ps = new PrintStream(new File(dir, "all." + d + "." + METADATA_TIER.get(i) + ".txt"));
                    try     { log.generateForAll(ps, d, i); }
                    finally { ps.close();                   }
                }
            }
            */
        }
        finally { cli.close(); }
    }
}
