package org.slaq.slaqworx.panoptes.asset;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * SecurityAttributeDeserializer is a Jackson MapDeserializer that handles SecurityAttributes.
 *
 * @author jeremy
 */
public class SecurityAttributeDeserializer extends MapDeserializer {

    public SecurityAttributeDeserializer(JavaType mapType, ValueInstantiator valueInstantiator,
            KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser,
            TypeDeserializer valueTypeDeser) {
        super(mapType, valueInstantiator, keyDeser, valueDeser, valueTypeDeser);
        System.err.println("SecurityAttributeDeserializer() created");
    }
}
