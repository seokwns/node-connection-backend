package node.connection.repository;

import node.connection.entity.FabricRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FabricRegisterRepository extends JpaRepository<FabricRegister, String> {
}
