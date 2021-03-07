package com.db.dataplatform.techtest.server.persistence.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "DATA_STORE")
@Setter
@Getter
public class DataBodyEntity {

    @Id
    @SequenceGenerator(name = "dataStoreSequenceGenerator", sequenceName = "SEQ_DATA_STORE", allocationSize = 1)
    @GeneratedValue(generator = "dataStoreSequenceGenerator")
    @Column(name = "DATA_STORE_ID")
    private Long dataStoreId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "DATA_HEADER_ID")
    private DataHeaderEntity dataHeaderEntity;

    @Column(name = "DATA_BODY")
    private String dataBody;

    @Column(name = "CREATED_TIMESTAMP")
    private Instant createdTimestamp;

    @PrePersist
    public void setTimestamps() {
        if (createdTimestamp == null) {
            createdTimestamp = Instant.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataBodyEntity)) return false;
        DataBodyEntity that = (DataBodyEntity) o;
        return Objects.equals(dataStoreId, that.dataStoreId) && Objects.equals(dataHeaderEntity, that.dataHeaderEntity) && Objects.equals(dataBody, that.dataBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataStoreId, dataHeaderEntity, dataBody);
    }
}
