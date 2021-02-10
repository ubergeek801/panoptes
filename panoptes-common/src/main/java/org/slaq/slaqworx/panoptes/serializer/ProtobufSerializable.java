package org.slaq.slaqworx.panoptes.serializer;

/**
 * A marker interface to indicate that a Protobuf serializer exists for a particular type. This
 * mostly exists to remind developers that when adding state to such objects, the serialization
 * format may have to be updated as well.
 *
 * @author jeremy
 */
public interface ProtobufSerializable {
    // marker interface only
}
