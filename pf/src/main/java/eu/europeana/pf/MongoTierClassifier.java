package eu.europeana.pf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.europeana.ld.ResourceCallback;
import eu.europeana.ld.mongo.MongoEDMHarvester;
import eu.europeana.pf.media.MediaType;
import eu.europeana.pf.model.Record;
import eu.europeana.pf2.alg.TierClassifierAlgorithm;
import static eu.europeana.util.MongoUtils.*;

/**
 * Hello world!
 *
 */
public class MongoTierClassifier 
{
    private static Logger _log = Logger.getLogger(MongoTierClassifier.class);

    private MongoEDMHarvester _harvester;
    private TierClassifier2   _classifier;

    public MongoTierClassifier(MongoClient cli, MongoDatabase db
                             , TierConfig cfg)
    { 
        _harvester  = new MongoEDMHarvester(cli, db, null, true);
        _classifier = new TierClassifier2(cfg);
    }


    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public TierReport2 classifyRecord(String... ids)
    {
        return classifyRecord(new TierReport2(), ids);
    }

    public TierReport2 classifyRecord(TierReport2 report, String... ids)
    {
        for ( String id : ids )
        {
            return classify(report, new BasicDBObject("about", id));
        }
        return report;
    }

    public TierReport2 classifyDatasets(String... datasets)
    {
        return classifyDatasets(new TierReport2(), datasets);
    }

    public TierReport2 classifyDatasets(TierReport2 report, String... datasets)
    {
        return classify(report, BasicDBObject.parse(getDatasetQuery(datasets)));
    }

    public TierReport2 classify(Bson filter)
    {
        return classify(new TierReport2(), filter);
    }

    public TierReport2 classify(TierReport2 rpt, Bson filter)
    {
        _harvester.harvestBySearch(filter, new ClassifierHandler(rpt));
        return rpt;
    }

    public TierReport2 classifyAll()
    {
        return classifyAll(new TierReport2());
    }

    public TierReport2 classifyAll(TierReport2 rpt)
    {
        _harvester.harvestAll(new ClassifierHandler(rpt));
        return rpt;
    }


    /***************************************************************************
     * Private Classes
     **************************************************************************/

    private class ClassifierHandler implements ResourceCallback<Resource>
    {
        private TierReport2 _rpt;

        public ClassifierHandler(TierReport2 rpt) { _rpt = rpt; }

        public void handle(String id, Resource r, Status s)
        {
            _classifier.classify(r.getModel(), _rpt);
        }
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
}
