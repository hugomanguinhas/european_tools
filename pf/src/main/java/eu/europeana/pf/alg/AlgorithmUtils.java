/**
 * 
 */
package eu.europeana.pf.alg;

import eu.europeana.pf.metadata.ContextualClassClassifierV1;
import eu.europeana.pf.metadata.ContextualClassClassifierV2;
import eu.europeana.pf.metadata.ContextualClassClassifierV3;
import eu.europeana.pf.metadata.EnablingElementsClassifier;
import eu.europeana.pf.metadata.LanguageClassifierV1;
import eu.europeana.pf.metadata.LanguageClassifierV2;
import eu.europeana.pf.metadata.LanguageClassifierV3;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 5 Nov 2018
 */
public class AlgorithmUtils
{
    public static TierClassifierAlgorithm[] getMetadataAlgorithmsV1()
    {
       TierClassifierAlgorithm lang = new LanguageClassifierV1();
       TierClassifierAlgorithm elem = new EnablingElementsClassifier();
       TierClassifierAlgorithm ctxc = new ContextualClassClassifierV1();
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
       TierClassifierAlgorithm ctxc = new ContextualClassClassifierV2();
       CombinedClassifier      comb = new CombinedClassifier("metadata");
       comb.add(lang);
       comb.add(elem);
       comb.add(ctxc);
       return new TierClassifierAlgorithm[] { lang, elem, ctxc, comb };
    }

    public static TierClassifierAlgorithm[] getMetadataAlgorithmsV3()
    {
       TierClassifierAlgorithm lang = new LanguageClassifierV3(false);
       TierClassifierAlgorithm elem = new EnablingElementsClassifier();
       TierClassifierAlgorithm ctxc = new ContextualClassClassifierV3();
       CombinedClassifier      comb = new CombinedClassifier("metadata");
       comb.add(lang);
       comb.add(elem);
       comb.add(ctxc);
       return new TierClassifierAlgorithm[] { lang, elem, ctxc, comb };
    }
}
