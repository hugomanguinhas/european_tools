/**
 * 
 */
package eu.europeana.pf.model;

import org.bson.Document;

import eu.europeana.pf.media.MediaType;
import eu.europeana.util.EmbeddableMedia;
import eu.europeana.util.Reusability;
import static eu.europeana.util.MongoUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public class WebResource
{
    private Record   _record;
    private Document _wr;
    private Document _wrTM;

    public WebResource(Record record, Document wr, Document wrTM)
    {
        _record = record;
        _wr     = wr;
        _wrTM   = wrTM;
    }

    public String   getURL()          { return _wr.getString("about"); }
    public Document getWebResource()  { return _wr;   }
    public Document getTechMetadata() { return getTechMetadata(_record.getMediaType()); }

    public Document getTechMetadata(MediaType mt)
    {
        switch (mt)
        {
            case IMAGE: return getAsDocument(_wrTM, "imageMetaInfo");
            case SOUND: return getAsDocument(_wrTM, "audioMetaInfo");
            case VIDEO: return getAsDocument(_wrTM, "videoMetaInfo");
            case TEXT : return getAsDocument(_wrTM, "textMetaInfo");
        }
        return null;
    }

    public MediaType getMediaType()
    {
        if ( _wrTM.get("imageMetaInfo") != null ) { return MediaType.IMAGE; }
        if ( _wrTM.get("audioMetaInfo") != null ) { return MediaType.SOUND; }
        if ( _wrTM.get("videoMetaInfo") != null ) { return MediaType.VIDEO; }
        if ( _wrTM.get("textMetaInfo")  != null ) { return MediaType.TEXT; }
        return null;
    }

    public boolean isMediaType(MediaType mt)
    {
        switch (mt)
        {
            case IMAGE: return ( _wrTM.get("imageMetaInfo") != null );
            case SOUND: return ( _wrTM.get("audioMetaInfo") != null );
            case VIDEO: return ( _wrTM.get("videoMetaInfo") != null );
            case TEXT : return ( _wrTM.get("textMetaInfo")  != null );
        }
        return false;
    }

    public boolean hasMedia()
    {
        return ( getMediaType() != null );
    }

    public Reusability getReusability()
    {
        return Reusability.getReusability(getAsString(_wr.get("webResourceEdmRights")));
    }

    public boolean hasReusability(Reusability... ra)
    {
        Reusability r1 = getReusability();
        for ( Reusability r : ra )
        {
            if ( r.equals(r1) ) { return true; }
        }
        return _record.hasReusability(ra);
    }

    public String getMimetype()
    {
        return getTechMetadata().getString("mimeType");
    }

    public boolean hasMimetype(String... mimetypes)
    {
        String mime = getTechMetadata().getString("mimeType");
        for ( String mimetype : mimetypes )
        {
            if ( mimetype.equals(mime)) { return true; }
        }
        return false;
    }

    public int getResolution()
    {
        Document doc = getTechMetadata(MediaType.IMAGE);
        if ( doc == null ) { doc = getTechMetadata(MediaType.VIDEO); }
        if ( doc == null ) { return 0; }

        return doc.getInteger("width" , 0)
             * doc.getInteger("height", 0);
    }

    public int getSpatialResolution(WebResource wr)
    {
        Document doc = getTechMetadata(MediaType.TEXT);
        return ( doc == null ? 0 : doc.getInteger("spatialResolution", 0) );
    }

    public int getHeight()
    {
        Document doc = getTechMetadata(MediaType.IMAGE);
        if ( doc == null ) { doc = getTechMetadata(MediaType.VIDEO); }
        return ( doc == null ? 0 : doc.getInteger("height", 0) );
    }
}
