package com.luisborrayo.catalogo_peliculasseries.repositories;

import com.luisborrayo.catalogo_peliculasseries.model.MovieGenre;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class MovieGenreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public MovieGenre save(MovieGenre genre) {
        if (genre.getMovieGenreId() == null) {
            entityManager.persist(genre);
            return genre;
        } else {
            return entityManager.merge(genre);
        }
    }

    public Optional<MovieGenre> findById(Long id) {
        return Optional.ofNullable(entityManager.find(MovieGenre.class, id));
    }

    public Optional<MovieGenre> findByGenreName(String genreName) {
        try {
            TypedQuery<MovieGenre> query = entityManager.createQuery(
                    "SELECT mg FROM MovieGenre mg WHERE mg.genreName = :genreName",
                    MovieGenre.class
            );
            query.setParameter("genreName", genreName);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<MovieGenre> findAll() {
        return entityManager.createQuery("SELECT mg FROM MovieGenre mg", MovieGenre.class)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(mg) FROM MovieGenre mg", Long.class)
                .getSingleResult();
    }

    public void delete(MovieGenre genre) {
        if (entityManager.contains(genre)) {
            entityManager.remove(genre);
        } else {
            entityManager.remove(entityManager.merge(genre));
        }
    }
}