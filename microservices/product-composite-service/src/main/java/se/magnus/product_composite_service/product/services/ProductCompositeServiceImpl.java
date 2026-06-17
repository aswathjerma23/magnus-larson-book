package se.magnus.product_composite_service.product.services;

import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.ProductCompositeService;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.composite.product.ServiceAddresses;
import util.http.ServiceUtil;
import se.magnus.api.core.product.Product;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService{
    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration productCompositeIntegration;
    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration productCompositeIntegration){
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Override
    public ProductAggregate getProduct(@PathVariable int productId){
        Product product = productCompositeIntegration.getProduct(productId);

        if(product == null){
            throw new NotFoundException("No product found for this productId" + productId);
        }
        List<Recommendation> recommendation = productCompositeIntegration.getRecommendations(productId);
        List<Review> reviews = productCompositeIntegration.getReviews(productId);
        return createProductAggregate(product, recommendation, reviews, serviceUtil.getServiceAddress());
    }

public ProductAggregate createProductAggregate(
        Product product,
        List<Recommendation> recommendations,
        List<Review> reviews,
        String serviceAddress
    ){
            // 1. Setup product info
    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();
    List<RecommendationSummary> recommendationSummaries = (recommendations == null)? null : recommendations.stream()
                        .map(r-> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                        .collect(Collectors.toList());
    List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                        .map(r-> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                        .collect(Collectors.toList());
     // 4. Create info regarding the involved microservices addresses
    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, reviewSummaries,recommendationSummaries, serviceAddresses);
    }

}
