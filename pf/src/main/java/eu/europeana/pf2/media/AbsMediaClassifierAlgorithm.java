/**
 * 
 */
package eu.europeana.pf2.media;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import eu.europeana.ld.edm.EDM;
import eu.europeana.pf2.alg.TierClassifierAlgorithm;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 2 Jun 2018
 */
public abstract class AbsMediaClassifierAlgorithm
       implements TierClassifierAlgorithm
{
    public String getLabel()  { return "media"; }

    public int    getLevels() { return 4;       }

    protected int getMaxTierFromWebResources(Model m)
    {
        int tier = 0;
        ResIterator iter = m.listResourcesWithProperty(RDF.type
                                                     , EDM.WebResource);
        try {
            while ( iter.hasNext() )
            {
                tier = Math.max(tier, getTier(iter.next()));
            }
        }
        finally { iter.close(); }

        return tier;
    }

    public abstract int getTier(Resource wr);
}
