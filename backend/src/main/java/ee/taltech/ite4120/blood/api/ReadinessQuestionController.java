package ee.taltech.ite4120.blood.api;

import ee.taltech.ite4120.blood.model.ReadinessQuestion;
import ee.taltech.ite4120.blood.service.ReadinessQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The readiness questionnaire's content. Read-only, and deliberately so: there is
 * no endpoint that accepts answers (BLOOD.01, privacy constraint). They are
 * evaluated in the browser.
 */
@RestController
@RequestMapping("/api/readiness-questions")
@Tag(name = "Readiness questions", description = "The questionnaire's questions and advice — answers never come here")
public class ReadinessQuestionController {

    private final ReadinessQuestionService service;

    public ReadinessQuestionController(ReadinessQuestionService service) {
        this.service = service;
    }

    @Operation(summary = "The active questions, in display order")
    @GetMapping
    public List<ReadinessQuestion> active() {
        return service.activeQuestions();
    }
}
