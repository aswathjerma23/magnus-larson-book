package se.magnus.product_composite_service.product.services;

/*
 * ===========================
 * INTEGRATION LAYER
 * ===========================
 *
 * Purpose of this class:
 * - Acts as a REST CLIENT to other microservices
 * - Calls Product, Recommendation, and Review services
 * - Converts HTTP + JSON into clean Java objects
 * - Converts HTTP errors into domain exceptions
 *
 * IMPORTANT:
 * This class DOES NOT contain business logic.
 * It only handles communication with other services.
 */

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;

import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;

import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;

import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;

import util.http.HttpErrorInfo;

/*
 * This class IMPLEMENTS service interfaces,
 * but instead of accessing a database,
 * it CALLS OTHER MICROSERVICES using HTTP.
 */
@Component
public class ProductCompositeIntegration
        implements ProductService, RecommendationService, ReviewService {

    /*
     * Logger for debugging and production logs.
     * NEVER use System.out.println in production code.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(ProductCompositeIntegration.class);

    /*
     * RestTemplate:
     * - Blocking HTTP client
     * - Used to call other microservices
     */
    private final RestTemplate restTemplate;

    /*
     * ObjectMapper:
     * - Converts JSON <-> Java objects
     * - Used mainly to parse error responses
     */
    private final ObjectMapper objectMapper;

    /*
     * Base URLs of downstream services.
     * These are constructed once in the constructor
     * and reused in all methods.
     */
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    /*
     * Constructor
     *
     * Spring injects:
     * - RestTemplate
     * - ObjectMapper
     * - Configuration values from application.yml
     *
     * You DO NOT create these objects manually.
     */
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,

            // Read values from application.yml
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") String productServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") String recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") String reviewServicePort
    ) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        /*
         * Build base URLs for downstream services.
         * Example:
         * http://localhost:7001/product/
         */
        this.productServiceUrl =
                "http://" + productServiceHost + ":" + productServicePort + "/product/";

        this.recommendationServiceUrl =
                "http://" + recommendationServiceHost + ":" + recommendationServicePort
                        + "/recommendation?productId=";

        this.reviewServiceUrl =
                "http://" + reviewServiceHost + ":" + reviewServicePort
                        + "/review?productId=";
    }

    /*
     * ===========================
     * PRODUCT SERVICE CALL
     * ===========================
     *
     * Calls the Product microservice to fetch a single product.
     */
    
     public Product  getProduct (int productId) {

        try {
            // Build final URL
            String url = productServiceUrl + productId;

            LOG.debug("Will call getProduct API on URL: {}", url);

            /*
             * restTemplate.getForObject:
             * - Sends HTTP GET
             * - Receives JSON
             * - Converts JSON into Product object
             */
            Product product = restTemplate.getForObject(url, Product.class);

            LOG.debug("Found product with id: {}", product.getProductId());

            return product;

        } catch (HttpClientErrorException ex) {

            /*
             * Convert HTTP errors into domain-specific exceptions.
             * Controller layer understands domain exceptions,
             * NOT raw HTTP errors.
             */
            switch (HttpStatus.resolve(ex.getStatusCode().value())) {

                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));

                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));

                default:
                    LOG.warn("Unexpected HTTP error: {}", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    /*
     * Extracts meaningful error message from HTTP error response.
     *
     * Error body usually looks like:
     * {
     *   "message": "Product not found"
     * }
     */
    private String getErrorMessage(HttpClientErrorException ex) {

        try {
            // Convert JSON error body into HttpErrorInfo object
            return objectMapper
                    .readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)
                    .getMessage();

        } catch (Exception e) {
            // Fallback if JSON parsing fails
            return ex.getMessage();
        }
    }

    /*
     * ===========================
     * RECOMMENDATION SERVICE CALL
     * ===========================
     *
     * Returns a LIST of recommendations.
     * Uses ParameterizedTypeReference to handle Java generics.
     */
    @Override
    public List<Recommendation> getRecommendations(int productId) {

        try {
            String url = recommendationServiceUrl + productId;

            LOG.info("Will call getRecommendations API on URL: {}", url);

            /*
             * restTemplate.exchange is used instead of getForObject
             * because Java erases generic type information at runtime.
             *
             * ParameterizedTypeReference preserves:
             * List<Recommendation>
             */
            List<Recommendation> recommendations =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Recommendation>>() {}
                    ).getBody();

            LOG.info(
                    "Found {} recommendations for product id: {}",
                    recommendations.size(),
                    productId
            );

            return recommendations;

        } catch (Exception ex) {

            /*
             * Recommendations are OPTIONAL.
             * If this service fails, product page should still work.
             * This is graceful degradation.
             */
            LOG.warn(
                    "Exception while requesting recommendations, returning empty list: {}",
                    ex.getMessage()
            );

            return new ArrayList<>();
        }
    }

    /*
     * ===========================
     * REVIEW SERVICE CALL
     * ===========================
     *
     * Same pattern as recommendations.
     * Returns empty list if service fails.
     */
    @Override
    public List<Review> getReviews(int productId) {

        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);

            List<Review> reviews =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Review>>() {}
                    ).getBody();

            LOG.debug(
                    "Found {} reviews for product id: {}",
                    reviews.size(),
                    productId
            );

            return reviews;

        } catch (Exception ex) {

            LOG.warn(
                    "Exception while requesting reviews, returning empty list: {}",
                    ex.getMessage()
            );

            return new ArrayList<>();
        }
    }
}