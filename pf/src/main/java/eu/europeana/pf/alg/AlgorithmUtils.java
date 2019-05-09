/**
 * 
 */
package eu.europeana.pf.alg;

import eu.europeana.pf.metadata.ContextualClassClassifier;
import eu.europeana.pf.metadata.ContextualClassClassifier2;
import eu.europeana.pf.metadata.EnablingElementsClassifier;
import eu.europeana.pf.metadata.LanguageClassifierV1;
import eu.europeana.pf.metadata.LanguageClassifierV2;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 5 Nov 2018
 */
public class AlgorithmUtils
{
    public static TierClassifierAlgorithm[] getMetadataAlgorithms()
    {
       TierClassifierAlgorithm lang = new LanguageClassifierV1();
       TierClassifierAlgorithm elem = new EnablingElementsClassifier();
       TierClassifierAlgorithm ctxc = new ContextualClassClassifier();
       CombinedClassifier      comb = new CombinedClassifier("metadata");
       comb.add(lang);
       comb.add(elem);
       comb.add(ctxc);
       return new TierClassifierAlgorithm[] { lang, elem, ctxc, comb };
    }

    public static TierClassifierAlgorithm[] getMetadataAlgorithmsV2()
    {
       TierClassifierAlgorithm lang = new LanguageClassifierV2();
       TierClassifierAlgorithm elem = new EnablingElementsClassifier();
       TierClassifierAlgorithm ctxc = new ContextualClassClassifier2();
       CombinedClassifier      comb = new CombinedClassifier("metadata");
       comb.add(lang);
       comb.add(elem);
       comb.add(ctxc);
       return new TierClassifierAlgorithm[] { lang, elem, ctxc, comb };
    }
}
