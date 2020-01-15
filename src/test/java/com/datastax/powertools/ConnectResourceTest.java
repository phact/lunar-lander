package com.datastax.powertools;

import com.datastax.powertools.docker.DockerHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @Test
    public void testGetMissionNames() {
        given()
                .when().get("/missionNames")
                .then()
                .statusCode(is(200));
    }
}