package se.magnus.product_composite_service.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;

public class ProductCompositeIntegration implements ProductService,RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public ProductCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        @Value("${app.product-service.host}") String productServiceHost,
        @Value("${app.product-service.port}") String productServicePort,
        @Value("${app.recommendation-service.host}") String recommendationiServiceHost,
        @Value("${app.recommendation-service.port}") String recommendationiServicePort,
        @Value("${review-service.host}") String reviewServiceHost,
        @Value("${review-service.host}") String reviewServicePort
    ){
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        productServiceUrl = "http://"+ productServiceHost+ ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://"+ recommendationiServiceHost+ ":" + recommendationiServicePort + "/recommendation?productId=";
        reviewServiceUrl = "http://"+ reviewServiceHost+ ":" + reviewServicePort + "/review?productId=";
    }

    public Product geProduct(int productId){
        try {
            String url = productServiceUrl + productId;
            LOG.debug("will call getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class );
            LOG.debug("Found product with id : {}", product.getProductId());
            return product;
        } catch (HttpClientErrorException ex) {

      switch (HttpStatus.resolve(ex.getStatusCode().value())) {
        case NOT_FOUND:
          throw new NotFoundException(getErrorMessage(ex));

        case UNPROCESSABLE_ENTITY:
          throw new InvalidInputException(getErrorMessage(ex));

        default:
          LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
          LOG.warn("Error body: {}", ex.getResponseBodyAsString());
          throw ex;
      }
    }

    private String getErrorMessage(HttpClientErrorException ex){
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(),HttpErrorIn null)
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
