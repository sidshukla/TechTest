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
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

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

        String clientMd5Sum = envelope.getDataBody().getMd5Sum();

        String generatedMd5Sum = generateMd5Sum(envelope.getDataBody().getDataBody());

        log.info("Generated MD5Sum : {}", generatedMd5Sum);

        //Generate and compare the MD5Sum
        if(clientMd5Sum.equals(generatedMd5Sum)){
            // Save to persistence.
            persist(envelope);

            log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
            return true;
        }

        log.info("Md5Sum Mistmatch, cannot persist data");

        return false;
    }

    @Override
    public List<DataEnvelope> getDataEnvelopes(BlockTypeEnum blockType) {
        log.info("Getting data envelope for block Type: {}", blockType);
        List<DataBodyEntity> dataBodyEntities = dataBodyServiceImpl.getDataByBlockType(blockType);

        List<DataEnvelope> dataEnvelopes = new ArrayList<>();
        for(DataBodyEntity dataBodyEntity : dataBodyEntities){

            DataHeader dataHeader = modelMapper.map(dataBodyEntity.getDataHeaderEntity() , DataHeader.class);
            DataBody dataBody = modelMapper.map(dataBodyEntity, DataBody.class);
            DataEnvelope dataEnvelope = new DataEnvelope(dataHeader,dataBody);
            dataEnvelopes.add(dataEnvelope);
        }


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

    private String generateMd5Sum(String dataBody) {
        return DigestUtils.md5DigestAsHex(dataBody.getBytes());
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

}
