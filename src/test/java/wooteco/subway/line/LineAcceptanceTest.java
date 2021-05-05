package wooteco.subway.line;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.AcceptanceTest;
import static org.hamcrest.core.Is.is;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private final LineDao lineDao = LineDaoFactory.makeLineDao();

    @BeforeEach
    void beforeSetUp() {
        lineDao.deleteAll();
    }

    @DisplayName("지하철 노선 한개가 저장된다.")
    @Test
    void create() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "bg-green-600");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    @DisplayName("기존에 존재하는 노선 이름으로 노선을 생성한다.")
    @Test
    void createLineWithDuplicateName() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "2호선");
        params.put("color", "bg-green-600");

        RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철역 목록을 조회한다.")
    @Test
    void getStations() {
        /// given
        Map<String, String> params1 = new HashMap<>();
        params1.put("name", "2호선");
        params1.put("color", "bg-green-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(params1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        Map<String, String> params2 = new HashMap<>();
        params2.put("name", "3호선");
        params2.put("color", "bg-orange-600");
        ExtractableResponse<Response> createResponse2 = RestAssured.given().log().all()
            .body(params2)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/lines")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Stream.of(createResponse1, createResponse2)
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getId)
            .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("id를 이용하여 지하철역을 조회한다.")
    @Test
    public void getStation() {
        /// given
        Map<String, String> params1 = new HashMap<>();
        params1.put("name", "2호선");
        params1.put("color", "bg-green-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(params1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        Map<String, String> params2 = new HashMap<>();
        params2.put("name", "3호선");
        params2.put("color", "bg-orange-600");
        ExtractableResponse<Response> createResponse2 = RestAssured.given().log().all()
            .body(params2)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/lines/1")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("id", is(1))
            .body("name", is("2호선"))
            .body("color", is("bg-green-600"))
            .extract();
    }
}
/*

 RestAssured.given().log().all()
     .accept(MediaType.APPLICATION_JSON_VALUE)
     .when().get("/room/1/statistics")
     .then().log().all()
     .statusCode(HttpStatus.OK.value())
     .contentType(MediaType.APPLICATION_JSON_VALUE)
     .body("whiteName", is("fortune"))
     .body("whiteWin", is("0"))
     .body("whiteLose", is("0"))
     .body("blackName", is("portune"))
     .body("blackWin", is("0"))
     .body("blackLose", is("0"));


 */
