/**
 * 
 */
package eu.europeana.pf2.metadata;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import eu.europeana.pf2.alg.TierClassifierAlgorithm;

import static eu.europeana.pf2.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 */
public class LanguageClassifier implements TierClassifierAlgorithm
{
    private EDMExternalCrawler _crawler = new EDMExternalCrawler();

    public String getLabel() { return "lang"; }

    public int getLevels() { return 3; }

    public int classify(Model model)
    {
        Measure measure = new Measure();
        for ( Resource r : _crawler.crawl(model) )
        {
            measureResource(r, measure);
        }

        float percent = ( (float)measure.match / measure.count );
        if ( percent < 0.25 ) { return 0; }
        if ( percent < 0.5  ) { return 1; }
        if ( percent < 0.75 ) { return 2; }
        return 3;
    }

    private void measureResource(Resource r, Measure m)
    {
        StmtIterator iter = r.listProperties();
        try
        {
            while ( iter.hasNext() )
            {
                Statement stmt = iter.next();
                if ( !isRelevantProperty(stmt) ) { continue; }

                m.count++;
                if ( !StringUtils.isEmpty(stmt.getLanguage()) ) { m.match++; }
            }
        }
        finally { iter.close(); }
    }

    private static class Measure
    {
        protected int match;
        protected int count;
    }
}
