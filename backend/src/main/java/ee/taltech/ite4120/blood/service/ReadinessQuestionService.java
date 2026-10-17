package ee.taltech.ite4120.blood.service;

import ee.taltech.ite4120.blood.model.ReadinessQuestion;
import ee.taltech.ite4120.blood.repository.ReadinessQuestionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Serves the readiness questionnaire's content — business test 3 in
 * docs/src/specifications/BLOOD.01. There is no method that takes answers:
 * they are evaluated in the browser and never sent here (privacy constraint).
 */
@Service
public class ReadinessQuestionService {

    private final ReadinessQuestionRepository repository;

    public ReadinessQuestionService(ReadinessQuestionRepository repository) {
        this.repository = repository;
    }

    /** The active questions, in display order. */
    public List<ReadinessQuestion> activeQuestions() {
        return repository.active();
    }
}
