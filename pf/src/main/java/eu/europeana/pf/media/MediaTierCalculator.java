/**
 * 
 */
package eu.europeana.pf.media;

import eu.europeana.pf.model.Record;
import eu.europeana.pf.model.WebResource;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 13 Sep 2017
 */
public interface MediaTierCalculator
{
    public int getTier(Record record);
    public int getTier(WebResource wr);
}