/**
 * 
 */
package eu.europeana.pf.report;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import eu.europeana.pf.db.TierEntryConstants;
import eu.europeana.pf.metadata.MetadataDimension;
import eu.europeana.pf.metadata.MetadataTier;
import static eu.europeana.pf.alg.AlgorithmUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TierReportGenerator extends TierAnalysisGenerator 
                                 implements TierEntryConstants
{
    private MongoCollection<Document> _col;

    public TierReportGenerator(MongoClient cli, String dbs, String col)
           throws IOException
    {
        _col = cli.getDatabase(dbs).getCollection(col);
    }


    /***************************************************************************
     * Public Methods
     **************************************************************************/

    public void genForAllDatasets(CSVPrinter p) throws IOException
    {
        genByCategory(p, "ds", "Dataset");
    }

    public void genForAllProviders(CSVPrinter p) throws IOException
    {
        genByCategory(p, "provider", "Aggregator");
    }

    public void genForAllDataProviders(CSVPrinter p) throws IOException
    {
        genByCategory(p, "dataProvider", "Data Provider");
    }

    public void genForAllCountries(CSVPrinter p) throws IOException
    {
        genByCategory(p, "country", "Country");
    }

    public void genForAllEDMTypes(CSVPrinter p) throws IOException
    {
        genByCategory(p, "type", "EDM Type");
    }


    public void generateForDataset(CSVPrinter p, String ds) throws IOException
    {
        generateForSearch(p, createDatasetCriteria(ds));
    }

    public void generateForProvider(CSVPrinter p, String provider) throws IOException
    {
//        generateForSearch(p, );
    }

    public void generateForAll(CSVPrinter p) throws IOException
    {
        generateForSearch(p, new BasicDBObject());
    }

    public void generateForSearch(CSVPrinter p, BasicDBObject filter) throws IOException
    {
        printHead(p);
        long total = getCount(filter);
        for ( int i = 0; i <= METADATA_TIER_MAX; i++)
        {
            p.print(METADATA_TIER.get(i));
            for ( MetadataDimension d : MetadataDimension.values() )
            {
                printResult(p, getCount(filter, d.getID(), i), total);
            }
            p.println();
        }
    }


    /***************************************************************************
     * Protected Methods
     **************************************************************************/

    @Override
    protected MongoCollection<Document> getCollection() { return _col; }


    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private void genByCategory(CSVPrinter p, String prop, String label) throws IOException
    {
        MongoCursor<String> c = _col.distinct(prop, String.class)
                                    .batchSize(BATCH_SIZE).iterator();
        try
        {
            BasicDBObject filter = new BasicDBObject();

            printHead(p, label);
            while (c.hasNext())
            {
                String value = c.next();
                filter.put(prop, value);
                p.print(value);
                printTreeRow(p, filter);
                p.println();
            }
        }
        finally { c.close(); }
    }

    @Override
    protected void printTreeRow(CSVPrinter p, BasicDBObject filter) 
              throws IOException
    {
        long total = getCount(filter);
        for ( MetadataDimension d : MetadataDimension.values() )
        {
            int i = 0;
            for ( MetadataTier tier : MetadataTier.values() )
            {
                printResult(p, getCount(filter, d.getID(), i++), total);
            }
        }
    }

    private void printHead(CSVPrinter p) throws IOException
    {
        p.print("tier");
        for ( MetadataDimension d : MetadataDimension.values() )
        { 
            p.print(d.getLabel());
        }
        p.println();
    }


    protected long getCount(BasicDBObject filter)
    {
        return _col.count(filter);
    }

    protected long getCount(BasicDBObject filter, String tier, int level)
    {
        BasicDBObject obj = createQuery(tier, level);
        obj.putAll(filter.toMap());
        return _col.count(obj);

        //db.getCollection('tiers').find({'_id': { $regex: '^/2048202/.*' }, 'tiers.metadata':3}).count()
    }

    protected BasicDBObject createQuery(String tier, int level)
    {
        return new BasicDBObject("tiers." + tier, level);
    }

    protected BasicDBObject createDatasetCriteria(String ds)
    {
        return new BasicDBObject("_id", new BasicDBObject("$regex", "^/" + ds + "/.*"));
    }

    public static final void main(String[] args) throws IOException
    {
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            new TierReportGenerator(cli, "pf2", "tiers")
                .generateForAll(new CSVPrinter(System.out, CSVFormat.EXCEL));
        }
        finally { cli.close(); }
    }


}

