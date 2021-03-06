package org.eclipse.ice.dev.jsonschemaconverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JsonSchemaConverterTest {
	
	public static final String JSON_FILE = "TestJson.json";
	
	public static final String JSON_RESULT = "TestJson_result.json";
	
	public static final String GENERATED_INTERFACE = "TestJsonFields.java";
	
	public static final String GENERATED_IMPLEMENTATION = "TestJsonFieldsImplementation.java";
	
	public static final String GENERATED_PROP_INTERFACE = "Properties.java";
	
	public static final String GENERATED_PROP_IMPLEMENTATION = "PropertiesImplementation.java";
	
	public static String jsonResource;
	
	public static Path destination;
	
	@BeforeAll
	public static void setup() {
		jsonResource = JsonSchemaConverterTest.class.getClassLoader().getResource(JSON_FILE).getPath();
		destination = Path.of(jsonResource);
	}
	
	@Test
	void schemaConversionTest() throws FileNotFoundException, IOException {
		 
		InputStream jsonFile = new FileInputStream(jsonResource);
		JsonSchemaConverter.setWriteFile(true);
		JsonSchemaConverter.handleInputJson(jsonFile, destination);
		assertTrue(Files.exists(destination.getParent().resolve(JSON_RESULT)));
		assertTrue(Files.exists(destination.getParent().resolve(GENERATED_INTERFACE)));
		assertTrue(Files.exists(destination.getParent().resolve(GENERATED_IMPLEMENTATION)));
		assertTrue(Files.exists(destination.getParent().resolve(GENERATED_PROP_INTERFACE)));
		assertTrue(Files.exists(destination.getParent().resolve(GENERATED_PROP_IMPLEMENTATION)));
		teardown();
	}
	
	@Test
	void badFileNameTest() throws InvalidFileNameException {
		
		assertThrows(InvalidFileNameException.class, () -> {
			JsonSchemaConverter.formatFileName("Test#.Json.json");
		});
		
		assertThrows(InvalidFileNameException.class, () -> {
			JsonSchemaConverter.formatFileName("...............");
		});
		
		assertThrows(InvalidFileNameException.class, () -> {
			JsonSchemaConverter.formatFileName(",IsThisValid.json,");
		});
		
		
		assertThrows(InvalidFileNameException.class, () -> {
			JsonSchemaConverter.formatFileName("");
		});
		
		assertEquals(JsonSchemaConverter.formatFileName("json.json.json.json"), "jsonjsonjson");
		
		assertEquals(JsonSchemaConverter.formatFileName("valid.json"), "valid");
		
		assertEquals(JsonSchemaConverter.formatFileName("a.json"), "a");
		
		assertEquals(JsonSchemaConverter.formatFileName("a"), "a");
	}
	
	
	public static void teardown() throws IOException {
		Files.delete(destination.getParent().resolve(JSON_RESULT));
		Files.delete(destination.getParent().resolve(GENERATED_INTERFACE));
		Files.delete(destination.getParent().resolve(GENERATED_IMPLEMENTATION));
		Files.delete(destination.getParent().resolve(GENERATED_PROP_INTERFACE));
		Files.delete(destination.getParent().resolve(GENERATED_PROP_IMPLEMENTATION));
	}

}
