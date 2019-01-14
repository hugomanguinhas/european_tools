/**
 * 
 */
package eu.europeana.pf.metadata;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import eu.europeana.pf.alg.TierClassifierAlgorithm;
import static eu.europeana.pf.metadata.MetadataTierConstants.*;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 17 Apr 2018
 */
public class EnablingElementsClassifier implements TierClassifierAlgorithm
{
    public String getLabel() { return MetadataDimension.ENABLING.getID(); }

    public int getLevels() { return 3; }

    public int classify(Model model)
    {
        return classify(getProviderProxy(model));
    }

    private int classify(Resource r)
    {
        //distinct properties
        Set<Property> props  = new HashSet();
        //distinct groups
        Set<Integer>  groups = new HashSet();

        StmtIterator iter = r.listProperties();
        while ( iter.hasNext() )
        {
            Statement stmt = iter.next();
            Property  p    = stmt.getPredicate();
            int group = getGroup(p, stmt.getObject());
            if ( group < 0 ) { continue; }

            groups.add(group);
            props.add(p);
        }

        int nGroups = groups.size();
        int nProps  = props.size();

        if ( nGroups >= 2 && nProps >= 4 ) { return 3; }
        if ( nGroups >= 2 && nProps >= 3 ) { return 2; }
        if ( nGroups >= 1 && nProps >= 1 ) { return 1; }
        return 0;
    }

    private int getGroup(Property p, RDFNode node)
    {
        for ( int i = 0; i < GROUPS.size(); i++ )
        {
            Group g = GROUPS.get(i);
            if ( !g.containsKey(p) ) { continue; }

            Resource r = g.get(p);
            if ( r == null ) { return i; }

            if ( node.isResource()
              && node.asResource().hasProperty(RDF.type, r)) { return i; }
        }
        return -1;
    }
}