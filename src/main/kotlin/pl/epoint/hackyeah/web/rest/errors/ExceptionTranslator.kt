package pl.epoint.hackyeah.web.rest.errors

import pl.epoint.hackyeah.web.rest.util.HeaderUtil
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.DefaultProblem
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.violations.ConstraintViolationProblem
import javax.servlet.http.HttpServletRequest
import java.util.NoSuchElementException
import java.util.stream.Collectors

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
class ExceptionTranslator : ProblemHandling {

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed
     */
    override fun process(entity: ResponseEntity<Problem>, request: NativeWebRequest): ResponseEntity<Problem>? {
        if (entity == null) {
            return entity
        }
        val problem = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return entity
        }
        val builder = Problem.builder()
                .withType(if (Problem.DEFAULT_TYPE == problem.type) ErrorConstants.DEFAULT_TYPE else problem.type)
                .withStatus(problem.status)
                .withTitle(problem.title)
                .with("path", request.getNativeRequest(HttpServletRequest::class.java)!!.requestURI)

        if (problem is ConstraintViolationProblem) {
            builder
                    .with("violations", problem.violations)
                    .with("message", ErrorConstants.ERR_VALIDATION)
        } else {
            builder
                    .withCause((problem as DefaultProblem).cause)
                    .withDetail(problem.detail)
                    .withInstance(problem.instance)
            problem.parameters.forEach { key, value -> builder.with(key, value) }
            if (!problem.parameters.containsKey("message") && problem.status != null) {
                builder.with("message", "error.http." + problem.status!!.statusCode)
            }
        }
        return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
    }

    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, request: NativeWebRequest): ResponseEntity<Problem> {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors.stream()
                .map { f -> FieldErrorVM(f.objectName, f.field, f.code!!) }
                .collect(Collectors.toList())

        val problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("Method argument not valid")
                .withStatus(defaultConstraintViolationStatus())
                .with("message", ErrorConstants.ERR_VALIDATION)
                .with("fieldErrors", fieldErrors)
                .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleNoSuchElementException(ex: NoSuchElementException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .with("message", ErrorConstants.ENTITY_NOT_FOUND_TYPE)
                .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleBadRequestAlertException(ex: BadRequestAlertException, request: NativeWebRequest): ResponseEntity<Problem> {
        return create(ex, request, HeaderUtil.createFailureAlert(ex.entityName, ex.errorKey, ex.message!!))
    }

    @ExceptionHandler
    fun handleConcurrencyFailure(ex: ConcurrencyFailureException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
                .withStatus(Status.CONFLICT)
                .with("message", ErrorConstants.ERR_CONCURRENCY_FAILURE)
                .build()
        return create(ex, problem, request)
    }
}
