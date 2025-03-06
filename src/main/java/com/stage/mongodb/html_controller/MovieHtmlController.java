package com.stage.mongodb.html_controller;

import com.stage.mongodb.dto.MovieDto;
import com.stage.mongodb.dto.MovieDtoInput;
import com.stage.mongodb.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/view/movie")
public class MovieHtmlController {

    private final MovieService movieService;

    @GetMapping("/home")
    public String homeMovies() {
        log.info("Request for showing movie home page");
        return "movie_html/movie_index";
    }

    @GetMapping("/list")
    public String viewMovies(Model model) {
        log.info("Request for showing movie list view");
        List<MovieDto> moviesDto = movieService.getMovies();
        model.addAttribute("movies", moviesDto);
        return "movie_html/movie_list";
    }

    @GetMapping("/details")
    public String viewMovieDetails(@RequestParam String id, Model model) {
        log.info("Request for showing movie details view for id {}", id);

        try {
            MovieDto movieDto = movieService.getMovieById(id);
            model.addAttribute("movie", movieDto);
            return "movie_html/movie_details";
        } catch (Exception e) {
            log.error("Movie not found for id: {}", id);
            model.addAttribute("errorMessage", "Could not find movie. Please try again.");
            return "movie_html/id_not_found";
        }
    }

    @GetMapping("/add")
    public String showAddMovieForm(Model model) {
        MovieDtoInput movieDtoInput = MovieDtoInput.builder()
                .title("Example Title")
                .releaseDate("2025-01-01")
                .build();

        model.addAttribute("movieDtoInput", movieDtoInput);
        return "movie_html/add_movie";
    }

    @PostMapping("/add")
    public String submitAddMovieForm(@Valid @ModelAttribute("movieDtoInput") MovieDtoInput movieDtoInput,
                                     BindingResult bindingResult, Model model) {
        log.info("Request for submitting add movie form");

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in add movie form: {}", bindingResult.getAllErrors());
            return "movie_html/add_movie";
        }

        try {
            movieService.insertMovie(movieDtoInput);
            return "redirect:/view/movie/list?success";
        } catch (Exception e) {
            log.error("Error adding movie: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not add movie. Please try again.");
            return "movie_html/add_movie";
        }
    }

    @GetMapping("/edit")
    public String showEditMovieForm(@RequestParam String id, Model model) {

        try {
            MovieDto movieDto = movieService.getMovieById(id);
            model.addAttribute("movieDtoInput", movieDto);
            return "movie_html/edit_movie";
        } catch (Exception e) {
            log.error("Movie not found for id: {}", id);
            model.addAttribute("errorMessage", "Could not find movie. Please try again.");
            return "movie_html/id_not_found";
        }
    }

    @PostMapping("/update")
    public String updateMovie(@RequestParam String id,
                              @Valid @ModelAttribute("movieDtoInput") MovieDtoInput movieDtoInput,
                              BindingResult bindingResult, Model model) {
        log.info("Request for updating movie with id {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in update movie form: {}", bindingResult.getAllErrors());
            model.addAttribute("id", id);
            return "movie_html/edit_movie";
        }

        try {
            movieService.updateMovie(movieDtoInput, id);
            return "redirect:/view/movie/list?success";
        } catch (Exception e) {
            log.error("Error updating movie: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not update movie. Please try again.");
            model.addAttribute("id", id);
            return "movie_html/edit_movie";
        }
    }
/*
    @GetMapping("/patch")
    public String showPatchMovieForm(@RequestParam String id, Model model) {
        try {
            MovieDto movieDto = movieService.getMovieById(id);
            model.addAttribute("moviePatchDto", movieDto);
            return "movie_html/patch_movie";
        } catch (Exception e) {
            log.error("Movie not found for id: {}", id);
            model.addAttribute("errorMessage", "Could not find movie. Please try again.");
            return "movie_html/id_not_found";
        }
    }


    @PostMapping("/patch")
    public String patchMovie(@RequestParam String id,
                             @Valid @ModelAttribute("moviePatchDto") MoviePatchDto moviePatchDto,
                             BindingResult bindingResult, Model model) {
        log.info("Request for partial update of movie with id {}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in patch movie form: {}", bindingResult.getAllErrors());
            model.addAttribute("id", id);
            return "movie_html/patch_movie";
        }

        try {
            movieService.updateMoviePartial(id, moviePatchDto);
            return "redirect:/view/movie/list?success";
        } catch (Exception e) {
            log.error("Error updating movie partially: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not update movie. Please try again.");
            model.addAttribute("id", id);
            return "movie_html/patch_movie";
        }
    }*/


    // Metodo per eliminare un film (DELETE)
    @GetMapping("/delete")
    public String deleteMovie(@RequestParam String id, Model model) {
        log.info("Request for deleting movie with id {}", id);

        try {
            movieService.deleteMovie(id);
            return "redirect:/view/movie/list?success";
        } catch (Exception e) {
            log.error("Error deleting movie: {}", e.getMessage());
            model.addAttribute("errorMessage", "Could not delete movie. Please try again.");
            return "movie_html/id_not_found";
        }
    }
}