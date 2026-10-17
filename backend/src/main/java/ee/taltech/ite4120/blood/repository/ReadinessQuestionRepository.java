package ee.taltech.ite4120.blood.repository;

import ee.taltech.ite4120.blood.model.ReadinessQuestion;
import java.util.List;
import org.helex.commons.db.bean.PgBeanProcessor;
import org.helex.commons.db.repo.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Reads the readiness questions. Read-only on purpose: questions arrive by
 * changeset, and there is nothing for answers to be written to.
 */
@Repository
public class ReadinessQuestionRepository extends BaseRepository {

    private final PgBeanProcessor bp = new PgBeanProcessor(ReadinessQuestion.class);

    public List<ReadinessQuestion> active() {
        return getBeans("""
                select q.id, q.question_code, q.category, q.question_text, q.concern_answer,
                       q.severity, q.advice, q.display_order
                  from blood.readiness_question q
                 where q.sys_status = 'A'
                 order by q.display_order, q.question_code""", bp);
    }
}
