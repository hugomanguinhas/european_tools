/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.RDAGR2;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import static eu.europeana.pf.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 * 
 * Second version of the language classifier considering only the statements 
 * that are part of the record, aggregation and web resource. 
 * 
 * If statement refers to a reference, then it will count if the 
 * contextual entity is language qualified. A contextual entity is language 
 * qualified only if there is at least one pref label which is language 
 * qualified. Time Spans are simply ignored.
 */
public class LanguageClassifierV2 implements TierClassifierAlgorithm
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
            EDM.currentLocation,
            EDM.hasMet,
            EDM.hasType,
            EDM.isRelatedTo,
            EDM.dataProvider,
            EDM.provider,
            DC.rights,
            EDM.intermediateProvider
    );

    public static boolean isRelevantProperty(Statement s)
    {
        return ( RELEVANT_LANGUAGE_PROPERTIES.contains(s.getPredicate()) );
    }

    private EDMExternalCrawler _crawler
        = new EDMExternalCrawler(false, true, true);

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

                RDFNode obj = stmt.getObject();
                if ( obj.isLiteral() )
                {
                    m.count++;
                    if ( !StringUtils.isEmpty(stmt.getLanguage()) ) { m.match++; }
                    continue;
                }

                Resource r2 = obj.asResource();
                if ( !isAcceptableEntity(r2) ) { continue; }

                m.count++;
                if ( isLanguageQualified(r2) ) { m.match++; }
            }
        }
        finally { iter.close(); }
    }

    private boolean isAcceptableEntity(Resource r)
    {
        Resource v = r.getPropertyResourceValue(RDF.type);
        return ((v != null) && (EDM.Agent.equals(v) || EDM.Place.equals(v)
                            || SKOS.Concept.equals(v)));
    }

    private boolean isLanguageQualified(Resource r)
    {
        StmtIterator iter = r.listProperties(SKOS.prefLabel);
        try
        {
            while ( iter.hasNext() )
            {
                Statement stmt = iter.next();
                if ( !StringUtils.isEmpty(stmt.getLanguage()) ) { return true; }
            }
        }
        finally { iter.close(); }

        return false;
    }

    private static class Measure
    {
        protected int match;
        protected int count;
    }
}
