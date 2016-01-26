package org.talend.dataprep.schema;

import java.io.InputStream;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AbstractSchemaTestUtils.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public abstract class AbstractSchemaTestUtils {

    @Autowired
    protected IoTestUtils ioTestUtils;

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /**
     * Return the SchemaParser.Request for the given parameters.
     *
     * @param content the dataset con.ent.
     * @param dataSetId the dataset id.
     * @return the SchemaParser.Request for the given parameters.
     */
    protected SchemaParser.Request getRequest(InputStream content, String dataSetId) {
        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id(dataSetId).build();
        return new SchemaParser.Request(content, dataSetMetadata);
    }

}
