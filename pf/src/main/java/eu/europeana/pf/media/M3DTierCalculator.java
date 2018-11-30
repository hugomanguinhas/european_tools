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
public class M3DTierCalculator implements MediaTierCalculator
{
    public int getTier(Record record)
    {
        int tier = 0;
        for ( WebResource wr : record.getWebResources() )
        {
            tier = Math.max(tier, getTier(wr));
        }
        return tier;
    }

    public int getTier(WebResource wr)
    {
        if ( wr.hasReusability(Reusability.OPEN) ) { return 4; }

        if ( wr.hasReusability(Reusability.OPEN
                             , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
