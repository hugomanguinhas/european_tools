/**
 * 
 */
package eu.europeana.pf.media;

import org.bson.Document;

import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;
import static org.apache.commons.lang.StringUtils.*;
import eu.europeana.pf.media.ImageTierCalculator;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public class VideoTierCalculator implements MediaTierCalculator
{
    public int getTier(Record record)
    {
        if ( !record.hasThumbnail()     ) { return 0; }
        if ( !hasImageThumbnail(record) ) { return 0; }

        int tier = 0;
        for ( WebResource wr : record.getWebResources() )
        {
            tier = Math.max(tier, getTier(wr));
        }
        if ( tier < 1 )
        {
            if ( record.hasEmbeddableObject()   ) { return 2; }
            if ( record.hasWorkingLandingPage() ) { return 1; }
        }
        return tier;
    }

    public int getTier(WebResource wr)
    {
        boolean hasMedia = wr.isMediaType(MediaType.VIDEO);

        if ( !hasMedia ) { return 0; }

        int vRes = wr.getHeight();
        if ( vRes < 480 ) { return 1; } 

        if ( wr.hasReusability(Reusability.OPEN) ) { return 4; }

        if ( wr.hasReusability(Reusability.OPEN
                             , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }

    private boolean hasImageThumbnail(Record record)
    {
        String url = record.getEdmObject();
        if ( isEmpty(url) ) { return false; }

        WebResource wr = record.getWebResource(url);
        if ( wr == null   ) { return false; }

        return ( wr.getResolution() >= ImageTierCalculator.RESOLUTION_S );
    }
}
