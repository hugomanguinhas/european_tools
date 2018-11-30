/**
 * 
 */
package eu.europeana.pf;

import java.io.File;

import eu.europeana.ld.jena.JenaUtils;
import eu.europeana.pf2.alg.TierClassifierAlgorithm;
import eu.europeana.pf2.metadata.ContextualClassClassifier;
import eu.europeana.pf2.metadata.EnablingElementsClassifier;
import eu.europeana.pf2.metadata.LanguageClassifier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 26 Oct 2018
 */
public class TestMetadataClassifiers
{
    private static void test(TierClassifierAlgorithm alg, File file)
    {
        int tier = alg.classify(JenaUtils.load(file));
        System.out.println(file.getName() + ": " + tier);
    }

    private static void testLang(File file)
    {
        test(new LanguageClassifier(), file);
    }

    private static void testEnabling(File file)
    {
        test(new EnablingElementsClassifier(), file);
    }

    private static void testEntities(File file)
    {
        test(new ContextualClassClassifier(), file);
    }

    public static final void main(String[] args)
    {
        File dir = new File("D:\\work\\incoming\\tiers\\metadata\\");
        testLang(new File(dir, "lang.T3.xml"));
        testLang(new File(dir, "lang.T2.xml"));
        testLang(new File(dir, "lang.T1.xml"));
        testLang(new File(dir, "lang.T0.xml"));

        testEnabling(new File(dir, "enabling.T0.xml"));
        testEnabling(new File(dir, "enabling.T1.xml"));
        testEnabling(new File(dir, "enabling.T2.xml"));
        testEnabling(new File(dir, "enabling.T3.xml"));

        testEntities(new File(dir, "entities.T1.xml"));
        testEntities(new File(dir, "entities.T2.xml"));

        //testLang(new File(dir, "test.xml"));
        //testEnabling(new File(dir, "lang.T3.xml"));
        //testEntities(new File(dir, "lang.T3.xml"));
    }
}
