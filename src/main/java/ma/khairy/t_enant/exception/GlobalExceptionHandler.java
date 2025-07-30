package ma.khairy.t_enant.exception;

import org.springframework.dao.InvalidDataAccessResourceUsageException; // Importez cette classe
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Object> handleTenantNotFoundException(
            TenantNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * NOUVEAU GESTIONNAIRE
     * Gère les erreurs où la table n'existe pas dans un schéma qui, lui, existe.
     * C'est une erreur de configuration interne, donc on renvoie un 500.
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<Object> handleInvalidDataAccessResourceUsageException(
            InvalidDataAccessResourceUsageException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        // On vérifie si l'exception est bien celle que l'on attend pour donner un message plus précis
        if (ex.getCause() instanceof org.hibernate.exception.SQLGrammarException) {
            body.put("message", "Erreur de configuration du serveur : une table requise est manquante pour le tenant actuel.");
        } else {
            body.put("message", "Erreur d'accès aux données.");
        }
        body.put("details", "Une erreur de syntaxe SQL ou de ressource de données s'est produite.");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * NOUVEAU GESTIONNAIRE
     * Gère le cas où l'en-tête X-TenantID est manquant.
     */
    @ExceptionHandler(MissingTenantException.class)
    public ResponseEntity<Object> handleMissingTenantException(MissingTenantException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}