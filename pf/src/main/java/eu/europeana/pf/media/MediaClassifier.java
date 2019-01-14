/**
 * 
 */
package eu.europeana.pf.media;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import eu.europeana.pf.alg.TierClassifierAlgorithm;
import eu.europeana.pf.media.MediaType;
import static eu.europeana.pf.media.MediaUtils.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public class MediaClassifier implements TierClassifierAlgorithm
{
    public String getLabel() { return "media"; }

    public int getLevels() { return 4; }

    public int classify(Model model)
    {
        Resource r = getProxy(model, "true");
        MediaType mt = getMediaType(r);
        return (mt != null ? mt.getClassifier().classify(model) : 0);
    }
}
