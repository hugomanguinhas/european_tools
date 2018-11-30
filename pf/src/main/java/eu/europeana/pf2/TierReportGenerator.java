/**
 * 
 */
package eu.europeana.pf2;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf2.media.MediaType;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 24 Aug 2018
 */
public class TierReportGenerator
{

    public static final void main(String[] args) throws IOException
    {
        MongoClient     cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            MongoDatabase   db  = cli.getDatabase("pf");
            MongoCollection col = db.getCollection("tiers");

            CSVPrinter p = new CSVPrinter(System.out, CSVFormat.EXCEL);    
            printMedia(col, p);
            p.println();
            printCountry(col, p);
        }
        finally { cli.close(); }
    }

    private static void printMedia(MongoCollection col, CSVPrinter p)
            throws IOException
    {
        printHeader("MEDIA", p);

        BasicDBObject query = new BasicDBObject(2);
        for ( MediaType type : MediaType.values() )
        {
            String media = type.getLabel();
            p.print(media);
            for ( int i = 0; i < 5; i++)
            {
                query.put("type", media);
                query.put("tier", i);
                long count = col.count(query);
                p.print(count);
            }
            p.println();
        }
    }

    private static void printCountry(MongoCollection col, CSVPrinter p)
            throws IOException
    {
        printHeader("COUNTRY", p);

        BasicDBObject query = new BasicDBObject(2);
        for ( String country : getCountries(col) )
        {
            p.print(country);
            for ( int i = 0; i < 5; i++)
            {
                query.put("country", country);
                query.put("tier", i);
                long count = col.count(query);
                p.print(count);
            }
            p.println();
        }
    }

    private static Collection<String> getCountries(MongoCollection col)
    {
        Collection<String> ret = new TreeSet();
        DistinctIterable<String> iter = col.distinct("country", String.class);
        for ( String s : iter ) { ret.add(s); }
        return ret;
    }

    private static void printHeader(String criteria, CSVPrinter p) throws IOException
    {
        p.print(criteria);
        p.print("T0");
        p.print("T1");
        p.print("T2");
        p.print("T3");
        p.print("T4");
        p.println();
    }
}
