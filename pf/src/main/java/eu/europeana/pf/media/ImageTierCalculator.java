/**
 * 
 */
package eu.europeana.pf.media;

import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public class ImageTierCalculator implements MediaTierCalculator
{
    public static int RESOLUTION_S  = 100000;
    public static int RESOLUTION_M  = 420000;
    public static int RESOLUTION_L  = 950000;

    public int getTier(Record record)
    {
        if ( !record.hasThumbnail() ) { return 0; }

        int tier = 0;
        for ( WebResource wr : record.getWebResources() )
        {
            tier = Math.max(tier, getTier(wr));
        }
        return tier;
    }

    public int getTier(WebResource wr)
    {
        int resolution = wr.getResolution();

        if ( resolution >= RESOLUTION_L
          && wr.hasReusability(Reusability.OPEN) ) { return 4; }

        if ( resolution >= RESOLUTION_L
          && wr.hasReusability(Reusability.OPEN
                                  , Reusability.RESTRICTED) ) { return 3; }

        if ( resolution >= RESOLUTION_M ) { return 2; }

        if ( resolution >= RESOLUTION_S ) { return 1; }

        return 0;
    }
}
