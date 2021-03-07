package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.service.DataHeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final DataHeaderService dataHeaderServiceImpl;
    private final ModelMapper modelMapper;

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {
        // Save to persistence.
        persist(envelope);

        log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        return true;
    }

    @Override
    public DataEnvelope getDataEnvelopes(BlockTypeEnum blockType) {
        log.info("Getting data envelope for block Type: {}", blockType);
        DataBodyEntity dataBodyEntity = dataBodyServiceImpl.getDataByBlockType(blockType);

        DataEnvelope dataEnvelopes = new DataEnvelope();
        DataHeader dataHeader = modelMapper.map(dataBodyEntity.getDataHeaderEntity() , DataHeader.class);
        DataBody dataBody = modelMapper.map(dataBodyEntity, DataBody.class);

        return dataEnvelopes;
    }

    @Override
    public boolean updateBlockTypeOnBlockName(String blockName, BlockTypeEnum blockType){
        DataBodyEntity dataBodyEntity = dataBodyServiceImpl.getDataByBlockName(blockName).orElseThrow(IllegalArgumentException::new);

        DataHeaderEntity dataHeaderEntity = dataBodyEntity.getDataHeaderEntity();

        dataHeaderEntity.setBlocktype(blockType);

        dataHeaderServiceImpl.saveHeader(dataHeaderEntity);

        return true;
    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

}
