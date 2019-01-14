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
public class M3DClassifier extends AbsMediaClassifierAlgorithm
{
    public int classify(Model model)
    {
        return getMaxTierFromWebResources(model);
    }

    @Override
    public int getTier(Resource wr)
    {
        if ( hasReusability(wr, Reusability.OPEN) ) { return 4; }

        if ( hasReusability(wr, Reusability.OPEN
                              , Reusability.RESTRICTED) ) { return 3; }

        return 2;
    }
}
