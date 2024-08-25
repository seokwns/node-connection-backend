package node.connection.repository;

import node.connection.entity.CourtWalletConfig;
import node.connection.entity.pk.CourtKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtWalletConfigRepository extends JpaRepository<CourtWalletConfig, CourtKey> {
}
