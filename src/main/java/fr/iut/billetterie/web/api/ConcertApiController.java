package fr.iut.billetterie.web.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.billetterie.service.ConcertService;
import fr.iut.billetterie.web.dto.ConcertDetail;
import fr.iut.billetterie.web.dto.ConcertResume;

/** Lectures publiques des concerts. */
@RestController
@RequestMapping("/api/concerts")
public class ConcertApiController {

    private final ConcertService concerts;

    public ConcertApiController(ConcertService concerts) {
        this.concerts = concerts;
    }

    @GetMapping
    public List<ConcertResume> lister(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @RequestParam(required = false) String artiste) {
        return concerts.rechercher(du, au, artiste);
    }

    @GetMapping("/{id}")
    public ConcertDetail detail(@PathVariable Long id) {
        return concerts.detail(id);
    }
}
