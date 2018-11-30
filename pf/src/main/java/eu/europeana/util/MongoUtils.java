/**
 * 
 */
package eu.europeana.util;

import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf.TierClassifier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Sep 2017
 */
public class MongoUtils
{
    private static Logger _log = Logger.getLogger(TierClassifier.class);

    public static Document getAsDocument(Document doc, String field)
    {
        Object obj = doc.get(field);
        if ( obj instanceof Document ) { return (Document)obj; }
        return null;
    }

    public static Document fetch(MongoDatabase db, Object obj)
    {
        if ( !(obj instanceof DBRef) ) { return null; }
        DBRef ref = (DBRef)obj;
        return fetch(db, ref.getCollectionName(), ref.getId());
    }

    public static Document fetch(MongoDatabase db, String cn, Object id)
    {
        MongoCollection<Document> col = db.getCollection(cn);
        if ( col == null ) { logUnknownCol(cn);  return null; }

        Document doc = col.find(new BasicDBObject("_id", id)).first();
        if ( doc == null ) { logUnknownEntry(cn, id);  return null; }

        return doc;
    }

    public static String getAsString(Object obj)
    {
        if ( obj instanceof String   ) { return (String)obj; }
        if ( obj instanceof Document )
        {
            return getAsString(((Document)obj).values().iterator().next());
        }
        if ( obj instanceof List ) { return getAsString(((List)obj).get(0)); }

        return null;
    }

    /***************************************************************************
     * Private Methods - Logging
     **************************************************************************/

    private static void logUnknownCol(String cn)
    {
        _log.error("Unknown MongoDB Collection with name:" + cn);
    }

    private static void logUnknownEntry(String cn, Object id)
    {
        _log.warn("Unknown entry within collection <" + cn + ">"
                + " with id <" + id + ">");
    }
}