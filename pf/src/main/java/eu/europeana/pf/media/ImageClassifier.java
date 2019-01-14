/**
 * 
 */
package eu.europeana.pf.media;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import eu.europeana.ld.edm.EDM;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import eu.europeana.util.Reusability;
import static eu.europeana.pf.media.MediaUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class ImageClassifier extends AbsMediaClassifierAlgorithm
{
    public static int RESOLUTION_S  = 100000;
    public static int RESOLUTION_M  = 420000;
    public static int RESOLUTION_L  = 950000;

    public int classify(Model model)
    {
        if ( !hasThumbnail(model) ) { return 0; }

        return getMaxTierFromWebResources(model);
    }

    public int getTier(Resource wr)
    {
        int resolution = getResolution(wr);

        if ( resolution >= RESOLUTION_L
          && hasReusability(wr, Reusability.OPEN) ) { return 4; }

        if ( resolution >= RESOLUTION_L
          && hasReusability(wr, Reusability.OPEN
                              , Reusability.RESTRICTED) ) { return 3; }

        if ( resolution >= RESOLUTION_M ) { return 2; }

        if ( resolution >= RESOLUTION_S ) { return 1; }

        return 0;
    }
}
