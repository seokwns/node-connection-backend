package node.connection.data.wallet.metadata.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MetadataType {
    VIEW("TYPE_VIEW"),
    OWNER("TYPE_OWNER");

    @Getter
    private final String type;
}
