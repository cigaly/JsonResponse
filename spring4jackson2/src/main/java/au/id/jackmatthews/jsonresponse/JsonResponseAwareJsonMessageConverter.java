package au.id.jackmatthews.jsonresponse;

import java.io.IOException;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import au.id.jackmatthews.jsonresponse.JsonMixin;
import au.id.jackmatthews.jsonresponse.JsonResponse;
import au.id.jackmatthews.jsonresponse.ResponseWrapper;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adds support for {@link JsonResponse} annotation
 * 
 * @author Jack Matthews
 * 
 */
final class JsonResponseAwareJsonMessageConverter extends MappingJackson2HttpMessageConverter {

    public JsonResponseAwareJsonMessageConverter() {
        super();
        ObjectMapper defaultMapper = new ObjectMapper();
        setObjectMapper(defaultMapper);
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        if (object instanceof ResponseWrapper) {
            writeJson((ResponseWrapper) object, outputMessage);
        } else {
            super.writeInternal(object, outputMessage);
        }
    }

    protected void writeJson(ResponseWrapper response, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {

        JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());

        ObjectMapper mapper = new ObjectMapper();

        // Add support for jackson mixins
        JsonMixin[] jsonMixins = response.getJsonResponse().mixins();
        for (JsonMixin jsonMixin : jsonMixins) {
            mapper.addMixIn(jsonMixin.target(), jsonMixin.mixin());
        }

        JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
        try {
            mapper.writeValue(jsonGenerator, response.getOriginalResponse());
        } catch (IOException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

}
