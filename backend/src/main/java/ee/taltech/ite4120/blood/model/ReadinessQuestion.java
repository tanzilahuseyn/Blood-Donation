package ee.taltech.ite4120.blood.model;

/**
 * One readiness question with its advice. Maps onto {@code blood.readiness_question}
 * — see 03-readiness-question.sql. Content only: a person's answers are evaluated
 * in the browser and never reach the server (BLOOD.01, privacy constraint).
 *
 * <p>The sys columns are not exposed: questions arrive by changeset, so who
 * created them is not something the questionnaire needs to show.
 */
public class ReadinessQuestion {

    private Long id;
    private String questionCode;
    /** PREPARATION or ELIGIBILITY. */
    private String category;
    /** Phrased so it is answered yes or no. */
    private String questionText;
    /** YES or NO — the answer that raises a concern. */
    private String concernAnswer;
    /** ADVISORY (fixable before going) or BLOCKING (do not go today). */
    private String severity;
    /** What to do when the concern is raised. */
    private String advice;
    private Integer displayOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionCode() { return questionCode; }
    public void setQuestionCode(String questionCode) { this.questionCode = questionCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getConcernAnswer() { return concernAnswer; }
    public void setConcernAnswer(String concernAnswer) { this.concernAnswer = concernAnswer; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
