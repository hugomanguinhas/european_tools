package eu.europeana.pf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf.media.MediaType;
import eu.europeana.pf.model.Record;
import static eu.europeana.util.MongoUtils.*;

/**
 * Hello world!
 *
 */
public class TierClassifier 
{
    private static Logger _log = Logger.getLogger(TierClassifier.class);
    private static int    DEF_BATCHSIZE = 100;

    private HashFunction  _hf  = Hashing.md5();
    private MongoDatabase _db;
    private Collection<String> _filter;
    private int                _threads;

    public TierClassifier(MongoDatabase db, int threads
                        , Collection<String> filter)
    { 
        _db      = db;
        _filter  = filter;
        _threads = threads;
    }


    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public TierReport classifyRecord(String... ids)
    {
        return classifyRecord(new TierReport(), ids);
    }

    public TierReport classifyRecord(TierReport report, String... ids)
    {
        for ( String id : ids )
        {
            return classify(report, new BasicDBObject("about", id));
        }
        return report;
    }

    public TierReport classifyDatasets(String... datasets)
    {
        return classifyDatasets(new TierReport(), datasets);
    }

    public TierReport classifyDatasets(TierReport report, String... datasets)
    {
        return classify(report, BasicDBObject.parse(getDatasetQuery(datasets)));
    }

    public TierReport classify(Bson filter)
    {
        return classify(new TierReport(), filter);
    }

    public TierReport classify(TierReport report, Bson filter)
    {
        return classify(_db.getCollection("record").find(filter), report);
    }

    public TierReport classifyAll()
    {
        return classifyAll(new TierReport());
    }

    public TierReport classifyAll(TierReport report)
    {
        return classify(_db.getCollection("record").find(), report);
    }


    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private String getDatasetQuery(String... datasets)
    {
        String str = null;
        for ( String dataset : datasets )
        {
            if ( dataset.trim().isEmpty() ) { continue; }

            str = (str == null ? "" : str + "|") + "(" + dataset + ")";
        }
        return "{'about': { $regex: '^/" + str + "/.*' }}";
    }

    private TierReport classify(FindIterable<Document> i, TierReport report)
    {
        Phaser                phaser = new Phaser();
        ExecutorService       exec   = getExecutor();
        MongoCursor<Document> iter   = i.noCursorTimeout(true)
                                        .batchSize(DEF_BATCHSIZE).iterator();
        try
        {
            phaser.register();

            while ( iter.hasNext() )
            {
                Record record = new Record(iter.next());
                if ( _filter.contains(record.getRecordID()) ) { continue; }

                phaser.register();
                exec.submit(new TierClassifierTask(record, report, phaser));
            }

            phaser.arriveAndAwaitAdvance();
        }
        finally { iter.close(); exec.shutdown(); }

        return report;
    }

    protected void classify(Record record, TierReport report)
    {
        MediaType mt  = record.getMediaType();
        if ( mt == null ) { return; }

        int tier = mt.getTierCalculator().getTier(record);
        report.newTierRecord(record.getRecordID(), tier);
        

        report.getMedia(mt).newTier(tier);

        String country = record.getCountry();
        if ( country == null ) { return; }

        report.getCountry(country).newTier(tier);

        return;
    }

    protected Record collectResources(Record record)
    {
        Document doc = record.getDocument();
        String   rid = record.getRecordID();
        for ( Document aggr : fetchDocuments(doc.get("aggregations")) )
        {
            record.setAggregation(aggr);
            for ( Document wr : fetchDocuments(aggr.get("webResources")) )
            {
                Document twr = getTechMetadata(rid, wr);
                if ( twr != null ) { record.addWebResource(wr, twr); }
            }
        }

        Document eAggr = fetch(_db, doc.get("europeanaAggregation"));
        record.setEuropeanaAggregation(eAggr);

        for ( Document proxy : fetchDocuments(doc.get("proxies")) )
        {
            if ( proxy.getBoolean("europeanaProxy") ) { continue; }
            record.setProxy(proxy);
        }

        return record;
    }

    private List<Document> fetchDocuments(Object o)
    {
        if ( !(o instanceof List) ) { return Collections.EMPTY_LIST; }

        List<DBRef>    lr = (List<DBRef>)o;
        List<Document> ld = new ArrayList(lr.size());
        for ( DBRef ref : lr )
        {
            Document doc = fetch(_db, ref);
            if (doc != null) { ld.add(doc); }
        }
        return ld;
    }

    private Document getTechMetadata(String recordID, Document docWr)
    {
        String   id  = getTechMetaID(recordID
                                   , docWr.getString("about"));
        return fetch(_db, "WebResourceMetaInfo", id);
    }

    private String getTechMetaID(String recordID, String wrID)
    {
        HashCode hashCode = _hf.newHasher()
                .putString(wrID, Charsets.UTF_8)
                .putString("-", Charsets.UTF_8)
                .putString(recordID, Charsets.UTF_8)
                .hash();
        return hashCode.toString();
    }

    private void logTier(String recordID, int tier)
    {
        _log.info("T" + tier + ": " + recordID);
    }

    private ExecutorService getExecutor()
    {
        return new ThreadPoolExecutor(_threads, _threads
                                    , 0L, TimeUnit.MILLISECONDS
                                    , new ArrayBlockingQueue<Runnable>(_threads * 10)
                                    , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private class TierClassifierTask implements Runnable
    {
        private Phaser     _phaser;
        private Record     _record;
        private TierReport _report;

        public TierClassifierTask(Record record, TierReport report
                                , Phaser phaser)
        {
            _record = record;
            _report = report;
            _phaser = phaser;
        }

        public void run()
        {
            try     { classify(collectResources(_record), _report); }
            finally { _phaser.arriveAndDeregister();                }
        }
    }
}
