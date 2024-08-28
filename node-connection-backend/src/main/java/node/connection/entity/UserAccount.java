package node.connection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.hyperledger.fabric.Client;
import node.connection.hyperledger.fabric.ca.Registrar;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class FabricRegister {
    @Id
    private String name;

    @Column
    private String mspId;

    @Column
    private String secret;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String enrollment;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public FabricRegister(String name, String mspId, String secret, String enrollment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.mspId = mspId;
        this.secret = secret;
        this.enrollment = enrollment;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static FabricRegister of(String name, String mspId, String secret, String enrollment) {
        return FabricRegister.builder()
                .name(name)
                .mspId(mspId)
                .secret(secret)
                .enrollment(enrollment)
                .build();
    }

    public static FabricRegister of(Registrar registrar, String secret, String enrollment) {
        return FabricRegister.builder()
                .name(registrar.getName())
                .mspId(registrar.getMspId())
                .secret(secret)
                .enrollment(enrollment)
                .build();
    }

    public static FabricRegister of(Client client, String secret, String enrollment) {
        return FabricRegister.builder()
                .name(client.getName())
                .mspId(client.getMspId())
                .secret(secret)
                .enrollment(enrollment)
                .build();
    }
}
