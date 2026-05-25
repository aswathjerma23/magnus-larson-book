package se.magnus.api.core.review;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ReviewService {
    @GetMapping(value = "/review")
    List<Review> getReviews(@PathVariable(value = "productId",required = true) int productId);
}
