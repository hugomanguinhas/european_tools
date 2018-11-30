/**
 * 
 */
package eu.europeana.pf2.metadata;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import eu.europeana.ld.edm.EDM;
import eu.europeana.ld.edm.ORE;
import static eu.europeana.pf2.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 26 Oct 2018
 */
public class EDMExternalCrawler
{
    public Set<Resource> crawl(Model m)
    {
        return crawl(m, new HashSet());
    }

    public Set<Resource> crawl(Model m, Set<Resource> set)
    {
        Resource proxy = getProviderProxy(m);
        crawlForEntities(proxy, set).add(proxy);
        crawlAggregation(m, set);
        return set;
    }

    private Set<Resource> crawlAggregation(Model m, Set<Resource> set)
    {
        ResIterator iter = m.listResourcesWithProperty(RDF.type, ORE.Aggregation);
        try
        {
            while ( iter.hasNext() )
            {
                Resource aggr = iter.next();
                set.add(aggr);
                crawlWebResources(aggr, EDM.isShownBy, set);
                crawlWebResources(aggr, EDM.isShownAt, set);
                crawlWebResources(aggr, EDM.hasView  , set);
            }
        }
        finally { iter.close(); }
        
        return set;
    }

    private void crawlWebResources(Resource proxy, Property p, Set<Resource> set)
    {
        StmtIterator iter = proxy.listProperties(p);
        try
        {
            while ( iter.hasNext() )
            {
                Resource wr = iter.next().getResource();
                crawlForEntities(wr, set).add(wr);
            }
        }
        finally { iter.close(); }
    }

    private Set<Resource> crawlForEntities(Resource r, Set<Resource> set)
    {
        StmtIterator iter = r.listProperties();
        try
        {
            while ( iter.hasNext() )
            {
                RDFNode  node = iter.next().getObject();
                if ( !node.isResource() ) { continue; }

                Resource obj  = node.asResource();
                if ( obj.hasProperty(RDF.type, EDM.Agent)
                  || obj.hasProperty(RDF.type, EDM.Place)
                  || obj.hasProperty(RDF.type, SKOS.Concept)
                  || obj.hasProperty(RDF.type, EDM.TimeSpan) ) { set.add(obj); }
            }
        }
        finally { iter.close(); }

        return set;
    }
}
