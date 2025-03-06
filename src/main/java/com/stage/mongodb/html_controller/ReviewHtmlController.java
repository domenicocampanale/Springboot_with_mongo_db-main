package com.stage.mongodb.html_controller;

import com.stage.mongodb.dto.ReviewDto;
import com.stage.mongodb.dto.ReviewDtoInput;
import com.stage.mongodb.dto.ReviewDtoUpdate;
import com.stage.mongodb.repository.MovieRepository;
import com.stage.mongodb.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/view/review")
public class ReviewHtmlController {

    private final ReviewService reviewService;
    private final MovieRepository movieRepository;

    @GetMapping("/home")
    public String homeReviews() {
        log.info("Request for showing reviews home page");
        return "review_html/review_index";
    }

    @GetMapping("/list")
    public String viewReviews(Model model) {
        log.info("Request for showing review list view");
        List<ReviewDto> reviewsDto = reviewService.getReviews();
        model.addAttribute("reviews", reviewsDto);
        return "review_html/review_list";
    }

    @GetMapping("/details")
    public String viewReviewDetails(@RequestParam String id, Model model) {
        log.info("Request for showing review details view for id {}", id);

        try {
            ReviewDto reviewDto = reviewService.getReviewById(id);
            model.addAttribute("review", reviewDto);
            return "review_html/review_details";
        } catch (Exception e) {
            log.error("Review not found for id: {}", id);
            model.addAttribute("errorMessage", "Could not find review. Please try again.");
            return "review_html/id_not_found";
        }
    }

    @GetMapping("/add")
    public String showAddReviewForm(Model model) {
        ReviewDtoInput reviewDtoInput = ReviewDtoInput.builder()
                .rating(1)
                .comment("Default comment")
                .movieId("Default movie id")
                .build();

        model.addAttribute("reviewDtoInput", reviewDtoInput);
        return "review_html/add_review";
    }

    @PostMapping("/add")
    public String submitAddReviewForm(@Valid @ModelAttribute("reviewDtoInput") ReviewDtoInput reviewDtoInput, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        log.info("Request for submitting add review form");

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in add review form: {}", bindingResult.getAllErrors());
            return "review_html/add_review";
        }

        try {

            if (!movieRepository.existsById(reviewDtoInput.getMovieId())) {
                model.addAttribute("errorMessage", "Movie ID not found");
                return "review_html/add_review";
            }

            reviewService.insertReview(reviewDtoInput);
            redirectAttributes.addFlashAttribute("successMessage", "Review added successfully!");
            return "redirect:/view/review/list";
        } catch (Exception e) {
            log.error("Error adding review: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not add review. Please try again.");
            return "review_html/add_review";
        }
    }

    @GetMapping("/edit")
    public String showEditReviewForm(@RequestParam String id, Model model) {
        try {
            ReviewDto reviewDto = reviewService.getReviewById(id);
            ReviewDtoUpdate reviewDtoUpdate = ReviewDtoUpdate.builder()
                    .rating(reviewDto.getRating())
                    .comment(reviewDto.getComment())
                    .build();
            model.addAttribute("reviewDtoUpdate", reviewDtoUpdate);
            model.addAttribute("id", id);
            return "review_html/edit_review";
        } catch (Exception e) {
            log.error("Review not found for id: {}", id);
            model.addAttribute("errorMessage", "Could not find review. Please try again.");
            return "review_html/id_not_found";
        }
    }

    @PostMapping("/update")
    public String updateReview(@RequestParam String id,
                               @Valid @ModelAttribute("reviewDtoUpdate") ReviewDtoUpdate reviewDtoUpdate,
                               BindingResult bindingResult, Model model) {
        log.info("Request for updating review with id {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in update review form: {}", bindingResult.getAllErrors());
            model.addAttribute("id", id);
            return "review_html/edit_review";
        }

        try {
            reviewService.updateReview(reviewDtoUpdate, id);
            return "redirect:/view/review/list?success";
        } catch (Exception e) {
            log.error("Error updating review: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not update review. Please try again.");
            model.addAttribute("id", id);
            return "review_html/edit_review";
        }
    }

    @GetMapping("/delete")
    public String deleteReview(@RequestParam String id, Model model) {
        log.info("Request for deleting review with id {}", id);

        try {
            reviewService.deleteReview(id);
            return "redirect:/view/review/list?success";
        } catch (Exception e) {
            log.error("Error deleting review: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not delete review. Please try again.");
            return "review_html/id_not_found";
        }
    }
}