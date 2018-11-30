/**
 * 
 */
package eu.europeana.pf2.media;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import eu.europeana.ld.edm.EBUCORE;
import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.ORE;
import eu.europeana.pf2.media.MediaType;
import eu.europeana.util.EmbeddableMedia;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class MediaUtils
{
    public static Resource getResource(Model m, Resource type)
    {
        ResIterator iter = m.listResourcesWithProperty(RDF.type, type);
        return ( iter.hasNext() ? iter.next() : null );
    }

    public static Resource getProxy(Model m, String eProxy)
    {
        ResIterator iter = m.listResourcesWithProperty(RDF.type, ORE.Proxy);
        try
        {
            while ( iter.hasNext() )
            {
                Resource r = iter.next();
                if ( r.hasProperty(EDM.europeanaProxy, eProxy) ) { return r; }
            }
        }
        finally { iter.close(); }

        return null;
    }

    public static MediaType getMediaType(Resource proxy)
    {
        if ( proxy == null ) { return null; }

        Statement stmt = proxy.getProperty(EDM.type);
        return ( stmt == null ? null : MediaType.getMedia(stmt.getString()) );
    }

    public static String getIsShownBy(Resource aggr)
    {
        if ( aggr == null ) { return null; }

        Statement stmt = aggr.getProperty(EDM.isShownBy);
        return ( stmt == null ? null : stmt.getResource().getURI() );
    }

    public static String getEdmObject(Resource aggr)
    {
        Statement stmt = aggr.getProperty(EDM.object);
        return ( stmt == null ? null : stmt.getResource().getURI() );
    }

    public static String getLandingPage(Resource aggr)
    {
        Statement stmt = aggr.getProperty(EDM.isShownAt);
        return ( stmt == null ? null : stmt.getResource().getURI() );
    }

    public static boolean hasThumbnail(Model m)
    {
        return true;
    }

    public static boolean hasMedia(Resource wr)
    {
        for ( Property p : EDM.MEDIA_PROPERTIES )
        {
            if ( wr.hasProperty(p) ) { return true; }
        }
        return false;
    }

    public static boolean hasWorkingLandingPage(Resource aggr)
    {
        return !StringUtils.isEmpty(getLandingPage(aggr));
    }

    public static boolean hasEmbeddableObject(Resource aggr)
    {
        return EmbeddableMedia.isEmbeddable(getIsShownBy(aggr));
    }

    public static boolean isImage(Resource wr)
    {
        return isImageMimeType(getMimetype(wr));
    }

    public static boolean isSound(Resource wr)
    {
        return isSoundMimeType(getMimetype(wr));
    }

    public static boolean isVideo(Resource wr)
    {
        return isVideoMimeType(getMimetype(wr));
    }

    public static boolean isImageMimeType(String type)
    {
        return (StringUtils.startsWithIgnoreCase(type, "image"));
    }

    public static boolean isSoundMimeType(String type) {
        return (StringUtils.startsWithIgnoreCase(type, "sound")
             || StringUtils.startsWithIgnoreCase(type, "audio"));
    }

    public static boolean isVideoMimeType(String type) {
        return (StringUtils.startsWithIgnoreCase(type, "video"));
    }

    public static String getRights(Resource wr)
    {
        Statement stmt = wr.getProperty(EDM.rights);
        return (stmt == null ? null : stmt.getResource().getURI() );
    }

    public static Reusability getReusability(Resource wr)
    {
        return Reusability.getReusability(getRights(wr));
    }

    public static boolean hasReusability(Resource wr, Reusability... ra)
    {
        boolean ret = hasReusabilityInt(wr, ra);
        if ( ret ) { return true; }

        Resource aggr = getResource(wr.getModel(), ORE.Aggregation);
        return ( aggr == null ? false : hasReusabilityInt(aggr, ra));
    }

    private static boolean hasReusabilityInt(Resource rsrc, Reusability... ra)
    {
        Reusability r1 = getReusability(rsrc);
        for ( Reusability r : ra )
        {
            if ( r.equals(r1) ) { return true; }
        }
        return false;
    }

    public static String getMimetype(Resource wr)
    {
        Statement stmt = wr.getProperty(EBUCORE.hasMimeType);
        return (stmt == null ? null : stmt.getString());
    }

    public static boolean hasMimetype(Resource wr, String... mimetypes)
    {
        String mime = getMimetype(wr);
        if ( mime == null ) { return false; }

        for ( String mimetype : mimetypes )
        {
            if ( mimetype.equals(mime)) { return true; }
        }
        return false;
    }

    public static int getSpatialResolution(Resource wr)
    {
        Statement stmt = wr.getProperty(EDM.spatialResolution);
        return ( stmt == null ? 0 : stmt.getInt() );
    }

    public static int getResolution(Resource wr)
    {
        return (getWidth(wr) * getHeight(wr));
    }

    public static int getWidth(Resource wr)
    {
        Statement stmt = wr.getProperty(EBUCORE.width);
        return ( stmt == null ? 0 : stmt.getInt() );
    }

    public static int getHeight(Resource wr)
    {
        Statement stmt = wr.getProperty(EBUCORE.height);
        return ( stmt == null ? 0 : stmt.getInt() );
    }
}
