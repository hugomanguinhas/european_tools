/**
 * 
 */
package eu.europeana.pf;

import static eu.europeana.pf2.alg.AlgorithmUtils.*;

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
        PrintStream ps = null;

        File dir = new File("D:\\work\\incoming\\tiers\\metadata\\testing");
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            TierReportGenerator report = new TierReportGenerator(cli, "pf2", "tiers");

            /*
            ps = new PrintStream(new File(dir, "all.report.csv"));
            try 
            {
                report.generateForAll(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }

            ps = new PrintStream(new File(dir, "country.report.csv"));
            try 
            {
                report.genForAllCountries(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }

            ps = new PrintStream(new File(dir, "dataset.report.csv"));
            try 
            {
                report.genForAllDatasets(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }

            ps = new PrintStream(new File(dir, "data.providers.report.csv"));
            try 
            {
                report.genForAllDataProviders(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }

            ps = new PrintStream(new File(dir, "providers.report.csv"));
            try 
            {
                report.genForAllProviders(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }
            
            ps = new PrintStream(new File(dir, "type.report.csv"));
            try 
            {
                report.genForAllEDMTypes(new CSVPrinter(ps, CSVFormat.EXCEL));
            }
            finally { ps.close(); }
            */

            ps = new PrintStream(new File(dir, "tree.report.csv"));
            try 
            {
                report.genByCategories(new CSVPrinter(ps, CSVFormat.EXCEL)
                                     , "provider", "dataProvider", "ds");
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
