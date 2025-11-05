package is.hi.hbv501g.demo.dto;

import is.hi.hbv501g.demo.entity.VoteTargetType;
import java.util.UUID;

public class VoteRequest {

    private UUID targetId;
    private VoteTargetType targetType;
    private int direction;

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public VoteTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(VoteTargetType targetType) {
        this.targetType = targetType;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
