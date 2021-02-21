package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RoomEvaluationRequestMsg;
import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code RoomEvaluationRequest}.
 *
 * @author jeremy
 */
public class RoomEvaluationRequestSerializer implements ProtobufSerializer<RoomEvaluationRequest> {
    /**
     * Creates a new {@code RoomEvaluationRequestSerializer}.
     */
    public RoomEvaluationRequestSerializer() {
        // nothing to do
    }

    @Override
    public RoomEvaluationRequest read(byte[] buffer) throws IOException {
        RoomEvaluationRequestMsg requestMsg = RoomEvaluationRequestMsg.parseFrom(buffer);

        PortfolioKey portfolioKey = new PortfolioKey(requestMsg.getPortfolioKey().getId(),
                requestMsg.getPortfolioKey().getVersion());
        SecurityKey securityKey = new SecurityKey(requestMsg.getSecurityKey().getId());

        return new RoomEvaluationRequest(portfolioKey, securityKey, requestMsg.getTargetValue());
    }

    @Override
    public byte[] write(RoomEvaluationRequest request) throws IOException {
        RoomEvaluationRequestMsg.Builder requestBuilder = RoomEvaluationRequestMsg.newBuilder();

        IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
        portfolioKeyBuilder.setId(request.getPortfolioKey().getId());
        portfolioKeyBuilder.setVersion(request.getPortfolioKey().getVersion());
        requestBuilder.setPortfolioKey(portfolioKeyBuilder.build());

        IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
        securityKeyBuilder.setId(request.getSecurityKey().getId());
        requestBuilder.setSecurityKey(securityKeyBuilder.build());

        requestBuilder.setTargetValue(request.getTargetValue());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        requestBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
