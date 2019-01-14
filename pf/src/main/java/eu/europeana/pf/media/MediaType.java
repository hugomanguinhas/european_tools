/**
 * 
 */
package eu.europeana.pf.media;

import eu.europeana.pf.alg.TierClassifierAlgorithm;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 15 Sep 2017
 */
public enum MediaType
{
    TEXT("TEXT", new TextClassifier())
  , IMAGE("IMAGE", new ImageClassifier())
  , SOUND("SOUND", new SoundClassifier())
  , VIDEO("VIDEO", new VideoClassifier())
  , M3D("3D", new M3DClassifier());

    private TierClassifierAlgorithm _classifier;
    private String                  _label;

    private MediaType(String label, TierClassifierAlgorithm c)
    { 
        _label      = label;
        _classifier = c;
    }

    public String                  getLabel()      { return _label;      }
    public TierClassifierAlgorithm getClassifier() { return _classifier; }

    public static MediaType getMedia(String type)
    {
        if ( type == null       ) { return null; }
        if ( type.equals("_3D") ) { return M3D;  }

        for ( MediaType mt : values() )
        {
            if ( mt.getLabel().equals(type) ) { return mt; }
        }
        return null;
    }
};