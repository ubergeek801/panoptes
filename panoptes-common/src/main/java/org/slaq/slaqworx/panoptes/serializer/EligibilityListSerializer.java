package org.slaq.slaqworx.panoptes.serializer;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.slaq.slaqworx.panoptes.asset.EligibilityList;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EligibilityListMsg;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of an {@code EligibilityList}.
 *
 * @author jeremy
 */
@Singleton
public class EligibilityListSerializer implements ProtobufSerializer<EligibilityList> {
  /** Creates a new {@link EligibilityListSerializer}. */
  public EligibilityListSerializer() {
    // nothing to do
  }

  @Override
  public EligibilityList read(byte[] buffer) throws IOException {
    EligibilityListMsg msg = EligibilityListMsg.parseFrom(buffer);
    String name = msg.getName();
    EligibilityListMsg.ListType type = msg.getType();
    String description = msg.getDescription();
    Set<String> items = new HashSet<>(msg.getItemList());

    return new EligibilityList(name, type, description, items);
  }

  @Override
  public byte[] write(EligibilityList eligibilityList) throws IOException {
    EligibilityListMsg.Builder listBuilder = EligibilityListMsg.newBuilder();
    listBuilder.setName(eligibilityList.name());
    listBuilder.setType(eligibilityList.type());
    listBuilder.setDescription(eligibilityList.description());
    listBuilder.addAllItem(eligibilityList.items());
    EligibilityListMsg msg = listBuilder.build();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    return out.toByteArray();
  }
}
