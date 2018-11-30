/**
 * 
 */
package eu.europeana.pf.media;

import org.apache.commons.lang.StringUtils;

import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public class SoundTierCalculator implements MediaTierCalculator
{
    public int getTier(Record record)
    {
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
        boolean hasMedia = wr.isMediaType(MediaType.SOUND);

        if ( !hasMedia ) { return 0; }

        if ( wr.hasReusability(Reusability.OPEN) ) { return 4; }

        if ( wr.hasReusability(Reusability.OPEN
                             , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
