/**
 * 
 */
package eu.europeana.pf.alg;

import org.apache.jena.rdf.model.Model;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 17 Apr 2018
 */
public interface TierClassifierAlgorithm
{
    public String getLabel();

    public int getLevels();

    public int classify(Model model);
}
