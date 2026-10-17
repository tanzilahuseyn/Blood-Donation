/**
 * The readiness rule of BLOOD.01, as one pure function. It runs only in the
 * browser: the answers never leave the page, are never stored and are never sent
 * to the server (BLOOD.01, privacy constraint). Tested by evaluate.test.ts.
 */

export type YesNo = 'YES' | 'NO';
export type Severity = 'ADVISORY' | 'BLOCKING';
export type Outcome = 'READY' | 'PREPARE_FIRST' | 'NOT_TODAY';

/** One question as served by GET /api/readiness-questions. */
export interface ReadinessQuestion {
  questionCode: string;
  category: 'PREPARATION' | 'ELIGIBILITY';
  questionText: string;
  /** The answer that raises a concern. */
  concernAnswer: YesNo;
  severity: Severity;
  advice: string;
  displayOrder: number;
}

/** The person's answers, keyed by question code. Held in component state only. */
export type Answers = Record<string, YesNo>;

export interface Readiness {
  outcome: Outcome;
  /** The advice of every raised concern, in question order. */
  advice: string[];
}

/**
 * A concern is raised when the answer equals the question's concernAnswer. The
 * outcome follows the most serious concern raised: any BLOCKING one means
 * Not today; otherwise any ADVISORY one means Prepare first; none means Ready.
 */
export function evaluateReadiness(questions: ReadinessQuestion[], answers: Answers): Readiness {
  const raised = questions.filter((q) => answers[q.questionCode] === q.concernAnswer);
  const outcome: Outcome = raised.some((q) => q.severity === 'BLOCKING')
    ? 'NOT_TODAY'
    : raised.length > 0
      ? 'PREPARE_FIRST'
      : 'READY';
  return { outcome, advice: raised.map((q) => q.advice) };
}
