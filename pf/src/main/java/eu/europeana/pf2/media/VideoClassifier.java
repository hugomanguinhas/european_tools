/**
 * 
 */
package eu.europeana.pf2.media;

import static eu.europeana.pf2.media.MediaUtils.*;
import static org.apache.commons.lang.StringUtils.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import eu.europeana.ld.edm.ORE;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class VideoClassifier extends AbsMediaClassifierAlgorithm
{
    public int classify(Model m)
    {
        if ( !hasThumbnail(m)      ) { return 0; }
        if ( !hasImageThumbnail(m) ) { return 0; }

        int tier = getMaxTierFromWebResources(m);
        if ( tier < 1 )
        {
            Resource aggr = getResource(m, ORE.Aggregation);
            if ( hasEmbeddableObject(aggr)   ) { return 2; }
            if ( hasWorkingLandingPage(aggr) ) { return 1; }
        }
        return tier;
    }

    @Override
    public int getTier(Resource wr)
    {
        if ( !isVideo(wr) ) { return 0; }

        int vRes = getHeight(wr);
        if ( vRes < 480 ) { return 1; } 

        if ( hasReusability(wr, Reusability.OPEN) ) { return 4; }

        if ( hasReusability(wr, Reusability.OPEN
                              , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }

    private boolean hasImageThumbnail(Model m)
    {
        Resource aggr = getResource(m, ORE.Aggregation);
        String url = getEdmObject(aggr);
        if ( isEmpty(url) ) { return false; }

        Resource wr = m.getResource(url);
        return ( getResolution(wr) >= ImageClassifier.RESOLUTION_S );
    }
}
