/**
 * 
 */
package eu.europeana.pf2.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;

import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.EuropeanaDataUtils;
import eu.europeana.ld.edm.ORE;
import eu.europeana.ld.edm.RDAGR2;


/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 25 Oct 2018
 */
public class MetadataTierConstants
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

//    public static List<String> LOD_VOCABULARIES = loadLODs();

    public static List<Group> GROUPS = Arrays.asList(
            new Group(DCTerms.created    , null
                    , DCTerms.issued     , null
                    , DCTerms.temporal   , null
                    , EDM.hasMet         , EDM.TimeSpan)
          , new Group(DC.subject         , SKOS.Concept
                    , DC.format          , null
                    , DC.type            , null
                    , DCTerms.medium     , null)
          , new Group(DC.creator         , null
                    , DCTerms.contributor, null
                    , DC.publisher       , null
                    , DC.subject         , EDM.Agent
                    , EDM.hasMet         , EDM.Agent)
          , new Group(DC.subject         , EDM.Place
                    , DCTerms.spatial    , null
                    , EDM.currentLocation, null)
    );

    public static class Group extends HashMap<Property, Resource>
    {
        public Group(Object... props)
        {
            for (int i = 0; i < props.length; i++)
            {
                Property p = (Property)props[i];
                Resource r = (Resource)props[++i];
                put(p, r);
            }
        }
    }
    public static boolean inGroup(Property p)
    {
        for ( Group g : GROUPS )
        {
            if ( !g.containsKey(p) ) { return true; }
         }
        return false;
    }

    public static Resource getProviderProxy(Model m)
    {
        ResIterator iter = m.listResourcesWithProperty(RDF.type, ORE.Proxy);
        while ( iter.hasNext() )
        {
            Resource r = iter.next();
            if ( isProviderProxy(r) ) { return r; }
        }
        return null;
    }

    public static boolean isProviderProxy(Resource r)
    {
        return ( r.hasProperty(EDM.europeanaProxy
                             , r.getModel().createLiteral("false")) );
    }

    public static boolean isEuropeanaProxy(Resource r)
    {
        return ( !isProviderProxy(r) );
    }

    public static boolean isEuropeanaAggregation(Resource r)
    {
        return r.hasProperty(RDF.type, EDM.EuropeanaAggregation);
    }

    public static boolean isRelevantProperty(Statement s)
    {
        return ( RELEVANT_LANGUAGE_PROPERTIES.contains(s.getPredicate()) 
              && s.getObject().isLiteral() );
    }

    public static boolean isEnrichment(Resource r)
    {
        return r.getURI().startsWith(EuropeanaDataUtils.NS);
    }

    /*
    public static boolean isDereferenced(Resource r)
    {
        String uri = r.getURI();
        for ( String lod : LOD_VOCABULARIES )
        {
            if ( uri.startsWith(lod) ) { return true; }
        }
        return false;
    }

    private static List<String> loadLODs()
    {
        try {
            return IOUtils.readLines(
                MetadataTierConstants.class.getResourceAsStream("/etc/cfg/lod.txt"));
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    */
}
