package is.hi.hbv501g.demo.dto;

import is.hi.hbv501g.demo.entity.VoteTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class VoteRequest {

    @NotNull(message = "targetId is required")
    private UUID targetId;

    @NotNull(message = "targetType is required")
    private VoteTargetType targetType;

    @Min(value = -1, message = "direction must be -1, 0 or 1")
    @Max(value = 1, message = "direction must be -1, 0 or 1")
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
