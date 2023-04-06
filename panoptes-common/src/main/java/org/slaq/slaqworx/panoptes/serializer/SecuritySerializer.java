package org.slaq.slaqworx.panoptes.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.SecurityMsg;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link Security}.
 *
 * @author jeremy
 */
public class SecuritySerializer implements ProtobufSerializer<Security> {
  /** Creates a new {@link SecuritySerializer}. */
  public SecuritySerializer() {
    // nothing to do
  }

  /**
   * Converts a {@link SecurityAttributes} instance into a new {@link SecurityMsg}.
   *
   * @param securityAttributes the {@link SecurityAttributes} to be converted
   * @return a {@link SecurityMsg}
   */
  public static SecurityMsg convert(SecurityAttributes securityAttributes) {
    SecurityMsg.Builder securityBuilder = SecurityMsg.newBuilder();
    for (Entry<SecurityAttribute<?>, Object> attributeEntry :
        securityAttributes.asMap().entrySet()) {
      SecurityAttribute<?> attribute = attributeEntry.getKey();
      Object value = attributeEntry.getValue();
      try {
        securityBuilder.putAttributes(
            attribute.getName(), SerializerUtil.defaultJsonMapper().writeValueAsString(value));
      } catch (JsonProcessingException e) {
        // FIXME throw a better exception
        throw new RuntimeException(
            "could not serialize value " + value + "of attribute " + attribute.getName(), e);
      }
    }

    return securityBuilder.build();
  }

  /**
   * Converts a {@link SecurityMsg} into a new {@link Map} relating each {@link SecurityAttribute}
   * to its corresponding value.
   *
   * @param securityMsg the message to be converted
   * @return a {@link Map} relating each {@link SecurityAttribute} to its corresponding value
   */
  public static Map<SecurityAttribute<?>, ? super Object> convert(SecurityMsg securityMsg) {
    Map<String, String> msgAttributes = securityMsg.getAttributesMap();
    Map<SecurityAttribute<?>, ? super Object> attributes =
        msgAttributes.entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> SecurityAttribute.of(e.getKey()),
                    e -> {
                      try {
                        return SerializerUtil.coerce(
                            SecurityAttribute.of(e.getKey()),
                            SerializerUtil.defaultJsonMapper()
                                .readValue(e.getValue(), String.class));
                      } catch (IOException ex) {
                        // FIXME throw a better exception
                        throw new RuntimeException(ex);
                      }
                    }));

    return attributes;
  }

  @Override
  public Security read(byte[] buffer) throws IOException {
    SecurityMsg securityMsg = SecurityMsg.parseFrom(buffer);

    return new Security(convert(securityMsg));
  }

  @Override
  public byte[] write(Security security) throws IOException {
    SecurityMsg securityMsg = convert(security.getAttributes());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    securityMsg.writeTo(out);
    return out.toByteArray();
  }
}
