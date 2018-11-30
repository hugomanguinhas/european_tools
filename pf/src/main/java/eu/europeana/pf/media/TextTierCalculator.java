/**
 * 
 */
package eu.europeana.pf.media;

import eu.europeana.pf.media.ImageTierCalculator;
import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public class TextTierCalculator implements MediaTierCalculator
{
    public int getTier(Record record)
    {
        int tier = 0;
        for ( WebResource wr : record.getWebResources() )
        {
            tier = Math.max(tier, getTier(wr));
        }
        if ( tier < 1 && record.hasWorkingLandingPage() ) { return 1; }
        return tier;
    }

    public int getTier(WebResource wr)
    {
        if ( wr.isMediaType(MediaType.TEXT) )
        {
            int res = wr.getSpatialResolution(wr);
            if ( res > 0 ) {
                if ( res < ImageTierCalculator.RESOLUTION_S ) { return 0; }
                if ( res < ImageTierCalculator.RESOLUTION_M ) { return 1; }
                if ( res < ImageTierCalculator.RESOLUTION_L ) { return 2; }
            }
            if ( !wr.hasMimetype("application/pdf") ) { return 1; }
        }
        if ( wr.isMediaType(MediaType.IMAGE) )
        {
            int res = wr.getResolution();
            if ( res < ImageTierCalculator.RESOLUTION_S ) { return 0; }
            if ( res < ImageTierCalculator.RESOLUTION_M ) { return 1; }
            if ( res < ImageTierCalculator.RESOLUTION_L ) { return 2; }
        }

        if ( wr.hasReusability(Reusability.OPEN) ) { return 4; }

        if ( wr.hasReusability(Reusability.OPEN
                             , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
