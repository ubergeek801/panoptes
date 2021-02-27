package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEvaluationRequestMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code
 * PortfolioEvaluationRequest}.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioEvaluationRequestSerializer
    implements ProtobufSerializer<PortfolioEvaluationRequest> {
  /**
   * Creates a new {@code PortfolioEvaluationRequestSerializer}.
   */
  public PortfolioEvaluationRequestSerializer() {
    // nothing to do
  }

  @Override
  public PortfolioEvaluationRequest read(byte[] buffer) throws IOException {
    PortfolioEvaluationRequestMsg requestMsg = PortfolioEvaluationRequestMsg.parseFrom(buffer);
    IdVersionKeyMsg keyMsg = requestMsg.getPortfolioKey();
    PortfolioKey key = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
    EvaluationContextMsg evaluationContextMsg = requestMsg.getEvaluationContext();
    EvaluationContext evaluationContext =
        EvaluationContextSerializer.convert(evaluationContextMsg);

    Transaction transaction;
    if (requestMsg.hasTransaction()) {
      TransactionMsg transactionMsg = requestMsg.getTransaction();
      transaction = TransactionSerializer.convert(transactionMsg);
    } else {
      transaction = null;
    }

    return new PortfolioEvaluationRequest(key, transaction, evaluationContext);
  }

  @Override
  public byte[] write(PortfolioEvaluationRequest request) throws IOException {
    IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
    portfolioKeyBuilder.setId(request.getPortfolioKey().getId());
    portfolioKeyBuilder.setVersion(request.getPortfolioKey().getVersion());

    PortfolioEvaluationRequestMsg.Builder requestBuilder =
        PortfolioEvaluationRequestMsg.newBuilder();
    requestBuilder.setPortfolioKey(portfolioKeyBuilder);
    EvaluationContextMsg evaluationContext =
        EvaluationContextSerializer.convert(request.getEvaluationContext());
    requestBuilder.setEvaluationContext(evaluationContext);
    if (request.getTransaction() != null) {
      requestBuilder.setTransaction(TransactionSerializer.convert(request.getTransaction()));
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    requestBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
