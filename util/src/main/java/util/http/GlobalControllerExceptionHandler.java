package util.http;

/*
 * =========================================
 * GLOBAL CONTROLLER EXCEPTION HANDLER
 * =========================================
 *
 * Purpose of this class:
 * - Catches exceptions thrown by controllers
 * - Converts them into clean HTTP error responses
 * - Ensures consistent error format across the app
 *
 * IMPORTANT:
 * Controllers DO NOT build error responses manually.
 * They simply throw exceptions.
 *
 * This class decides:
 * - HTTP status code
 * - Error response body
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;

/*
 * @RestControllerAdvice
 *
 * This annotation makes this class:
 * - GLOBAL for all controllers
 * - Automatically intercept exceptions
 * - Return JSON responses (like @RestController)
 *
 * Think of it as:
 * "Try–Catch for the entire application"
 */
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    /*
     * Logger used for debugging and observability.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    /*
     * ===========================
     * NOT FOUND (404) HANDLER
     * ===========================
     *
     * Triggered when a NotFoundException is thrown
     * anywhere in any controller.
     */
    @ResponseStatus(NOT_FOUND)   // Sets HTTP status = 404
    @ExceptionHandler(NotFoundException.class) // Exception to catch
    public @ResponseBody HttpErrorInfo handleNotFoundExceptions(
            ServerHttpRequest request,
            NotFoundException ex) {

        /*
         * Build and return standardized error response.
         */
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }

    /*
     * ===========================
     * INVALID INPUT (422) HANDLER
     * ===========================
     *
     * Triggered when InvalidInputException is thrown.
     */
    @ResponseStatus(UNPROCESSABLE_ENTITY) // Sets HTTP status = 422
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputExceptions(
            ServerHttpRequest request,
            InvalidInputException ex) {

        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    /*
     * ===========================
     * COMMON ERROR RESPONSE BUILDER
     * ===========================
     *
     * Creates a standardized HTTP error response body.
     *
     * Error response contains:
     * - HTTP status
     * - Request path
     * - Human-readable message
     */
    private HttpErrorInfo createHttpErrorInfo(
            HttpStatus status,
            ServerHttpRequest request,
            Exception ex) {

        /*
         * Extract the request path that caused the error.
         * Example: /product-composite/123
         */
        final String path =
                request.getPath().pathWithinApplication().value();

        /*
         * Message from the thrown exception.
         */
        final String message = ex.getMessage();

        /*
         * Log the error details (debug level).
         * Useful for troubleshooting.
         */
        LOG.debug(
                "Returning HTTP status: {} for path: {}, message: {}",
                status,
                path,
                message
        );

        /*
         * Build and return error response object.
         * This will be serialized to JSON automatically.
         */
        return new HttpErrorInfo(status, path, message);
    }
}