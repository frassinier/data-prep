package org.talend.dataprep.transformation.api.transformer.type;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.transformer.input.TransformerConfiguration;
import org.talend.dataprep.transformation.exception.TransformationErrorCodes;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Columns array Serializer
 */
@Component
public class ColumnsTypeTransformer implements TypeTransformer {

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Override
    public void process(final TransformerConfiguration configuration) {
        final JsonParser parser = configuration.getParser();

        final List<Consumer<ColumnMetadata>> columnActions = configuration.getActions(ColumnMetadata.class);
        final Consumer<ColumnMetadata> actions = columnActions == null ? null : columnActions.get(0);

        try {
            final StringWriter content = new StringWriter();
            final JsonGenerator contentGenerator = new JsonFactory().createGenerator(content);

            JsonToken nextToken;
            int level = 0;
            while ((nextToken = parser.nextToken()) != null) {
                switch (nextToken) {
                case VALUE_EMBEDDED_OBJECT:
                    contentGenerator.writeRaw(parser.getText());
                    break;
                case NOT_AVAILABLE:
                    break;
                // Object delimiter
                case START_OBJECT:
                    contentGenerator.writeStartObject();
                    break;
                case END_OBJECT:
                    contentGenerator.writeEndObject();
                    break;
                // Fields key/value
                case FIELD_NAME:
                    contentGenerator.writeFieldName(parser.getText());
                    break;
                case VALUE_FALSE:
                    contentGenerator.writeBoolean(false);
                    break;
                case VALUE_TRUE:
                    contentGenerator.writeBoolean(true);
                    break;
                case VALUE_NUMBER_FLOAT:
                    contentGenerator.writeNumber(parser.getNumberValue().floatValue());
                    break;
                case VALUE_NUMBER_INT:
                    contentGenerator.writeNumber(parser.getNumberValue().intValue());
                    break;
                case VALUE_STRING:
                    contentGenerator.writeString(parser.getText());
                    break;
                // Array delimiter : on array end, we consider the column part ends
                case START_ARRAY:
                    level++;
                    contentGenerator.writeStartArray();
                    break;
                case END_ARRAY:
                    contentGenerator.writeEndArray();
                    level--;
                    if (level == 0) {
                        contentGenerator.flush();
                        final List<ColumnMetadata> columns = getColumnsMetadata(content);
                        transform(columns, actions);
                        configuration.getWriter().write(columns);
                        return;
                    }
                case VALUE_NULL:
                    break;
                }
            }
        } catch (JsonParseException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_WRITE_JSON, e);
        }
    }

    /**
     * Apply columns transformations
     *  @param columns - the columns list
     * @param action - transformation action
     */
    // TODO Temporary: actions may transform columns, for now just print them as is
    private void transform(final List<ColumnMetadata> columns, final Consumer<ColumnMetadata> action) {
    }

    /**
     * Convert String to list of ColumnMetadataObject
     *
     * @param content - the String writer that contains JSON format array
     * @throws IOException
     */
    private List<ColumnMetadata> getColumnsMetadata(final StringWriter content) throws IOException {
        final ObjectReader columnReader = builder.build().reader(ColumnMetadata.class);
        return columnReader.<ColumnMetadata> readValues(content.toString()).readAll();
    }
}
