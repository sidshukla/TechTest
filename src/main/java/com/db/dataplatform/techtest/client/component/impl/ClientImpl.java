package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.component.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{blockName}/{newBlockType}");

    @Autowired
    RestTemplate restTemplate;

    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        Boolean response = restTemplate.postForObject(URI_PUSHDATA,dataEnvelope,Boolean.class);
        log.info("Push data response {}", response);
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        URI getData = URI_GETDATA.expand(blockType);
        //TODO return list
        DataEnvelope response = restTemplate.getForObject(getData, DataEnvelope.class);
        log.info("Get data response {}", response);
        return Arrays.asList(response);
    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blockType to {} for block with name {}", newBlockType, blockName);
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("blockName", blockName);
        urlParams.put("newBlockType", newBlockType);

        Boolean response = restTemplate.patchForObject(URI_PATCHDATA.toString(), String.class,Boolean.class,urlParams);
        log.info("Update data response {}", response);
        return true;
    }


}
