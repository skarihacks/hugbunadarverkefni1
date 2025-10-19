package is.hi.hbv501g.demo.dto;

public final class CommunityRequests {

    private CommunityRequests() {}

    public static class CreateCommunityRequest {

        private String name;
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

    public static class JoinCommunityRequest {

        private String communityName;

        public String getCommunityName() {
            return communityName;
        }

        public void setCommunityName(String communityName) {
            this.communityName = communityName;
        }
    }
}
