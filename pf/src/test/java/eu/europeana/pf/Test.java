/**
 * 
 */
package eu.europeana.pf;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 21 Sep 2017
 */
public class Test
{
    private static HashFunction  _hf  = Hashing.md5();

    private static String getTechMetaID(String recordID, String wrID)
    {
        HashCode hashCode = _hf.newHasher()
                .putString(wrID, Charsets.UTF_8)
                .putString("-", Charsets.UTF_8)
                .putString(recordID, Charsets.UTF_8)
                .hash();
        return hashCode.toString();
    }

    public static final void main(String[] args)
    {
        System.out.println(getTechMetaID(
                "/2021672/resource_document_mauritshuis_974"
              , "http://fileshare.mauritshuis.nl/collectie/974_repro.jpg"));
        System.out.println(getTechMetaID(
                "/2048618/data_item_ub_ffm_horkheimer_Na_1_9"
              , "http://sammlungen.ub.uni-frankfurt.de/horkheimer/download/webcache/304/3899294"));
        System.out.println(getTechMetaID(
                "/9200124/letters_judsam_2011_mar_dsa_object9997"
              , "http://www.kb.dk/e-mat/cop/letters/judsam/letters-judsam-2011-mar-dsa-object9997.pdf"));
    }
}
