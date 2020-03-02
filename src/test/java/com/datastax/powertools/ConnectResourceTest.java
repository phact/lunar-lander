package com.datastax.powertools;

import com.datastax.powertools.cassandra.CassandraClusterConfiguration;
import com.datastax.powertools.docker.DockerHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

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

        CassandraClusterConfiguration cassandraClusterConf = new CassandraClusterConfiguration();
        cassandraClusterConf.setContactPoints("localhost");
        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(cassandraClusterConf)
                .when().post("/connect");

        response.then().log().all().statusCode(is(200));

    }

    @AfterAll
    static void tearDown() {
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