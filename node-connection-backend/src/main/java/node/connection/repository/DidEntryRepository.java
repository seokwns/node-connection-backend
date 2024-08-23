package node.connection.repository;

import node.connection.entity.DidEntry;
import node.connection.entity.pk.DidEntryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DidEntryRepository extends JpaRepository<DidEntry, DidEntryKey> {
    @Query("SELECT d FROM DidEntry d WHERE d.key.phoneNumber = :phoneNumber")
    List<DidEntry> findAllByPhoneNumber(String phoneNumber);
}
