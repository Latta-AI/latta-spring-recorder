package ai.latta.core.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateSnapshot {

    @JsonProperty
    String message;

    @JsonProperty("relation_id")
    String relationId;

    @JsonProperty("related_to_relation_id")
    String relatedRelationId;

    public CreateSnapshot(String message) {
        this.message = message;
    }



    public static CreateSnapshot fromRelation(String message) {
        return fromRelation(message, UUID.randomUUID().toString());
    }

    public static CreateSnapshot fromRelation(String message, String relationId) {
        var snapshot = new CreateSnapshot(message);
        snapshot.relationId = relationId;
        return snapshot;
    }
    public static CreateSnapshot fromRelatedRelation(String message, String relatedRelationId) {
        var snapshot = new CreateSnapshot(message);
        snapshot.relatedRelationId = relatedRelationId;
        return snapshot;
    }
}
