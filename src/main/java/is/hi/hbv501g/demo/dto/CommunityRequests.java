package is.hi.hbv501g.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class CommunityRequests {

    private CommunityRequests() {}

    public static class CreateCommunityRequest {

        @NotBlank(message = "name is required")
        @Size(min = 3, max = 100, message = "name must be between 3 and 100 characters")
        private String name;

        @Size(max = 1000, message = "description cannot exceed 1000 characters")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class MembershipRequest {

        @NotBlank(message = "communityName is required")
        private String communityName;

        private UUID userId;

        public String getCommunityName() {
            return communityName;
        }

        public void setCommunityName(String communityName) {
            this.communityName = communityName;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }
    }
}
