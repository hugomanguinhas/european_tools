/**
 * 
 */
package eu.europeana.pf.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.mongodb.MongoClient;

import eu.europeana.pf.ParallelTierClassifier;
import eu.europeana.pf.TierClassifier;
import eu.europeana.pf.alg.AlgorithmUtils;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 20 Jul 2018
 */
public class RunTierClassifierAll
{

    public static final void main(String[] args) throws IOException
    {
        if ( args.length < 1) { return; }
        Collection<String> filter = FileUtils.readLines(new File(args[0]));

        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        ParallelTierClassifier t = new ParallelTierClassifier(
            cli, "europeana_production_publish_1", "pf2", filter, 10
          , AlgorithmUtils.getMetadataAlgorithmsV2());

        try
        {
            t.classifyAll();
        }
        finally { cli.close(); }
    }
}
