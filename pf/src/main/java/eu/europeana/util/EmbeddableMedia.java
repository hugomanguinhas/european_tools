/**
 * 
 */
package eu.europeana.util;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 22 Sep 2017
 */
public class EmbeddableMedia
{
    private static Collection<String> prefixes = Arrays.asList(
        "urn:soundcloud:"
      , "http://player.vimeo.com/video/"
      , "https://vimeo.com/"
      , "https://sketchfab.com/models/"
      , "http://sounds.bl.uk/embed/"
      , "http://eusounds.ait.co.at/"
      , "http://images3.noterik.com/edna/domain/euscreenxl/"
      
      , "http://www.ccma.cat/tv3/alacarta/programa/titol/video/"
      , "http://www.ina.fr/video/"
      , "http://www.ina.fr/*/video/"
      , "http://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token="
      , "https://api.picturepipe.net/api/html/widgets/public/playout_cloudfront?token="
      , "http://archives.crem-cnrs.fr/archives/items/"
      , "http://www.theeuropeanlibrary.org/tel4/newspapers/issue/fullscreen/"
    );

    public static boolean isEmbeddable(String url)
    {
        if ( StringUtils.isEmpty(url) ) { return false; }

        for ( String prefix : prefixes )
        {
            if ( url.startsWith(prefix) ) { return true; }
        }
        return false;
    }
}
