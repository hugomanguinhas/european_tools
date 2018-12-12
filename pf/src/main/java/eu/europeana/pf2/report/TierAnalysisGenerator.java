/**
 * 
 */
package eu.europeana.pf2.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import eu.europeana.pf2.metadata.MetadataDimension;
import eu.europeana.pf2.metadata.MetadataTier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 11 Dec 2018
 */
public abstract class TierAnalysisGenerator
{
    protected static int BATCH_SIZE = 1000;


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

    protected abstract void printTreeRow(CSVPrinter p, BasicDBObject filter)
              throws IOException;

    protected void printCell(CSVPrinter p, Document prev, Document doc
                           , String... props) throws IOException
    {
        int i = 0;
        for ( ; i < props.length; i++ )
        {
            String prop = props[i];
            if ( prev == null || !prev.get(prop).equals(doc.get(prop)) ) { break; }

            p.print("");
        }

        for ( ; i < props.length; i++ ) { p.print(doc.get(props[i])); }
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
