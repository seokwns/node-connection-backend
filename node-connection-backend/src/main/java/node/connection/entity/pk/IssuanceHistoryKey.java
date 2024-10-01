package node.connection.entity.pk;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class IssuanceHistoryKey implements Serializable {
    private String fabricId;
    private String issuanceHash;

    @Builder
    public IssuanceHistoryKey(String fabricId, String issuanceHash) {
        this.fabricId = fabricId;
        this.issuanceHash = issuanceHash;
    }
}
