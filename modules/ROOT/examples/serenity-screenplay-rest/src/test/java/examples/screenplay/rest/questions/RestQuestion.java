package examples.screenplay.rest.questions;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.annotations.Subject;
import net.serenitybdd.screenplay.rest.interactions.Get;

import java.util.function.Function;

/**
 * A REST query that sends a GET request to an endpoint and returns a result of a given type.
 * Sample usage:
 * public static Question<Float> cashBalanceFor(Client client) {
 *         return new RestQuestionBuilder<Float>().about("Cash account balance")
 *                                                .to("/client/{clientId}/portfolio")
 *                                                .withParameters(request -> request.pathParam("clientId", client.getId()))
 *                                                .returning(response -> response.path("cash"));
 * }
 */
@Subject("#name")
public class RestQuestion<T> implements Question<T> {

    private final Function<RequestSpecification,RequestSpecification> query;
    private final String endpoint;
    private final String name;
    private final Function<Response, T> result;

    public RestQuestion(String name,
                        String endpoint,
                        Function<RequestSpecification,RequestSpecification> query,
                        Function<Response, T> result) {
        this.name = name;
        this.endpoint = endpoint;
        this.query = query;
        this.result = result;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public T answeredBy(Actor actor) {

        actor.attemptsTo(
                Get.resource(endpoint).with(query)
        );

        ensureValidResponsFor(SerenityRest.lastResponse().statusCode());

        return result.apply(SerenityRest.lastResponse());
    }

    private void ensureValidResponsFor(int statusCode) {
        if (statusCode < 200 || statusCode >= 300) {
            throw new UnexpectedRestResponseException("Unexpected response received from endpoint " + endpoint + ":"
                                                      + System.lineSeparator()
                                                      + SerenityRest.lastResponse().getBody().prettyPrint());
        }
    }
}
