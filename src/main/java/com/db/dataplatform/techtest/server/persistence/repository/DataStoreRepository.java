package com.db.dataplatform.techtest.server.persistence.repository;

import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataStoreRepository extends JpaRepository<DataBodyEntity, Long> {

    @Query(value = "SELECT d.* FROM DATA_STORE d join DATA_HEADER h on h.data_header_id=d.data_header_id where blocktype=:blockType", nativeQuery = true)
    DataBodyEntity findByBlockType(@Param("blockType") String blockType);

    @Query(value = "SELECT d.* FROM DATA_STORE d join DATA_HEADER h on h.data_header_id=d.data_header_id where name=:blockName", nativeQuery = true)
    Optional<DataBodyEntity> findByBlockName(@Param("blockName") String blockName);
}
