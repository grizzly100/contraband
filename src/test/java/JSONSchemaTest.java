import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

class JSONSchemaTest {

    @Test
    public void givenInvalidInput_whenValidating_thenInvalid() throws ValidationException {
        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(Library.class.getResourceAsStream("/schema/recordingSchema.json")));
        JSONObject jsonSubject = new JSONObject(
                new JSONTokener(JSONSchemaTest.class.getResourceAsStream("/examples/info.json")));

        Schema schema=null;
        try {
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(jsonSchema)
                    .draftV7Support()
                    .build();
            schema = loader.load().build();

            //schema = SchemaLoader.load(jsonSchema);
            System.out.println(schema.getDescription());
            schema.validate(jsonSubject);
        } catch(SchemaException ex)
        {
            System.out.println(ex.fillInStackTrace());
        }
        catch(ValidationException ex)
        {
            System.out.println(ex.getAllMessages());
        }

    }
}