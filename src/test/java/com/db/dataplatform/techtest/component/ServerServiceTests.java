package com.db.dataplatform.techtest.component;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.mapper.ServerMapperConfiguration;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.component.impl.ServerImpl;
import com.db.dataplatform.techtest.server.service.DataHeaderService;
import jdk.nashorn.internal.runtime.options.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.createTestDataEnvelopeApiObject;
import static com.db.dataplatform.techtest.TestDataHelper.createTestDataEnvelopeApiObjectWithWrongMD5Sum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ServerServiceTests {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    @Mock
    private DataHeaderService dataHeaderServiceImplMock;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataEnvelope testDataEnvelope;

    private Server server;

    @Before
    public void setup() {
        ServerMapperConfiguration serverMapperConfiguration = new ServerMapperConfiguration();
        modelMapper = serverMapperConfiguration.createModelMapperBean();

        testDataEnvelope = createTestDataEnvelopeApiObject();
        expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        server = new ServerImpl(dataBodyServiceImplMock, dataHeaderServiceImplMock,modelMapper);

        Optional<DataBodyEntity> dataBodyEntity = Optional.of(expectedDataBodyEntity);

        when(dataBodyServiceImplMock.getDataByBlockName(any(String.class))).thenReturn(dataBodyEntity);
        when(dataBodyServiceImplMock.getDataByBlockType(any(BlockTypeEnum.class))).thenReturn(Arrays.asList(expectedDataBodyEntity));
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected() throws NoSuchAlgorithmException, IOException {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldNotSaveDataEnvelopeMD5MismatchAsExpected() throws NoSuchAlgorithmException, IOException {
        boolean success = server.saveDataEnvelope(createTestDataEnvelopeApiObjectWithWrongMD5Sum());

        assertThat(success).isFalse();
        verify(dataBodyServiceImplMock, times(0)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldGetDataEnvelopeAsExpected() throws NoSuchAlgorithmException, IOException {
        List<DataEnvelope> actualDataEnvelope = server.getDataEnvelopes(BlockTypeEnum.BLOCKTYPEA);

        verify(dataBodyServiceImplMock, times(1)).getDataByBlockType(eq(BlockTypeEnum.BLOCKTYPEA));
    }

    @Test
    public void shouldUpdateBlockTypeAsExpected() throws NoSuchAlgorithmException, IOException {
        boolean success = server.updateBlockTypeOnBlockName("TEST_NAME" , BlockTypeEnum.BLOCKTYPEB);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).getDataByBlockName(eq("TEST_NAME"));
        verify(dataHeaderServiceImplMock, times(1)).saveHeader(expectedDataBodyEntity.getDataHeaderEntity());
    }


}
