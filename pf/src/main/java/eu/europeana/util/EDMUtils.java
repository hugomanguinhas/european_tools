/**
 * 
 */
package eu.europeana.util;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import eu.europeana.ld.edm.EDM;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 19 Jul 2018
 */
public class EDMUtils
{
    public static String getCountry(Resource eaggr)
    {
        if ( eaggr == null ) { return null; }

        Statement stmt = eaggr.getProperty(EDM.country);
        return ( stmt == null ? null : stmt.getLiteral().getString() );
    }

    public static String getDataProvider(Resource aggr)
    {
        if ( aggr == null ) { return null; }

        Statement stmt = aggr.getProperty(EDM.dataProvider);
        return ( stmt == null ? null : stmt.getLiteral().getString() );
    }

    public static String getProvider(Resource aggr)
    {
        if ( aggr == null ) { return null; }

        Statement stmt = aggr.getProperty(EDM.provider);
        return ( stmt == null ? null : stmt.getLiteral().getString() );
    }
}
