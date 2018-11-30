/**
 * 
 */
package eu.europeana.pf2.media;

import static eu.europeana.pf2.media.MediaUtils.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import eu.europeana.ld.edm.ORE;
import eu.europeana.pf.media.ImageTierCalculator;
import eu.europeana.pf.media.MediaType;
import eu.europeana.util.Reusability;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class TextClassifier extends AbsMediaClassifierAlgorithm
{
    public int classify(Model m)
    {
        Resource aggr = getResource(m, ORE.Aggregation);
        int tier = getMaxTierFromWebResources(m);
        if ( tier < 1 && hasWorkingLandingPage(aggr) ) { return 1; }
        return tier;
    }

    @Override
    public int getTier(Resource wr)
    {
        if ( isVideo(wr) ) { return 0; }

        if ( isImage(wr) )
        {
            int res = getResolution(wr);
            if ( res < ImageClassifier.RESOLUTION_S ) { return 0; }
            if ( res < ImageClassifier.RESOLUTION_M ) { return 1; }
            if ( res < ImageClassifier.RESOLUTION_L ) { return 2; }
        }
        else
        {
            int res = getSpatialResolution(wr);
            if ( res > 0 ) {
                if ( res < ImageClassifier.RESOLUTION_S ) { return 0; }
                if ( res < ImageClassifier.RESOLUTION_M ) { return 1; }
                if ( res < ImageClassifier.RESOLUTION_L ) { return 2; }
            }
            else
            {
                if ( !hasMimetype(wr, "application/pdf") ) { return 0; }
            }
        }

        if ( hasReusability(wr, Reusability.OPEN) ) { return 4; }

        if ( hasReusability(wr, Reusability.OPEN
                              , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
