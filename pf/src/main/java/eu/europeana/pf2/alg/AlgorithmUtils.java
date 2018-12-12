/**
 * 
 */
package eu.europeana.pf2.alg;

import java.util.Arrays;
import java.util.List;

import eu.europeana.pf2.metadata.ContextualClassClassifier;
import eu.europeana.pf2.metadata.ContextualClassClassifier2;
import eu.europeana.pf2.metadata.EnablingElementsClassifier;
import eu.europeana.pf2.metadata.LanguageClassifier;
import eu.europeana.pf2.metadata.LanguageClassifier2;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 5 Nov 2018
 */
public class AlgorithmUtils
{
    public static int METADATA_TIER_MAX = 3;
    public static List<String> METADATA_TIER
        = Arrays.asList("0", "A", "B", "C");

    public static TierClassifierAlgorithm[] getMetadataAlgorithms()
    {
       TierClassifierAlgorithm lang = new LanguageClassifier();
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
       TierClassifierAlgorithm lang = new LanguageClassifier2();
       TierClassifierAlgorithm elem = new EnablingElementsClassifier();
       TierClassifierAlgorithm ctxc = new ContextualClassClassifier2();
       CombinedClassifier      comb = new CombinedClassifier("metadata");
       comb.add(lang);
       comb.add(elem);
       comb.add(ctxc);
       return new TierClassifierAlgorithm[] { lang, elem, ctxc, comb };
    }
}
