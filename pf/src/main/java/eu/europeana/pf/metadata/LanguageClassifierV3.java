/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import eu.europeana.ld.edm.EDM;
import eu.europeana.pf.alg.TierClassifierAlgorithm;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 9 May 2018
 
   Third version of the language classifier considering:

     - only the statements that are part of the record, aggregation and web resource

     - only properties that are relevant for the language dimension

     - a property (associated to any of the EDM classes) is counted only once

     - a property is counted if at least one of the following is true:
        1) a literal value (String) that is language qualified (has xml:lang)
        2) a link (ie. URI) to a contextual entity (only edm:Place, skos:Concept 
           and edm:TimeSpan) that is present in the record and has at least one 
           language qualified skos:prefLabel.
 
 
 */
public class LanguageClassifierV3 implements TierClassifierAlgorithm
{
    public static List<Property> RELEVANT_LANGUAGE_PROPERTIES
        = Arrays.asList(
//            DC.contributor,
            DC.coverage,
//            DC.creator,
            DC.description,
            DC.format,
//            DC.publisher,
            DC.relation,
            DC.rights,
            DC.source,
            DC.subject,
            DC.title,
            DC.type,
            DCTerms.alternative,
            DCTerms.hasPart,
            DCTerms.isPartOf,
            DCTerms.isReferencedBy,
            DCTerms.medium,
            DCTerms.provenance,
            DCTerms.references,
            DCTerms.spatial,
            DCTerms.tableOfContents,
            DCTerms.temporal,
            EDM.currentLocation,
            EDM.hasType,
            EDM.isRelatedTo,
            EDM.dataProvider,
            EDM.provider,
            EDM.intermediateProvider
    );

    private Collection<Property> _relevant = new HashSet<Property>();

    private EDMExternalCrawler _crawler
        = new EDMExternalCrawler(false, true, true, true);

    public LanguageClassifierV3(boolean withAgents)
    {
        _relevant.addAll(RELEVANT_LANGUAGE_PROPERTIES);
        if ( withAgents )
        {
            _relevant.add(DC.contributor);
            _relevant.add(DC.creator);
            _relevant.add(DC.publisher);
        }
    }

    public String getLabel() { return MetadataDimension.LANGUAGE.getID(); }

    public int getLevels() { return 3; }

    public int classify(Model model)
    {
        Measure measure = new Measure();
        for ( Resource r : _crawler.crawl(model) )
        {
            measureResource(r, measure);
        }

        float percent = measure.getPercentage();
        if ( percent < 0.25 ) { return 0; }
        if ( percent < 0.5  ) { return 1; }
        if ( percent < 0.75 ) { return 2; }
        return 3;
    }

    private boolean isRelevantProperty(Statement s)
    {
        return ( _relevant.contains(s.getPredicate()) );
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

                if ( isAcceptableEntity(stmt) ) { continue; }

                Property p = stmt.getPredicate();
                if ( !m.containsKey(p) ) { m.put(p, false); }

                RDFNode obj = stmt.getObject();
                if ( obj.isLiteral() )
                {
                    if ( StringUtils.isEmpty(stmt.getLanguage()) ) { continue; }

                    m.put(p, true);
                }

                Resource r2 = obj.asResource();
                if ( isLanguageQualified(r2) ) { m.put(p, true); }
            }
        }
        finally { iter.close(); }
    }

    /*
     * Checks if the statement can be considered for the language dimension 
     * given Agents were ignored for this tier calculation
     */
    private boolean isAcceptableEntity(Statement stmt)
    {
        RDFNode node = stmt.getObject();
        if ( !node.isResource() ) { return true; }

        Resource v = node.asResource().getPropertyResourceValue(RDF.type);
        return ((v != null) && (EDM.TimeSpan.equals(v) || EDM.Place.equals(v)
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

    @SuppressWarnings("serial")
    private static class Measure extends HashMap<Property,Boolean>
    {
        public float getPercentage()
        {
            int count = 0;
            for ( Map.Entry<Property, Boolean> entry : this.entrySet())
            {
                if ( entry.getValue() ) { count++; }
            }
            return ( (float)count / size() );
        }
    }
}
