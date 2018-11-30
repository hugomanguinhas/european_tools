/**
 * 
 */
package eu.europeana.pf2.report;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import eu.europeana.pf2.db.TierEntryConstants;
import static eu.europeana.pf2.alg.AlgorithmUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TierReportGenerator implements TierEntryConstants
{
    private MongoCollection<Document> _col;
    private NumberFormat FORMAT = new DecimalFormat("00.00%");

    public TierReportGenerator(MongoClient cli, String dbs, String col)
           throws IOException
    {
        _col = cli.getDatabase(dbs).getCollection(col);
    }


    /***************************************************************************
     * Public Methods
     * @throws IOException 
     **************************************************************************/

    public void generateForAll(CSVPrinter p) throws IOException
    {
        generateForSearch(p, new BasicDBObject());
    }

    public void generateForAllDatasets(CSVPrinter p, Collection<String> dss) throws IOException
    {
        for ( String ds : dss )
        {
            genSubCategory(p, createDatasetCriteria(ds), ds, 1);
        }
    }

    public void generateForDataset(CSVPrinter p, String ds) throws IOException
    {
        generateForSearch(p, createDatasetCriteria(ds));
    }

    public void generateForAllProviders(CSVPrinter p) throws IOException
    {
        MongoCursor<String> c = _col.distinct("provider", String.class).iterator();
        try
        {
            while (c.hasNext())
            {
                String provider = c.next();
                p.printRecord(provider);
                generateForProvider(p, provider);
                p.println();
            }
        }
        finally { c.close(); }
    }

    public void generateForProvider(CSVPrinter p, String provider) throws IOException
    {
        generateForSearch(p, new BasicDBObject("provider", provider));
    }

    public void generateForSearch(CSVPrinter p, BasicDBObject filter) throws IOException
    {
        printHead(p);
        long total = getCount(filter);        
        for ( int i = 0; i <= METADATA_TIER_MAX; i++)
        {
            p.print(METADATA_TIER.get(i));
            for ( String d : METADATA_DIMENSIONS )
            { 
                printResult(p, getCount(filter, d, i), total);
            }
            p.println();
        }
    }

    
    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private void genSubCategory(CSVPrinter p, BasicDBObject filter
                              , String category, int ident) throws IOException
    {
        long total = getCount(filter);
        for ( int i = 0; i <= METADATA_TIER_MAX; i++)
        {
            for ( int e = 0; e < ident; e++ )
            { 
                if ( i == 0 && e == (ident - 1)) { p.print(category); continue; }
                p.print("");
            }

            p.print(METADATA_TIER.get(i));
            for ( String d : METADATA_DIMENSIONS )
            { 
                printResult(p, getCount(filter, d, i), total);
            }
            p.println();
        }
    }


    private void printHead(CSVPrinter p) throws IOException
    {
        p.print("tier");
        for ( String d : METADATA_DIMENSIONS ) { p.print(d); }
        p.println();
    }

    private void printResult(CSVPrinter p, long count, long total) throws IOException
    {
        p.print(count + " (" + FORMAT.format((float)count / total) + ")");
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

