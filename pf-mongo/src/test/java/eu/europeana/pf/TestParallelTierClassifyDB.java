/**
 * 
 */
package eu.europeana.pf;

import java.io.IOException;
import java.util.Collections;

import com.mongodb.MongoClient;

import eu.europeana.pf.ParallelTierClassifier;
import eu.europeana.pf.TierClassifier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 19 Jul 2018
 */
public class TestParallelTierClassifyDB
{
    public static final void main(String[] args) throws IOException
    {
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        TierClassifier t = new ParallelTierClassifier(
                cli, "europeana_production_publish_1", "pf", Collections.EMPTY_LIST, 1);

        long elapsed = System.currentTimeMillis();
        try
        {
            t.classifyDataset("2059206");
        }
        finally {
            elapsed = System.currentTimeMillis() - elapsed;
            cli.close();
        }
        System.out.println(elapsed);
    }
}
