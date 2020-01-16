package com.datastax.powertools;

import com.datastax.powertools.docker.DockerHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ConnectResourceTest {

    static DockerHelper dh = new DockerHelper();

    @BeforeAll
    static void setUp() {
        String imgTag = "cassandra";
        String dockerfile = "docker/cassandra/Dockerfile";
        dh.buildDockerfile(dockerfile, imgTag);
        dh.startContainer(imgTag);
    }

    @AfterEach
    void tearDown() {
        dh.stopContainer();
        dh.rmContainer();
    }

    @Test
    public void testGetMissionNames() {
        Response response = given()
                .when().get("/missionNames");

        List<String> missionNames = Arrays.asList(response.getBody().as(String[].class));

        response.then().statusCode(is(200));

        for (String missionName : missionNames) {
           given().when().get("/initiateSequence/" +  missionName).then().statusCode(is(200));
        }
    }
}