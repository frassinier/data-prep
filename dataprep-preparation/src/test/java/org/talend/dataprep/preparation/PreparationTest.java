package org.talend.dataprep.preparation;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class PreparationTest {

    @Autowired
    private PreparationRepository repository;

    @Value("${local.server.port}")
    public int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
    }

    @Test
    public void CORSHeaders() throws Exception {
        when().get("/preparations").then().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
                .header("Access-Control-Max-Age", "3600").header("Access-Control-Allow-Headers", "x-requested-with, Content-Type");
    }

    @Test
    public void list() throws Exception {
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));
        repository.add(new Preparation("1234"));
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(equalTo("[\"7110eda4d09e062aa5e4a390b0a572ac0d2c0220\"]"));
        repository.add(new Preparation("5678"));
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(equalTo("[\"7110eda4d09e062aa5e4a390b0a572ac0d2c0220\",\"2abd55e001c524cb2cf6300a89ca6366848a77d5\"]"));

    }
}
