package com.db.dataplatform.techtest.server.api.controller;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
@RequestMapping("/dataserver")
@RequiredArgsConstructor
@Validated
public class ServerController {

    public static final String URI_PUSHDATA = "http://localhost:8090/hadoopserver/pushbigdata";

    private final Server server;

    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> pushData(@Valid @RequestBody DataEnvelope dataEnvelope) throws IOException, NoSuchAlgorithmException {

        log.info("Data envelope received: {}", dataEnvelope.getDataHeader().getName());
        boolean checksumPass = server.saveDataEnvelope(dataEnvelope);

        if(checksumPass){
          pushToDataLake(dataEnvelope.toString());
        }

        log.info("Data envelope persisted. Attribute name: {}", dataEnvelope.getDataHeader().getName());
        return ResponseEntity.ok(checksumPass);
    }

    @GetMapping(value = "/data/{blockType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataEnvelope> getData(@PathVariable(name="blockType") String blockType) {
        log.info("Block type request received: {}", blockType);

        //TODO add validation on enum
        return ResponseEntity.ok().body(server.getDataEnvelopes(BlockTypeEnum.valueOf(blockType)));

    }

    @PatchMapping(value = "/update/{blockName}/{newBlockType}",  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> updateData(@Valid @PathVariable(name="blockName") String blockName,@PathVariable(name="newBlockType") String newBlockType) throws IllegalArgumentException {
        log.info("Block type update request received newBlockType: {} for blockName {}", newBlockType,blockName);

        boolean response = false;

        //TODO add better validation and test case
        if(StringUtils.isEmpty(blockName)){
            throw new IllegalArgumentException("Block name not found");
        }

        //TODO add validation on enum
        response = server.updateBlockTypeOnBlockName(blockName,BlockTypeEnum.valueOf(newBlockType));

        return ResponseEntity.ok(response);

    }

    @Async
    //TODO review this code
    public CompletableFuture<HttpStatus> pushToDataLake(String dataEnvelope) {
        log.info("Push to data lake for data :  {}" , dataEnvelope);
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpStatus httpStatus = restTemplate.postForObject(URI_PUSHDATA,dataEnvelope, HttpStatus.class);
            return CompletableFuture.completedFuture(httpStatus);
        }catch (Exception e){
            log.warn("issue in the data lake {} : " , e.getMessage());
        }
        return null;
    }
}
