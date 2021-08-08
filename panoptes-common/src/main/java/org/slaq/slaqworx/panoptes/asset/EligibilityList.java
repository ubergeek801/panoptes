package org.slaq.slaqworx.panoptes.asset;

import java.util.Set;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EligibilityListMsg.ListType;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * Encapsulates a list of items that is used to determine eligibility for (or restriction of)
 * investment. The interpretation of the list items is specific to the list type; for example, a
 * security eligibility list might contain CUSIPs.
 *
 * @param name
 *     a short name used to identify the list
 * @param type
 *     the type of entity identified by each item on the list
 * @param description
 *     a description of the list and/or its purpose
 * @param items
 *     the items comprising the list
 */
public record EligibilityList(@Nonnull String name, @Nonnull ListType type,
                              @Nonnull String description, @Nonnull Set<String> items)
    implements ProtobufSerializable {
}
