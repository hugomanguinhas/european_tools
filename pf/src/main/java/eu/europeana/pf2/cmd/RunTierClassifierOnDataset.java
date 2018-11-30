/**
 * 
 */
package eu.europeana.pf2.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FileUtils;

import com.mongodb.MongoClient;

import eu.europeana.pf2.ParallelTierClassifier;
import eu.europeana.pf2.TierClassifier;
import eu.europeana.pf2.alg.AlgorithmUtils;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 20 Jul 2018
 */
public class RunTierClassifierOnDataset
{

    public static final void main(String[] args) throws IOException
    {
        if ( args.length < 1) { return; }
        Collection<String> filter = FileUtils.readLines(new File(args[0]));
        Collection<String> dss    = FileUtils.readLines(new File(args[1]));
        
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        TierClassifier t = new ParallelTierClassifier(
            cli, "europeana_production_publish_1", "pf2", Collections.EMPTY_LIST
          , 10, AlgorithmUtils.getMetadataAlgorithms());

        try
        {
            for ( String ds : dss )
            {
                ds = ds.trim();
                if ( ds.isEmpty() ) { continue; }

                t.classifyDataset(ds);
            }
        }
        finally { cli.close(); }
    }
}
