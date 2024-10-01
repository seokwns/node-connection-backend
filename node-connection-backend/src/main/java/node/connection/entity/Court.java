package node.connection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection._core.utils.Hash;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.entity.pk.CourtKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Court {
    @EmbeddedId
    private CourtKey key;

    @Column
    private String channelName;

    @Column
    private String phoneNumber;

    @Column
    private String address;

    @Column
    private String faxNumber;

    @Column(unique = true)
    private String registerCode;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Court(CourtKey key, String channelName, String phoneNumber, String address, String faxNumber, String registerCode, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.key = key;
        this.channelName = channelName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.faxNumber = faxNumber;
        this.registerCode = registerCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Court of(CourtCreateRequest request) {
        CourtKey courtKey = CourtKey.builder()
                .court(request.getCourt())
                .support(request.getSupport())
                .office(request.getOffice())
                .build();

        String registerCode = Hash.generate();

        return Court.builder()
                .key(courtKey)
                .channelName(request.getChannelName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .faxNumber(request.getFaxNumber())
                .registerCode(registerCode)
                .build();
    }
}