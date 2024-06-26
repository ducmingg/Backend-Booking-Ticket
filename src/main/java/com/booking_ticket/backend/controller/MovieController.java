package com.booking_ticket.backend.controller;

import com.booking_ticket.backend.Exception.NotFoundException;
import com.booking_ticket.backend.dto.MovieDto;
import com.booking_ticket.backend.entity.Movie;
import com.booking_ticket.backend.entity.Screening;
import com.booking_ticket.backend.entity.Theater;
import com.booking_ticket.backend.entity.UsernameCurrent;
import com.booking_ticket.backend.service.CloudinaryService;
import com.booking_ticket.backend.service.MovieService;
import com.booking_ticket.backend.service.TheaterService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class MovieController {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MovieService movieService;
    @Autowired
    TheaterService theaterService;
    @Autowired
    CloudinaryService cloudinaryService;

    @GetMapping("/theaters/{theaterId}/movies")
    public ResponseEntity<List<MovieDto>> getMovies(@PathVariable(value = "theaterId") Long theaterId) {
        if (!theaterService.existsTheaterId(theaterId)) {
            List<MovieDto> movies = null;
            return new ResponseEntity<List<MovieDto>>(movies, HttpStatus.NOT_FOUND);
        }
        List<MovieDto> movies = movieService
                .findByTheaterId(theaterId)
                .stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @PostMapping(path = "/theaters/{theaterId}/movies", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Movie> createMovies(@PathVariable(value = "theaterId") Long theaterId,
                                              @RequestPart("movie") Movie movie,
                                              @RequestPart("image") MultipartFile file) throws IOException {
        Theater re = theaterService.findById(theaterId).orElseThrow(() -> new NotFoundException("Not found theater_id"));

        Movie movie1 = new Movie();
        BeanUtils.copyProperties(movie, movie1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        movie1.setTheater(re);
        movie1.setCreate_at(now);
        movie1.setCreate_by(new UsernameCurrent().usernameCurrent);
        movie1.setUrlImg(this.cloudinaryService.upLoadFile(file));
        movieService.saveMovie(movie1);
        return new ResponseEntity<>(movie1, HttpStatus.OK);
    }

    @GetMapping("/movie/{movie_id}")
    public ResponseEntity<MovieDto> getById(@PathVariable(value = "movie_id") Long movie_id) {
        Movie movie = movieService.getById(movie_id);
        MovieDto movieDto = modelMapper.map(movie, MovieDto.class);
        return new ResponseEntity<>(movieDto, HttpStatus.OK);
    }


}