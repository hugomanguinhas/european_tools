/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.RDAGR2;
import eu.europeana.ld.edm.WGS84;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import static eu.europeana.pf.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 18 Apr 2018
 */
public class ContextualClassClassifierV1 implements TierClassifierAlgorithm
{
    public String getLabel() { return MetadataDimension.CONTEXTUAL.getID(); }

    public int getLevels() { return 3; }

    public int classify(Model model)
    {
        Set<Resource> entities = new HashSet();

        Resource proxy = getProviderProxy(model);
        if ( proxy != null ) { classifyResource(proxy, entities); }

        ResIterator iter = model.listResourcesWithProperty(RDF.type
                                                         , EDM.WebResource);
        try
        {
            while ( iter.hasNext() ) { classifyResource(iter.next(),entities); }
        }
        finally { iter.close(); }

        int ne = entities.size();
        if ( ne >= 2 ) { return 3; }
        if ( ne == 1 ) { return 2; }
        return 1;
    }

    private void classifyResource(Resource r, Set<Resource> set)
    {
        StmtIterator iter = r.listProperties();
        while ( iter.hasNext() )
        {
            RDFNode  node = iter.next().getObject();
            if ( !node.isResource() ) { continue; }

            Resource e = node.asResource();
            if ( classifyEntity(e) ) { set.add(e); }
        }
    }

    private boolean classifyEntity(Resource r)
    {
        Resource v = r.getPropertyResourceValue(RDF.type);
        if ( v == null            ) { return false;               }
        if (EDM.Agent.equals(v)   ) { return classifyAgent(r);    }
        if (EDM.Place.equals(v)   ) { return classifyPlace(r);    }
        if (EDM.TimeSpan.equals(v)) { return classifyTimeSpan(r); }
        if (SKOS.Concept.equals(v)) { return classifyConcept(r);  }
        return false;
    }

    private boolean classifyAgent(Resource r)
    {
        boolean hasLabel = r.hasProperty(SKOS.prefLabel);
        boolean hasBegin = r.hasProperty(EDM.begin)
                        || r.hasProperty(RDAGR2.dateOfBirth);
        boolean hasEnd   = r.hasProperty(EDM.end)
                        || r.hasProperty(RDAGR2.dateOfDeath);
        return ( hasLabel && (hasBegin && hasEnd) );
    }

    private boolean classifyPlace(Resource r)
    {
        boolean hasLabel = r.hasProperty(SKOS.prefLabel);
        boolean hasLat   = r.hasProperty(WGS84.longitude);
        boolean hasLong  = r.hasProperty(WGS84.latitude);
        return (hasLabel && hasLat && hasLong );
    }

    private boolean classifyTimeSpan(Resource r)
    {
        boolean hasBegin = r.hasProperty(EDM.begin);
        boolean hasEnd   = r.hasProperty(EDM.end);
        return ( hasBegin && hasEnd );
    }

    private boolean classifyConcept(Resource r)
    {
        boolean hasLabel    = r.hasProperty(SKOS.prefLabel);
        boolean hasRelation = r.hasProperty(SKOS.broader)
                           || r.hasProperty(SKOS.narrower)
                           || r.hasProperty(SKOS.exactMatch)
                           || r.hasProperty(SKOS.closeMatch)
                           || r.hasProperty(SKOS.related);
        return ( hasLabel && hasRelation );
    }
}
