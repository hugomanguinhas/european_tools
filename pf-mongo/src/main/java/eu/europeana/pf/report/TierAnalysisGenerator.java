/**
 * 
 */
package eu.europeana.pf.report;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import eu.europeana.pf.metadata.MetadataDimension;
import eu.europeana.pf.metadata.MetadataTier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 11 Dec 2018
 */
public abstract class TierAnalysisGenerator
{
    protected static int BATCH_SIZE = 1000;
    protected NumberFormat FORMAT = new DecimalFormat("00.00%");


    /***************************************************************************
     * Public Methods - Tree clusterings
     **************************************************************************/

    
    /***************************************************************************
     * Private Methods - Tree clusterings
     **************************************************************************/

    public void genByCategories(CSVPrinter p, String... props) throws IOException
    {
        printHead(p, props);
        Document prev = null;
        MongoCursor<Document> c = 
                getCollection().aggregate(createAggregatedQuery(props))
                               .batchSize(BATCH_SIZE).iterator();
        try
        {
            while (c.hasNext())
            {
                Document doc = (Document)c.next().get("_id");
                printCell(p, prev, doc, props);
                printTreeRow(p, new BasicDBObject(doc));
                p.println();
                prev = doc;
            }
        }
        finally { c.close(); }        
    }

    public void genTree(CSVPrinter p, String... props) throws IOException
    {
        printHead(p, props);
        Document prev = null;
        MongoCursor<Document> c = 
                getCollection().aggregate(createTreeQuery(props))
                               .batchSize(BATCH_SIZE).iterator();
        try
        {
            while (c.hasNext())
            {
                Document result = c.next();
                Document aggr   = (Document)result.get("_id");
                printCell(p, prev, aggr, props);
                printTreeRow2(p, result);
                p.println();
                prev = aggr;
            }
        }
        finally { c.close(); }        
    }

    protected void printTreeRow2(CSVPrinter p, Document doc) 
            throws IOException
    {
        long total = doc.getInteger("total");
        for ( MetadataDimension d : MetadataDimension.values() )
        {
            for ( MetadataTier tier : MetadataTier.values() )
            {
                printResult(p, doc.getInteger(getSumLabel(d, tier)), total);
            }
        }
    }

    protected void printResult(CSVPrinter p, long count, long total)
            throws IOException
    {
        p.print(count
              + " (" + FORMAT.format(Math.round((float)count / total)) + ")");
    }

    protected abstract void printTreeRow(CSVPrinter p, BasicDBObject filter)
              throws IOException;

    protected void printCell(CSVPrinter p, Document prev, Document doc
                           , String... props) throws IOException
    {
        int i = 0;
        for ( ; i < props.length; i++ )
        {
            if ( prev == null ) { break; }

            String prop   = props[i];
            if ( !getValue(prev, prop).equals(getValue(doc, prop)) ) { break; }

//            if ( prev == null || !prev.get(prop).equals(doc.get(prop)) ) { break; }

            p.print("");
        }

        for ( ; i < props.length; i++ ) { p.print(getValue(doc, props[i])); }
    }

    private String getValue(Document d, String prop)
    {
        Object value = d.get(prop);
        return ( value == null ? "?" : value.toString() );
    }

    protected List createAggregatedQuery(String... props)
    {
//      {"$group": { "_id": { provider: "$provider", dataProvider: "$dataProvider", ds: "$ds" } } },
//      {$sort:{"_id.provider":1, "_id.dataProvider":1, "_id.ds":1}}
      List l = new ArrayList();

      BasicDBObject groupBy = new BasicDBObject();
      for ( String prop : props ) { groupBy.append(prop, "$" + prop); }
      l.add(new BasicDBObject("$group", new BasicDBObject("_id", groupBy)));

      BasicDBObject sort    = new BasicDBObject();
      for ( String prop : props ) { sort.append("_id." + prop, 1); }
      l.add(new BasicDBObject("$sort", sort));

      return l;
    }

    /*
    db.getCollection('tiers').aggregate([
    {$group: { 
        "_id": { provider: "$provider", dataProvider: "$dataProvider", ds: "$ds" },
        "total": { $sum: 1 } ,
        "enabling_T0": { $sum: { $cond: { if: { $eq: [ "$tiers.enabling", 0 ] }, then: 1, else: 0 } } } ,
        "enabling_T1": { $sum: { $cond: { if: { $eq: [ "$tiers.enabling", 1 ] }, then: 1, else: 0 } } } ,
        "enabling_T2": { $sum: { $cond: { if: { $eq: [ "$tiers.enabling", 2 ] }, then: 1, else: 0 } } } ,
        "enabling_T3": { $sum: { $cond: { if: { $eq: [ "$tiers.enabling", 3 ] }, then: 1, else: 0 } } } 
    } },
    {$sort:{"_id.provider":1, "_id.dataProvider":1, "_id.ds":1}}]
    )
    */
    
    protected List createTreeQuery(String... props)
    {
      List l = new ArrayList();

      BasicDBObject group   = new BasicDBObject();

      BasicDBObject groupBy = new BasicDBObject();
      for ( String prop : props ) { groupBy.append(prop, "$" + prop); }
      group.put("_id", groupBy);

      group.put("total", new BasicDBObject("$sum", 1));

      for ( MetadataDimension d : MetadataDimension.values() )
      {
          for ( MetadataTier tier : MetadataTier.values() )
          {
              addSum(group, d, tier);
          }
      }

      l.add(new BasicDBObject("$group", group));

      BasicDBObject sort    = new BasicDBObject();
      for ( String prop : props ) { sort.append("_id." + prop, 1); }
      l.add(new BasicDBObject("$sort", sort));

      return l;
    }

    private String getSumLabel(MetadataDimension d, MetadataTier tier)
    {
        return (d.getID() + "_" + tier.getLabel());
    }

    //"enabling_T0": { $sum: { $cond: { if: { $eq: [ "$tiers.enabling", 0 ] }, then: 1, else: 0 } } }
    private void addSum(BasicDBObject group
                      , MetadataDimension d, MetadataTier tier)
    {
        String label = getSumLabel(d, tier);
        BasicDBObject cond = new BasicDBObject("if", new BasicDBObject("$eq", 
            Arrays.asList("$tiers." + d.getID(), tier.ordinal())));
        cond.put("then", 1);
        cond.put("else", 0);
        group.put(label, new BasicDBObject("$sum"
                                         , new BasicDBObject("$cond",cond)));
    }
    

    /***************************************************************************
     * Protected Methods
     **************************************************************************/

    protected abstract MongoCollection<Document> getCollection();


    /***************************************************************************
     * Private Methods - Printing
     **************************************************************************/

    protected void printHead(CSVPrinter p, String... categories)
              throws IOException
    {
        for ( String c : categories ) { p.print(c); }

        int l = MetadataTier.values().length - 1;
        for ( MetadataDimension d : MetadataDimension.values() )
        { 
            p.print(d.getLabel());
            for ( int i = 0; i < l; i++) { p.print(""); }
        }
        p.println();

        for ( String c : categories ) { p.print(""); }

        for ( MetadataDimension d : MetadataDimension.values() )
        {
            for ( MetadataTier tier : MetadataTier.values() )
            {
                p.print(tier.getLabel());
            }
        }
        p.println();
    }
}
