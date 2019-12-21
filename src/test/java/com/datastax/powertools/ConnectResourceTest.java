package com.datastax.powertools;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ConnectResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/getMissions")
          .then()
             .statusCode(is(200));
    }

}