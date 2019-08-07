package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.SecurityMsg;

/**
 * <code>SecuritySerializer</code> (de)serializes the state of a <code>Security</code> using
 * Protobuf.
 *
 * @author jeremy
 */
public class SecuritySerializer implements ByteArraySerializer<Security> {
    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.SECURITY.ordinal();
    }

    @Override
    public Security read(byte[] buffer) throws IOException {
        SecurityMsg securityMsg = SecurityMsg.parseFrom(buffer);
        Map<String, String> msgAttributes = securityMsg.getAttributesMap();
        Map<SecurityAttribute<?>, ? super Object> attributes = msgAttributes.entrySet().stream()
                .collect(Collectors.toMap(e -> SecurityAttribute.of(e.getKey()), e -> {
                    try {
                        return SerializerUtil.coerce(SecurityAttribute.of(e.getKey()),
                                SerializerUtil.defaultJsonMapper().readValue(e.getValue(),
                                        String.class));
                    } catch (IOException ex) {
                        // FIXME throw a better exception
                        throw new RuntimeException(ex);
                    }
                }));
        return new Security(attributes);
    }

    @Override
    public byte[] write(Security security) throws IOException {
        SecurityMsg.Builder securityBuilder = SecurityMsg.newBuilder();
        for (Entry<SecurityAttribute<?>, Object> attributeEntry : security.getAttributes()
                .entrySet()) {
            SecurityAttribute<?> attribute = attributeEntry.getKey();
            Object value = attributeEntry.getValue();
            try {
                securityBuilder.putAttributes(attribute.getName(),
                        SerializerUtil.defaultJsonMapper().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new IOException("could not serialize value " + value + "of attribute "
                        + attribute.getName(), e);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        securityBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
