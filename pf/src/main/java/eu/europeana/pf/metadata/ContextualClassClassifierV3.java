/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.HashSet;
import java.util.Iterator;
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
 * 
 * 3rd version of the measurement for the contextual classes criteria
 
   - consider only contextual entities that are directly associated to the Proxy 
     or edm:WebResource (referred from edm:isShownBy and edm:hasView)

   - contextual entities need to comply with minimal requirements

   
 
   Changes:

   - Tier C now mandates that the contextual resources need to be from 2 
     different classes
 */
public class ContextualClassClassifierV3 implements TierClassifierAlgorithm
{
    private EDMExternalCrawler _crawler
        = new EDMExternalCrawler(true, false, false, false);

    public String getLabel() { return MetadataDimension.CONTEXTUAL.getID(); }

    public int getLevels() { return 3; }

    public int classify(Model model)
    {
        Set<Resource> entities = classifyEntities(_crawler.crawl(model));

        int ne = entities.size();
        int nc = countClasses(entities);

        if ( ne >= 2 && nc >=2 ) { return 3; }
        if ( ne >  1           ) { return 2; }
        return 1;
    }

    private int countClasses(Set<Resource> entities)
    {
        Set<Resource> classes = new HashSet();
        for ( Resource entity : entities )
        {
            Resource type = entity.getPropertyResourceValue(RDF.type);
            if ( type != null ) { classes.add(type); }
        }
        return classes.size();
    }

    private Set<Resource> classifyEntities(Set<Resource> entities)
    {
        Iterator<Resource> iter = entities.iterator();
        while ( iter.hasNext() )
        {
            Resource e = iter.next();
            if ( !classifyEntity(e) ) { iter.remove(); }
        }
        return entities;
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

    /*
     * added rdagr2:professionOrOccupation, rdagr2:placeOfBirth, 
     * rdagr2:placeOfDeath and lifted the constraint for the existence of 
     * date of birth and death at the same time
     */
    private boolean classifyAgent(Resource r)
    {
        boolean hasLabel = r.hasProperty(SKOS.prefLabel);
        boolean hasBegin = r.hasProperty(EDM.begin)
                        || r.hasProperty(RDAGR2.dateOfBirth);
        boolean hasEnd   = r.hasProperty(EDM.end)
                        || r.hasProperty(RDAGR2.dateOfDeath);
        boolean prof     = r.hasProperty(RDAGR2.professionOrOccupation);
        boolean pBirth   = r.hasProperty(RDAGR2.placeOfBirth);
        boolean pDeath   = r.hasProperty(RDAGR2.placeOfDeath);
        return ( hasLabel && (hasBegin || hasEnd || prof || pBirth || pDeath) );
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

    //added skos:note, however other match properties are missing!!!
    private boolean classifyConcept(Resource r)
    {
        boolean hasLabel    = r.hasProperty(SKOS.prefLabel);
        boolean hasNote     = r.hasProperty(SKOS.note);
        boolean hasRelation = r.hasProperty(SKOS.broader)
                           || r.hasProperty(SKOS.narrower)
                           || r.hasProperty(SKOS.exactMatch)
                           || r.hasProperty(SKOS.closeMatch)
                           || r.hasProperty(SKOS.related);
        return ( hasLabel && ( hasRelation || hasNote ));
    }
}
