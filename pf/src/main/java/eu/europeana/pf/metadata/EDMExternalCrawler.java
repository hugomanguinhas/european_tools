/**
 * 
 */
package eu.europeana.pf.metadata;

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
import static eu.europeana.pf.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 26 Oct 2018
 */
public class EDMExternalCrawler
{
    private boolean _entities;
    private boolean _webResources;
    private boolean _aggr;

    public EDMExternalCrawler() { this(true, true, true); }

    public EDMExternalCrawler(boolean entities, boolean webResources
                            , boolean aggr)
    {
        _entities     = entities;
        _webResources = webResources;
        _aggr         = aggr;
    }

    public Set<Resource> crawl(Model m)
    {
        return crawl(m, new HashSet());
    }

    public Set<Resource> crawl(Model m, Set<Resource> set)
    {
        Resource proxy = getProviderProxy(m);
        set.add(proxy);

        if ( _entities ) { crawlForEntities(proxy, set); }
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
                if ( _aggr ) { set.add(aggr); }
                //did not consider edm:object because statements on a thumbnail 
                //do not make sense to count
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
                if ( _entities     ) { crawlForEntities(wr, set); }
                if ( _webResources ) { set.add(wr);               }
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
