/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;

import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.RDAGR2;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import static eu.europeana.pf.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 */
public class LanguageClassifierV1 implements TierClassifierAlgorithm
{
    public static List<Property> RELEVANT_LANGUAGE_PROPERTIES
        = Arrays.asList(
            DC.contributor,
            DC.coverage,
            DC.creator,
            DC.date,
            DC.description,
            DC.format,
            DC.publisher,
            DC.relation,
            DC.rights,
            DC.source,
            DC.subject,
            DC.title,
            DC.type,
            DCTerms.alternative,
            DCTerms.conformsTo,
            DCTerms.created,
            DCTerms.extent,
            DCTerms.hasFormat,
            DCTerms.hasPart,
            DCTerms.hasVersion,
            DCTerms.isFormatOf,
            DCTerms.isPartOf,
            DCTerms.isReferencedBy,
            DCTerms.isReplacedBy,
            DCTerms.isRequiredBy,
            DCTerms.issued,
            DCTerms.isVersionOf,
            DCTerms.medium,
            DCTerms.provenance,
            DCTerms.references,
            DCTerms.replaces,
            DCTerms.requires,
            DCTerms.spatial,
            DCTerms.tableOfContents,
            DCTerms.temporal,
            EDM.hasType,
            EDM.isRelatedTo,
            EDM.dataProvider,
            EDM.provider,
            DC.rights,
            EDM.intermediateProvider,
    
            //Entities
            SKOS.prefLabel,
            SKOS.altLabel,
            SKOS.note,
            EDM.begin,
            EDM.end,
            FOAF.name,
            RDAGR2.biographicalInformation,
            RDAGR2.dateOfBirth,
            RDAGR2.dateOfDeath,
            RDAGR2.dateOfEstablishment,
            RDAGR2.dateOfTermination,
            RDAGR2.gender,
            RDAGR2.placeOfBirth,
            RDAGR2.placeOfDeath,
            RDAGR2.professionOrOccupation
    );

    public static boolean isRelevantProperty(Statement s)
    {
        return ( RELEVANT_LANGUAGE_PROPERTIES.contains(s.getPredicate()) 
              && s.getObject().isLiteral() );
    }

    private EDMExternalCrawler _crawler = new EDMExternalCrawler();

    public String getLabel() { return MetadataDimension.LANGUAGE.getID(); }

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
