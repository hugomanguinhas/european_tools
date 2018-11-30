package eu.europeana.pf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Unit test for simple App.
 */
public class TestTierClassifier 
{
    private TierClassifier _tc;
    private TierReport         _report;

    public TestTierClassifier(MongoDatabase db, Collection<String> filter)
    {
        _tc     = new TierClassifier(db, 10, filter);
        _report = new TierReport();
    }

    public void test(String url)
    {
        _tc.classifyRecord(_report, url);
    }

    public void testDataset(String datasetId)
    {
        _tc.classifyDatasets(_report, datasetId);
    }

    public void print()
    {
        _report.printPerCountry(System.out);
    }

    /***************************************************************************
     * Private Methods
     * @throws IOException 
     **************************************************************************/

    public static void main( String[] args ) throws IOException
    {
        MongoClient   cli = new MongoClient("mongo1.eanadev.org", 27017);
//        MongoClient   cli = new MongoClient("reindexing1.eanadev.org", 27017);
        Collection<String> filter = FileUtils.readLines(new File("D:\\work\\incoming\\diff_mongo_solr\\records_mongo_not_solr.csv"));
        try
        {
//            MongoDatabase db  = cli.getDatabase("europeana_1");
            TestTierClassifier t = new TestTierClassifier(cli.getDatabase("europeana_production_publish_1"), filter);

            //IMAGE
            //t.test("/09428/u__orte_99");
            //t.test("/2064102/Museu_ProvidedCHO_Stiftung_Preussische_Schl_sser_und_G_rten_Berlin_Brandenburg_237030");
            //t.test("/2058605/HA9916");
            //t.test("/11624/GEOCASEGIT_GIT_ESTONIA_99_200");
            //t.test("/92070/BibliographicResource_1000126223920");

            //SOUND
            //t.test("/08506/FFD664E4F506DC18C449FB89FB4DE0A3726D480D");
            //t.test("/92056/URN_NBN_SI_snd_SKBLSLQD_");
            //t.test("/92056/URN_NBN_SI_snd_ZB8NC4GH_");
            //t.test("/9200257/BibliographicResource_3000055619975");
            //t.test("/11622/_TIERSTIMMENARCHIV_MFN_GERMANY_TSA_Vultur_gryphus_Lue_61_6_1");

            //VIDEO
//            t.test("/9200445/BibliographicResource_3000149208339");
//            t.test("/2022611/H_DF_DF_7876");
//            t.test("/08626/1037479000000219003");
//            t.test("/2022622/detaljer__req_3908");
//            t.test("/08623/12951");

            //3D
            //t.test("/2022202/uuid_ea9d0e13_362c_4b67_b453_4c3b3f8c5370");
            //t.test("/2048715/RMAH_198614_NLHR");
            //t.test("/2048703/object_HA_96");
            //t.test("/2020738/UJAEN_HASSET_7918");
            //t.test("/2026101/Partage_Plus_ProvidedCHO_Manx_National_Heritage_1954_5298");

            //TEXT
            //t.test("/2048622/data_item_ecorr_burckhardtsource_99");
            //t.test("/2048618/data_item_ub_ffm_horkheimer_Na_1_9");
            //t.test("/9200124/letters_judsam_2011_mar_dsa_object9997");
            //t.test("/9200452/handle_123456789_67692");
            //t.test("/2022087/20_500_11841_A12458AD_F962_46EE_856D_D78E5B007F8B_cho");

            t.testDataset("2021009");
            t.print();
        }
        finally { cli.close(); }
    }
}
