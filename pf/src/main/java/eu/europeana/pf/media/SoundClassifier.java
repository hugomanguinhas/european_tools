/**
 * 
 */
package eu.europeana.pf.media;

import static eu.europeana.pf.media.MediaUtils.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import eu.europeana.ld.edm.ORE;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class SoundClassifier extends AbsMediaClassifierAlgorithm
{
    public int classify(Model model)
    {
        int tier = getMaxTierFromWebResources(model);
        if ( tier < 1 )
        {
            Resource aggr = getResource(model, ORE.Aggregation);
            if ( hasEmbeddableObject(aggr)   ) { return 2; }
            if ( hasWorkingLandingPage(aggr) ) { return 1; }
        }
        return tier;
    }

    @Override
    public int getTier(Resource wr)
    {
        if ( !isSound(wr) ) { return 0; }

        //boolean hasMedia = hasMedia(wr);
        //if ( !hasMedia ) { return 0; }

        if ( hasReusability(wr, Reusability.OPEN) ) { return 4; }

        if ( hasReusability(wr, Reusability.OPEN
                              , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
