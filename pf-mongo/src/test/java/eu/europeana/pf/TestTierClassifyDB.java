/**
 * 
 */
package eu.europeana.pf;

import java.io.IOException;
import java.util.Collections;

import com.mongodb.MongoClient;

import eu.europeana.pf.ParallelTierClassifier;
import eu.europeana.pf.TierClassifier;
import eu.europeana.pf.alg.AlgorithmUtils;
import eu.europeana.pf.alg.CombinedClassifier;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import eu.europeana.pf.metadata.ContextualClassClassifier;
import eu.europeana.pf.metadata.EnablingElementsClassifier;
import eu.europeana.pf.metadata.LanguageClassifier;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 19 Jul 2018
 */
public class TestTierClassifyDB
{
    public static final void main(String[] args) throws IOException
    {
        MongoClient cli = new MongoClient("144.76.218.178", 27017);
        try
        {
            ParallelTierClassifier t = new ParallelTierClassifier(
                cli, "europeana_production_publish_1", "pf2"
              , Collections.EMPTY_LIST, 10
              , AlgorithmUtils.getMetadataAlgorithmsV2());
    
            //IMAGE
//            t.classify("/09428/u__orte_99");
//            t.classify("/2064102/Museu_ProvidedCHO_Stiftung_Preussische_Schl_sser_und_G_rten_Berlin_Brandenburg_237030");
//            t.classify("/2058605/HA9916");
//            t.classify("/11624/GEOCASEGIT_GIT_ESTONIA_99_200");
//            t.classify("/92070/BibliographicResource_1000126223920");
    
            //SOUND
//            t.classify("/08506/FFD664E4F506DC18C449FB89FB4DE0A3726D480D");
//            t.classify("/92056/URN_NBN_SI_snd_SKBLSLQD_");
//            t.classify("/92056/URN_NBN_SI_snd_ZB8NC4GH_");
//            t.classify("/9200257/BibliographicResource_3000055619975");
//            t.classify("/11622/_TIERSTIMMENARCHIV_MFN_GERMANY_TSA_Vultur_gryphus_Lue_61_6_1");
    
            //VIDEO
//            t.classify("/9200445/BibliographicResource_3000149208339");
//            t.classify("/2022611/H_DF_DF_7876");
//            t.classify("/08626/1037479000000219003");
//            t.classify("/2022622/detaljer__req_3908");
            //t.classify("/08623/12951");
    
            //3D
//            t.classify("/2022202/uuid_ea9d0e13_362c_4b67_b453_4c3b3f8c5370");
//            t.classify("/2048715/RMAH_198614_NLHR");
//            t.classify("/2048703/object_HA_96");
//            t.classify("/2020738/UJAEN_HASSET_7918");
//            t.classify("/2026101/Partage_Plus_ProvidedCHO_Manx_National_Heritage_1954_5298");
    
            //TEXT
//            t.classify("/2048622/data_item_ecorr_burckhardtsource_99");
//            t.classify("/2048618/data_item_ub_ffm_horkheimer_Na_1_9");
//            t.classify("/2022087/20_500_11841_A12458AD_F962_46EE_856D_D78E5B007F8B_cho");
//            t.classify("/9200124/letters_judsam_2011_mar_dsa_object9997");
//            t.classify("/9200452/handle_123456789_67692");

            t.classifyDataset("2048202");
            
        }
        finally { cli.close(); }
    }
}
