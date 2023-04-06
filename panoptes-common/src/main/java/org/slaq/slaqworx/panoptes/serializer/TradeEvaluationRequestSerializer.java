package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeEvaluationRequestMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link TradeEvaluationRequest}.
 *
 * @author jeremy
 */
public class TradeEvaluationRequestSerializer
    implements ProtobufSerializer<TradeEvaluationRequest> {
  /** Creates a new {@link TradeEvaluationRequestSerializer}. */
  public TradeEvaluationRequestSerializer() {
    // nothing to do
  }

  @Override
  public TradeEvaluationRequest read(byte[] buffer) throws IOException {
    TradeEvaluationRequestMsg requestMsg = TradeEvaluationRequestMsg.parseFrom(buffer);

    IdKeyMsg keyMsg = requestMsg.getTradeKey();
    TradeKey tradeKey = new TradeKey(keyMsg.getId());

    EvaluationContext context =
        EvaluationContextSerializer.convert(requestMsg.getEvaluationContext());

    return new TradeEvaluationRequest(tradeKey, context);
  }

  @Override
  public byte[] write(TradeEvaluationRequest request) throws IOException {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(request.getTradeKey().getId());

    TradeEvaluationRequestMsg.Builder requestBuilder = TradeEvaluationRequestMsg.newBuilder();
    requestBuilder.setTradeKey(keyBuilder.build());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    requestBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
