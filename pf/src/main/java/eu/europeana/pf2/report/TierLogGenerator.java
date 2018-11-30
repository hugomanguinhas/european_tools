/**
 * 
 */
package eu.europeana.pf2.report;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.Converters;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;





import eu.europeana.pf2.db.TierEntryConstants;

import static eu.europeana.ld.mongo.MongoUtils.DEF_BATCHSIZE;
import static eu.europeana.pf2.alg.AlgorithmUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TierLogGenerator implements TierEntryConstants
{
    private MongoCollection<Document> _col;

    public TierLogGenerator(MongoClient cli, String dbs, String col)
           throws IOException
    {
        _col = cli.getDatabase(dbs).getCollection(col);
    }


    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public void generateForAll(PrintStream ps, String dim, int level) throws IOException
    {
        generateForSearch(ps, null, dim, level);
    }

    public void generateForDataset(PrintStream ps, String ds
                                 , String dim, int level) throws IOException
    {
        generateForSearch(ps, createDatasetCriteria(ds), dim, level);
    }

    public void generateForSearch(PrintStream ps, Bson filter
                                , String dim, int level) throws IOException
    {
        BasicDBObject obj = createQuery(dim, level);
        if ( filter != null ) { obj.append("_id", filter); }

        MongoCursor<Document> iter = _col.find(obj).projection(new BasicDBObject("_id", 1))
                                                   .noCursorTimeout(true)
                                                   .batchSize(DEF_BATCHSIZE).iterator();
        try {
            while ( iter.hasNext() )
            {
                Document doc = iter.next();
                ps.println("http://data.europeana.eu/item" + doc.get("_id"));
            }
        }
        finally { iter.close(); }
    }


    /***************************************************************************
     * Private Methods
     **************************************************************************/

    protected BasicDBObject createQuery(String tier, int level)
    {
        return new BasicDBObject("tiers." + tier, level);
    }

    protected BasicDBObject createDatasetCriteria(String ds)
    {
        return new BasicDBObject("$regex", "^/" + ds + "/.*");
    }

    public static final void main(String[] args) throws IOException
    {
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            new TierLogGenerator(cli, "pf2", "tiers")
                .generateForDataset(System.out, "2048202", "metadata", 1);
        }
        finally { cli.close(); }
    }
}
