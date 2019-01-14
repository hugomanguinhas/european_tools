/**
 * 
 */
package eu.europeana.pf.report;

import java.io.File;
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












import eu.europeana.pf.db.TierEntryConstants;
import eu.europeana.pf.metadata.MetadataDimension;
import eu.europeana.pf.metadata.MetadataTier;
import static eu.europeana.ld.mongo.MongoUtils.DEF_BATCHSIZE;
import static eu.europeana.pf.alg.AlgorithmUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TierLogGenerator extends TierAnalysisGenerator 
                              implements TierEntryConstants
{
    private MongoCollection<Document> _col;
    private int                       _sampleSize;
    private CSVFormat                 _format;

    public TierLogGenerator(MongoClient cli, String dbs, String col
                          , int sampleSize)
           throws IOException
    {
        _col = cli.getDatabase(dbs).getCollection(col);
        _sampleSize = sampleSize;
        _format = CSVFormat.EXCEL;
        _format.withCommentMarker('#');
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

    public void genForAllProviders(File template) throws IOException
    {
        genByCategory(template, "provider");
    }

    public void genForAllDatasets(File template) throws IOException
    {
        genByCategory(template, "ds");
    }

    /***************************************************************************
     * Protected Methods
     **************************************************************************/

    protected void printTreeRow(CSVPrinter p, BasicDBObject filter)
              throws IOException
    {
        for ( MetadataDimension d : MetadataDimension.values() )
        {
            int i = 0;
            for ( MetadataTier tier : MetadataTier.values() )
            {
                printTreeSample(p, filter, d, i++);
            }
        }
    }

    private void printTreeSample(CSVPrinter p, BasicDBObject filter
                           , MetadataDimension d, int tier) throws IOException
    {

        BasicDBObject obj = createQuery(d.getID(), tier);
        obj.putAll(filter.toMap());

        StringBuilder sb = new StringBuilder();

        int bs = Math.min(BATCH_SIZE, _sampleSize);
        MongoCursor<Document> c = _col.find(obj).batchSize(bs).iterator();
        try
        {
            int i = _sampleSize;
            while (c.hasNext())
            {
                Document doc = c.next();
                sb.append("http://data.europeana.eu/item" + doc.get("_id"));
                sb.append("\n");

                if ( --i <= 0 ) { break; }
            }
        }
        finally { c.close(); }

        p.print(sb.toString());
    }

    @Override
    protected MongoCollection<Document> getCollection() { return _col; }

    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private void genByCategory(File template, String prop) throws IOException
    {
        MongoCursor<String> c = _col.distinct(prop, String.class)
                                    .batchSize(BATCH_SIZE).iterator();
        try
        {
            CSVPrinter p;
            BasicDBObject filter = new BasicDBObject();
            while (c.hasNext())
            {
                String value = c.next();
                filter.put(prop, value);

                File file = getFile(template, value);
                p = new CSVPrinter(new PrintStream(file), _format);
                try     { genCategory(p, filter); }
                finally { p.close();              }
            }
        }
        finally { c.close(); }
    }

    private File getFile(File tpt, String suffix)
    {
        if ( tpt.isDirectory()     ) { return new File(tpt, suffix + ".csv"); }

        File   dir  = tpt.getParentFile();
        String name = tpt.getName();
        if ( name.endsWith(".csv") ) { name = name.replace(".csv", "");       }

        return new File(dir, name + "." + suffix + ".csv");
    }

    private void genCategory(CSVPrinter p, BasicDBObject filter)
            throws IOException
    {
        for ( MetadataDimension d : MetadataDimension.values() )
        {
            int i = 0;
            for ( MetadataTier tier : MetadataTier.values() )
            {
                printSeparator(p, d, tier);
                printSample(p, filter, d, i++);
            }
        }
        p.flush();
    }

    private void printSeparator(CSVPrinter csv
                              , MetadataDimension d, MetadataTier tier)
            throws IOException
    {
        csv.printRecord(d.getLabel() + "," + tier.getLabel());
    }

    private void printSample(CSVPrinter p, BasicDBObject filter, MetadataDimension d, int tier) throws IOException
    {
        int batchSize = Math.min(BATCH_SIZE, _sampleSize);

        BasicDBObject obj = createQuery(d.getID(), tier);
        obj.putAll(filter.toMap());
        
        MongoCursor<Document> c = _col.find(obj).batchSize(batchSize).iterator();
        try
        {
            int i = _sampleSize;
            while (c.hasNext())
            {
                Document doc = c.next();
                p.printRecord("http://data.europeana.eu/item" + doc.get("_id"));

                if ( --i <= 0 ) { break; }
            }
            p.println();
        }
        finally { c.close(); }
    }


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
        File        dir = new File("D:\\work\\incoming\\tiers\\metadata\\samples\\");
        PrintStream ps  = new PrintStream(new File(dir, "tree.log.csv"));
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            new TierLogGenerator(cli, "pf2", "tiers", 10)
                .genByCategories(new CSVPrinter(ps, CSVFormat.EXCEL)
                               , "provider", "dataProvider", "ds");
        }
        finally { cli.close(); ps.close(); }
    }
}
