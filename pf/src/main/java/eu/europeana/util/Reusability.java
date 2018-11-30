/**
 * 
 */
package eu.europeana.util;

import eu.europeana.pf.corelib.RightReusabilityCategorizer;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Sep 2017
 */
public enum Reusability
{
    OPEN, RESTRICTED;

    public static Reusability getReusability(String url)
    {
        if ( url == null ) { return null; }

        String reuse = new RightReusabilityCategorizer().categorize(url, 0);
        if ( reuse == null ) { return null; }

        reuse = reuse.toUpperCase().trim();
        for ( Reusability r : Reusability.values() )
        {
            if ( r.name().equals(reuse) ) { return r; }
        }
        return null;
    }
}
