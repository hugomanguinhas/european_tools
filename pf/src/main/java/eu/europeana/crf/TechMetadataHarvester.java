/**
 * 
 */
package eu.europeana.crf;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import eu.europeana.pf.TierClassifier;
import eu.europeana.pf.TierReport;
import eu.europeana.pf.media.MediaType;
import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Oct 2017
 */
public class TechMetadataHarvester extends TierClassifier
{
    private Map<MediaType,PrintStream> _ps     = null;
    private int                        _cursor = 0;

    public TechMetadataHarvester(MongoDatabase db
                               , Map<MediaType,PrintStream> ps
                               , int threads
                               , Collection<String> filter)
    {
        super(db, threads, filter);
        _ps = ps;
    }

    protected void classify(Record record, TierReport report)
    {
        MediaType mt = record.getMediaType();
        if ( mt == null ) { return; }

        for ( WebResource wr : record.getWebResources() )
        {
            Document doc = wr.getTechMetadata(mt);
            if ( doc == null ) { continue; }

            newFile(wr.getURL(), mt, doc);
        }
    }

    private void newFile(String url, MediaType mt, Document doc)
    {
        PrintStream ps = _ps.get(mt);
        if ( ps == null ) { return; }

        doc.put("id", url);
        ps.println(doc.toJson());
    }
}
