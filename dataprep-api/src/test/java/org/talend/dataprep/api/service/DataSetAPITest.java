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

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.api.dataset.DataSetGovernance;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * Unit test for Data Set API.
 */
public class DataSetAPITest extends ApiServiceTestBase {

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void cleanupFolder() throws Exception {
        folderRepository.clear();
    }

    @Test
    public void testDataSetUpdate() throws Exception {
        // given a created dataset
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        // when it's updated
        given().body(IOUtils.toString(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/api/datasets/" + dataSetId + "?name=testDataset").asString();

        // then, the content is updated
        String dataSetContent = when().get("/api/datasets/" + dataSetId + "?metadata=true").asString();
        final String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("t-shirt_100.csv.expected.json"));
        assertThat(dataSetContent, sameJSONAs(expectedContent).allowingExtraUnexpectedFields());
    }

    @Test
    public void test_TDP_2052() throws Exception {
        // given a created dataset
        final String datasetOriginalName = "testDataset";
        final String dataSetId = createDataset("dataset/dataset.csv", datasetOriginalName, "text/csv");

        // when it's updated
        given().body(IOUtils.toString(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/api/datasets/" + dataSetId).asString();

        // then, the content is updated
        String dataSetContent = when().get("/api/datasets/" + dataSetId + "?metadata=true").asString();
        final String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("t-shirt_100.csv.expected.json"));
        assertThat(dataSetContent, sameJSONAs(expectedContent).allowingExtraUnexpectedFields());

        final String jsonUpdatedMetadata = when().get("/api/datasets/{id}/metadata", dataSetId).asString();
        final DataSetMetadata updatedMetadata = mapper.readValue(jsonUpdatedMetadata, DataSetMetadata.class);
        assertEquals(datasetOriginalName, updatedMetadata.getName());
    }

    @Test
    public void testDataSetUpdateMetadata() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");

        // when
        final String jsonOriginalMetadata = when().get("/api/datasets/{id}/metadata", dataSetId).asString();
        final DataSetMetadata metadata = mapper.readValue(jsonOriginalMetadata, DataSetMetadata.class);
        metadata.setName("Toto");
        final String jsonMetadata = mapper.writeValueAsString(metadata);

        given().body(jsonMetadata).when().put("/api/datasets/{id}/metadata", dataSetId).asString();

        final String jsonUpdatedMetadata = when().get("/api/datasets/{id}/metadata", dataSetId).asString();
        final DataSetMetadata updatedMetadata = mapper.readValue(jsonUpdatedMetadata, DataSetMetadata.class);

        // then
        assertEquals(updatedMetadata, metadata);
    }

    @Test
    public void testDataSetList() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        // when
        final String list = when().get("/api/datasets").asString();

        // then
        assertTrue(list.contains(dataSetId));
    }

    @Test
    public void testListCompatibleDataSets() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "compatible1", "text/csv");
        final String dataSetId2 = createDataset("dataset/dataset.csv", "compatible2", "text/csv");
        final String dataSetId3 = createDataset("t-shirt_100.csv", "incompatible", "text/csv");

        // when
        final String compatibleDatasetList = when().get("/api/datasets/{id}/compatibledatasets", dataSetId).asString();

        // then
        assertTrue(compatibleDatasetList.contains(dataSetId2));
        assertFalse(compatibleDatasetList.contains(dataSetId3));
    }

    @Test
    public void testListCompatiblePreparationsWhenNothingIsCompatible() throws Exception {
        //
        final String dataSetId = createDataset("dataset/dataset.csv", "compatible1", "text/csv");
        createDataset("dataset/dataset.csv", "compatible2", "text/csv");
        createDataset("t-shirt_100.csv", "incompatible", "text/csv");

        final String getResult = when().get("/api/datasets/{id}/compatiblepreparations", dataSetId).asString();
        final List compatiblePreparations = mapper.readerFor(List.class).readValue(getResult);

        // then
        assertTrue(compatiblePreparations.isEmpty());
    }

    @Test
    public void testListCompatiblePreparationsWhenTwoPreparationsAreCompatible() throws Exception {
        //
        final String dataSetId = createDataset("dataset/dataset.csv", "compatible1", "text/csv");
        final String dataSetId2 = createDataset("dataset/dataset.csv", "compatible2", "text/csv");
        createDataset("t-shirt_100.csv", "incompatible", "text/csv");

        final String prep1 = createPreparationFromDataset(dataSetId, "prep1");
        final String prep2 = createPreparationFromDataset(dataSetId2, "prep2");

        final String getResult = when().get("/api/datasets/{id}/compatiblepreparations", dataSetId).asString();
        final List<Preparation> compatiblePreparations = mapper.readerFor(new TypeReference<Collection<Preparation>>() {
        }).readValue(getResult);

        // then
        assertTrue(compatiblePreparations.size() == 2);
        assertTrue(prep2.equals(compatiblePreparations.get(0).getId()));
        assertTrue(prep1.equals(compatiblePreparations.get(1).getId()));
    }

    @Test
    public void testListCompatibleDataSetsWhenUniqueDatasetInRepository() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "unique", "text/csv");

        // when
        final String compatibleDatasetList = when().get("/api/datasets/{id}/compatibledatasets", dataSetId).asString();

        // then
        assertFalse(compatibleDatasetList.contains(dataSetId));
    }

    @Test
    public void testDataSetListWithDateOrder() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        // when (sort by date, order is desc)
        String list = when().get("/api/datasets?sort={sort}&order={order}", "date", "desc").asString();

        // then
        Iterator<JsonNode> elements = mapper.readTree(list).elements();
        String[] expectedNames = new String[] { dataSetId2, dataSetId1 };
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }

        // when (sort by date, order is desc)
        list = when().get("/api/datasets?sort={sort}&order={order}", "date", "asc").asString();

        // then
        elements = mapper.readTree(list).elements();
        expectedNames = new String[] { dataSetId1, dataSetId2 };
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void testDataSetListWithNameOrder() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        // when (sort by date, order is desc)
        String list = when().get("/api/datasets?sort={sort}&order={order}", "name", "desc").asString();

        // then
        Iterator<JsonNode> elements = mapper.readTree(list).elements();
        String[] expectedNames = new String[] { dataSetId2, dataSetId1 };
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }

        // when (sort by date, order is desc)
        list = when().get("/api/datasets?sort={sort}&order={order}", "date", "asc").asString();

        // then
        elements = mapper.readTree(list).elements();
        expectedNames = new String[] { dataSetId1, dataSetId2 };
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }
    }

    /**
     * Simple dataset deletion case.
     */
    @Test
    public void testDataSetDelete() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        final String list = when().get("/api/datasets").asString();
        assertTrue(list.contains(dataSetId));

        // when
        when().delete("/api/datasets/" + dataSetId).asString();
        final String updatedList = when().get("/api/datasets").asString();

        // then
        assertEquals("[]", updatedList);
    }

    /**
     * DataSet deletion test case when the dataset is used by a preparation.
     */
    @Test
    public void testDataSetDeleteWhenUsedByPreparation() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        createPreparationFromDataset(dataSetId, "testPreparation");

        // when/then
        final Response response = when().delete("/api/datasets/" + dataSetId);

        // then
        final int statusCode = response.statusCode();
        assertThat(statusCode, is(409));

        final String responseAsString = response.asString();
        final JsonPath json = from(responseAsString);
        assertThat(json.get("code"), is("TDP_API_DATASET_STILL_IN_USE"));
    }

    @Test
    public void testDataSetCreate() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_metadata.json");

        // when
        final String contentAsString = when().get("/api/datasets/{id}?metadata=true&columns=false", dataSetId).asString();

        // then
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void shouldCopyDataset() throws Exception {
        // given
        final String originalId = createDataset("dataset/dataset.csv", "original", "text/csv");

        // when
        final Response response = given().param("name", "copy") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/api/datasets/{id}/copy", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
        String copyId = response.asString();
        assertNotNull(dataSetMetadataRepository.get(copyId));
    }

    @Test
    public void copyDataSetClashShouldForwardException() throws Exception {
        // given
        final String originalId = createDataset("dataset/dataset.csv", "taken", "text/csv");

        // when
        final Response response = given().param("name", "taken") //
                .when() //
                .expect().statusCode(409).log().ifError() //
                .post("/api/datasets/{id}/copy", originalId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void testDataSetGetMetadata() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "test_metadata", "text/csv");

        // when
        final String content = when().get("/api/datasets/{id}/metadata", dataSetId).asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_columns.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetCreateWithSpace() throws Exception {
        // given
        String dataSetId = createDataset("dataset/dataset.csv", "Test with spaces", "text/csv");

        // when
        final DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);

        // then
        assertNotNull(metadata);
        assertEquals("Test with spaces", metadata.getName());
    }

    @Test
    public void testDataSetColumnSuggestions() throws Exception {
        // given
        final String columnDescription = IOUtils
                .toString(PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_metadata.json"));

        // when
        final String content = given().body(columnDescription).when().post("/api/transform/suggest/column").asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("suggestions/expected_all_line_scope_actions.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetColumnActions() throws Exception {
        // given
        final String columnDescription = IOUtils
                .toString(PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_metadata.json"));

        // when
        final String content = given().body(columnDescription).when().post("/api/transform/actions/column").asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_actions.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetLineActions() throws Exception {
        // when
        final String content = given().when().get("/api/transform/actions/line").asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("suggestions/all_line_scope_actions.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetActions() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        // when
        final String contentAsString = when().get("/api/datasets/{id}/actions", dataSetId).asString();

        // then
        assertThat(contentAsString, sameJSONAs("[]"));
    }

    @Test
    public void testLookupActionsActions() throws Exception {
        // given
        final String firstDataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        final String dataSetId = createDataset("dataset/dataset_cars.csv", "cars", "text/csv");
        final String thirdDataSetId = createDataset("dataset/dataset.csv", "third", "text/csv");

        List<String> expectedIds = Arrays.asList(firstDataSetId, thirdDataSetId);

        // when
        final String actions = when().get("/api/datasets/{id}/actions", dataSetId).asString();

        // then
        final JsonNode jsonNode = mapper.readTree(actions);
        // response is an array
        assertTrue("json not an array:" + actions, jsonNode.isArray());
        Assertions.assertThat(jsonNode.isArray()).isTrue();
        // an array of 2 entries
        ArrayNode lookups = (ArrayNode) jsonNode;
        assertThat(lookups.size(), is(2));

        // let's check the url of the possible lookups
        for (int i = 0; i < lookups.size(); i++) {
            final JsonNode lookup = lookups.get(i);
            final ArrayNode parameters = (ArrayNode) lookup.get("parameters");
            for (int j = 0; j < parameters.size(); j++) {
                final JsonNode parameter = parameters.get(j);
                if (StringUtils.equals(parameter.get("name").asText(), "url")) {
                    final String url = parameter.get("default").asText();
                    // the url id must be known
                    assertThat(expectedIds.stream().filter(url::contains).count(), is(1L));
                }
            }
        }
    }

    @Test
    public void testAskCertification() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        // when
        when().put("/api/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());

        // then
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.PENDING, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRowMetadata().getColumns(), not(empty()));

        // when
        when().put("/api/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());

        // then
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.CERTIFIED, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRowMetadata().getColumns(), not(empty()));
    }

    @Test
    public void testDataSetCreateUnsupportedFormat() throws Exception {
        // given
        final String datasetContent = IOUtils.toString(DataSetAPITest.class.getResourceAsStream("dataset/dataset.ods"));
        final int metadataCount = dataSetMetadataRepository.size();
        // then
        final Response response = given().body(datasetContent).when().post("/api/datasets");
        assertThat(response.getStatusCode(), is(400));
        JsonErrorCode code = mapper.readValue(response.asString(), JsonErrorCode.class);
        assertThat(code.getCode(), is(DataSetErrorCodes.UNSUPPORTED_CONTENT.getCode()));
        assertThat(dataSetMetadataRepository.size(), is(metadataCount)); // No data set metadata should be created
    }

    @Test
    public void preview_xls_multi_sheet() throws Exception {

        // then
        Response response = given() //
                .body(IOUtils
                        .toByteArray(DataSetAPITest.class.getResourceAsStream("dataset/Talend_Desk-Tableau_de_Bord-011214.xls"))) //
                .when().post("/api/datasets");

        assertThat(response.getStatusCode(), is(200));
        String datasetId = response.asString();
        // call preview to ensure no error
        response = given().when().get("/api/datasets/preview/{id}", datasetId);

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void should_list_encodings() throws Exception {

        // then
        String json = given() //
                .expect().statusCode(200).log().ifError() //
                .when().get("/api/datasets/encodings").asString();

        List<String> encodings = mapper.readValue(json, new TypeReference<List<String>>() {
        });

        assertThat(encodings.isEmpty(), is(false));
        assertThat(encodings.get(0), is("UTF-8"));
        assertThat(encodings.get(1), is("UTF-16"));
    }

    @Test
    public void should_list_filtered_datasets_properly() throws Exception {
        // create data sets
        final String dataSetId1 = createDataset("dataset/dataset.csv", "dataset1", "text/csv");
        final String dataSetId2 = createDataset("dataset/dataset.csv", "dataset2", "text/csv");
        final String dataSetId3 = createDataset("dataset/dataset.csv", "dataset3", "text/csv");
        createDataset("dataset/dataset.csv", "dataset4", "text/csv");

        // Make dataset1 more recent
        final DataSetMetadata dataSetMetadata1 = dataSetMetadataRepository.get(dataSetId1);
        dataSetMetadata1.setFavorite(true);
        dataSetMetadata1.getGovernance().setCertificationStep(DataSetGovernance.Certification.CERTIFIED);
        dataSetMetadata1.setLastModificationDate(Instant.now().getEpochSecond() + 1);
        dataSetMetadataRepository.add(dataSetMetadata1);
        final DataSetMetadata dataSetMetadata2 = dataSetMetadataRepository.get(dataSetId2);
        dataSetMetadata2.setFavorite(true);
        dataSetMetadataRepository.add(dataSetMetadata2);
        final DataSetMetadata dataSetMetadata3 = dataSetMetadataRepository.get(dataSetId3);
        dataSetMetadata3.getGovernance().setCertificationStep(DataSetGovernance.Certification.CERTIFIED);
        dataSetMetadataRepository.add(dataSetMetadata3);

        // @formatter:off
        // certified, favorite and recent
        given()
            .queryParam("favorite", "true")
            .queryParam("certified", "true")
            .queryParam("limit", "true")
            .queryParam("name", "dataset")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasItem("dataset1"))
            .body("name", hasSize(1));

        // certified, favorite and recent
        given()
            .queryParam("favorite", "true")
            .queryParam("certified", "true")
            .queryParam("limit", "true")
            .queryParam("name", "2")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasSize(0));

        // only names
        given()
            .queryParam("name", "ATASET2")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
                .body("name", hasItem("dataset2"))
                .body("name", hasSize(1));

        // only favorites
        given()
            .queryParam("favorite", "true")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasItems("dataset1", "dataset2"))
            .body("name", hasSize(2));

        // only certified
        given()
            .queryParam("certified", "true")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasItems("dataset1", "dataset3"))
            .body("name", hasSize(2));

        // only recent
        given()
            .queryParam("limit", "true")
        .when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasItems("dataset2", "dataset3", "dataset4"))
            .body("name", hasSize(3));

        // all
        when()
            .get("/api/datasets")
        .then()
            .statusCode(200)
            .body("name", hasItems("dataset1", "dataset2", "dataset3", "dataset4"))
            .body("name", hasSize(4));

        // @formatter:on
    }

}
