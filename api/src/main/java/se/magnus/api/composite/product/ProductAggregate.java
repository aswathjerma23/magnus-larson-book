package se.magnus.api.composite.product;

import java.util.List;

public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int weight;
    private final List<ReviewSummary> reviews;
    private final List<RecommendationSummary> recommendations;
    private final ServiceAddresses serviceAddresses;

    public ProductAggregate(int productId,
        String name,
        int weight,
        List<ReviewSummary> reviews,
        List<RecommendationSummary> recommendations,
        ServiceAddresses serviceAddresses){
            this.productId = productId;
            this.name = name;
            this.weight = weight;
            this.reviews = reviews;
            this.recommendations = recommendations;
            this.serviceAddresses = serviceAddresses;
    }

    public int getProductId(){
        return this.productId;
    }
    public String getName(){
        return this.name;
    }
    public int getWeight(){
        return this.weight;
    }
    public List<ReviewSummary> getReviewSummaries(){
        return this.reviews;
    }
    public  List<RecommendationSummary> geRecommendationSummaries(){
        return this.recommendations;
    }
    public ServiceAddresses getServiceAddresses(){
        return this.serviceAddresses;
    }
}
