/**
 * 
 */
package eu.europeana.pf;

import java.util.Map;

import org.apache.jena.rdf.model.Model;

import eu.europeana.pf2.alg.TierClassifierAlgorithm;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 */
@SuppressWarnings("serial")
public class TierClassifier2
{
    private TierConfig _cfg;

    public TierClassifier2(TierConfig cfg) { _cfg = cfg; }

    public void classify(Model model, TierReport2 report)
    {
        for ( Map.Entry<String, TierClassifierAlgorithm> e : _cfg.entrySet() )
        {
            report.newTierValue(e.getKey(), e.getValue().classify(model));
        }
    }
}