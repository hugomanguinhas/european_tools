/**
 * 
 */
package eu.europeana.pf2.db.type;

import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.MappingException;

import eu.europeana.pf2.media.MediaType;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 19 Jul 2018
 */
public class MediaTypeConverter extends TypeConverter 
{
    public MediaTypeConverter() { super(MediaType.class); }

    @Override
    public Object decode(final Class targetClass
                       , final Object obj
                       , final MappedField info ) throws MappingException
    {
        return (obj == null ? null : MediaType.getMedia((String)obj));
    }

    @Override
    public Object encode(final Object obj
                       , final MappedField info )
    {
        return (obj == null ? null : ((MediaType)obj).getLabel());
    }

}
