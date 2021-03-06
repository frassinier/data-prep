// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.dataset.DataSetLifecycle;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.schema.FormatFamily;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.user.store.UserDataRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;

/**
 * Base class for DataSet integration tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public abstract class DataSetBaseTest {

    protected static final String T_SHIRT_100_CSV_EXPECTED_JSON = "../t-shirt_100.csv.expected.json";

    protected static final String T_SHIRT_100_CSV = "../t-shirt_100.csv";

    protected static final String US_STATES_TO_CLEAN_CSV = "../us_states_to_clean.csv";

    protected static final String TAGADA2_CSV = "../tagada2.csv";

    protected static final String TAGADA_CSV = "../tagada.csv";

    protected static final String EMPTY_LINES2_JSON = "../empty_lines2.json";

    protected static final String EMPTY_LINES2_CSV = "../empty_lines2.csv";

    protected static final String METADATA_JSON = "../metadata.json";

    protected static final String DATASET_WITH_NUL_CHAR_CSV = "../dataset_with_NUL_char.csv";

    /** This class" logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${local.server.port}")
    public int port;

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    protected UserDataRepository userDataRepository;

    @Autowired
    protected JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("ContentStore#local")
    protected DataSetContentStore contentStore;

    @Autowired
    protected FormatFamily.Factory factory;

    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    /** DataPrep jackson ready to use builder. */
    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected Security security;

    @Autowired
    protected VersionService versionService;

    protected void assertQueueMessages(String dataSetId) throws Exception {
        // Asserts on metadata status
        DataSetMetadata metadata = dataSetMetadataRepository.getForContent(dataSetId);
        assertNotNull(metadata);
        DataSetLifecycle lifecycle = metadata.getLifecycle();
        assertThat(lifecycle.contentIndexed(), is(true));
        assertThat(lifecycle.schemaAnalyzed(), is(true));
        assertThat(lifecycle.qualityAnalyzed(), is(true));
    }

    @Before
    public void setUp() throws IOException {
        RestAssured.port = port;
    }

    @After
    public void tearDown() throws IOException {
        dataSetMetadataRepository.clear();
        contentStore.clear();
        userDataRepository.clear();
    }

    protected long getNumberOfRecords(String json) throws IOException {
        JsonNode rootNode = mapper.readTree(json);
        JsonNode records = rootNode.get("records");
        return records.size();
    }

    protected String requestDataSetSample(String dataSetId, boolean withMetadata, String sampleSize) {
        return given() //
                .expect() //
                .statusCode(200) //
                .when() //
                .get("/datasets/{id}/content?metadata={withMetadata}&sample={sampleSize}", dataSetId, withMetadata, sampleSize) //
                .asString();

    }

    protected String createCSVDataSet(InputStream content, String name) throws Exception {
        String dataSetId = given() //
                .body(IOUtils.toString(content)) //
                .queryParam("Content-Type", "text/csv") //
                .queryParam("name", name) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/datasets") //
                .asString();
        assertQueueMessages(dataSetId);
        return dataSetId;
    }
}
