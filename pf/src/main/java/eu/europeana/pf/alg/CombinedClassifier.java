/**
 * 
 */
package eu.europeana.pf.alg;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 */
@SuppressWarnings("serial")
public class CombinedClassifier extends ArrayList<TierClassifierAlgorithm>
                                implements TierClassifierAlgorithm
{
    private String _label;

    public CombinedClassifier(String label) { _label = label; }

    public String getLabel() { return _label; } 
    
    public int getLevels()
    {
        int ret = 0;
        for ( TierClassifierAlgorithm alg : this )
        {
            ret = Math.max(ret, alg.getLevels());
        }
        return ret;
    }

    public int classify(Model model)
    {
        if ( this.isEmpty() ) { return 0; }

        int ret = Integer.MAX_VALUE;
        for ( TierClassifierAlgorithm alg : this )
        {
            ret = Math.min(ret, alg.classify(model));
        }
        return ret;
    }
}