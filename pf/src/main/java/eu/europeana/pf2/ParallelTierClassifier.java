/**
 * 
 */
package eu.europeana.pf2;

import static eu.europeana.ld.mongo.MongoEDMConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf2.alg.TierClassifierAlgorithm;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 20 Jul 2018
 */
public class ParallelTierClassifier extends TierClassifier
{
    private MongoDatabase _db;
    private int           _threads;

    /**
     * @param cli
     * @param dbs
     * @param dbt
     * @param filter
     * @throws IOException
     */
    public ParallelTierClassifier(MongoClient cli, String dbs, String dbt
         , Collection<String> filter, int threads
         , TierClassifierAlgorithm... classifiers) throws IOException
    {
        super(cli, dbs, dbt, filter, classifiers);
        _db      = cli.getDatabase(dbs);
        _threads = threads;
    }

    public void classifyAll() { classify(new BasicDBObject()); }

    public void classifyDataset(String datasets)
    {
        classify(BasicDBObject.parse(getDatasetQuery(datasets)));
    }

    private void classify(BasicDBObject query)
    {
        MongoCollection<Document> col = _db.getCollection("record");
        if ( col == null ) { return; }

        Phaser                phaser = new Phaser();
        ExecutorService       exec   = getExecutor();


        MongoCursor<Document> iter = col.find(query)
                                    .projection(new BasicDBObject("about", true))
                                    .noCursorTimeout(true)
                                    .batchSize(5000).iterator();
        try
        {
            phaser.register();
            while ( iter.hasNext() )
            {
                String id = iter.next().getString(ABOUT);
                phaser.register();
                exec.submit(new TierClassifierTask(id, phaser));
            }
        }
        finally { iter.close(); exec.shutdown(); }
    }

    private ExecutorService getExecutor()
    {
        return new ThreadPoolExecutor(
            _threads, _threads, 0L, TimeUnit.MILLISECONDS
          , new ArrayBlockingQueue<Runnable>(_threads * 10)
          , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private class TierClassifierTask implements Runnable
    {
        private Phaser _phaser;
        private String _id;

        public TierClassifierTask(String id, Phaser phaser)
        {
            _id     = id;
            _phaser = phaser;
        }

        public void run()
        {
            try     { classify(_id);                 }
            finally { _phaser.arriveAndDeregister(); }
        }
    }
}
