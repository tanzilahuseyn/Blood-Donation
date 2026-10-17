import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Radio, Result } from 'antd';
import { AppButtonPrimary, AppButtonSecondary, AppCard, useAppNotification } from '@helex/ui';
import { readinessApi } from '../../bloodApi';
import { evaluateReadiness, type Answers, type Outcome, type Readiness, type ReadinessQuestion, type YesNo } from './evaluate';

const CATEGORIES: { key: ReadinessQuestion['category']; title: string }[] = [
  { key: 'PREPARATION', title: 'Before you go' },
  { key: 'ELIGIBILITY', title: 'Can you give blood today?' },
];

const OUTCOMES: Record<Outcome, { status: 'success' | 'warning' | 'error'; title: string; subTitle: string }> = {
  READY: {
    status: 'success',
    title: 'Ready',
    subTitle: 'Nothing in your answers stands in the way. Centre staff make the final decision.',
  },
  PREPARE_FIRST: {
    status: 'warning',
    title: 'Prepare first',
    subTitle: 'You can probably give blood today once you have done the following.',
  },
  NOT_TODAY: {
    status: 'error',
    title: 'Not today',
    subTitle: 'Something in your answers means you would likely be turned away today.',
  },
};

/**
 * The readiness questionnaire — BLOOD.01 § UI, Questionnaire.
 *
 * PRIVACY: the only request is loading the questions. Answers live in this
 * component's state and nowhere else — not localStorage, not sessionStorage,
 * not the URL, not a request. Leaving or reloading the page forgets them.
 */
export const ReadinessQuestionnaire = () => {
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [questions, setQuestions] = useState<ReadinessQuestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [answers, setAnswers] = useState<Answers>({});
  const [result, setResult] = useState<Readiness | null>(null);

  useEffect(() => {
    readinessApi
      .questions()
      .then(setQuestions)
      .catch((e) => notify.error('Could not load the questions', e.message))
      .finally(() => setLoading(false));
  }, [notify]);

  const answer = (code: string, value: YesNo) => {
    setAnswers((prev) => ({ ...prev, [code]: value }));
    setResult(null); // a changed answer invalidates the shown outcome
  };

  const allAnswered = questions.length > 0 && questions.every((q) => answers[q.questionCode]);

  const startOver = () => {
    setAnswers({});
    setResult(null);
  };

  return (
    <div style={{ maxWidth: 720, margin: '24px auto', display: 'grid', gap: 16 }}>
      <Alert
        type="info"
        showIcon
        message="Am I ready to give blood?"
        description="This is guidance only — the staff at the donation centre always make the final decision. Your answers stay in this browser tab: they are not sent anywhere or stored, and they are gone when you leave or reload the page."
      />

      {CATEGORIES.map(({ key, title }) => {
        const inCategory = questions.filter((q) => q.category === key);
        if (inCategory.length === 0) return null;
        return (
          <AppCard key={key} title={title} loading={loading}>
            <div style={{ display: 'grid', gap: 16 }}>
              {inCategory.map((q) => (
                <div key={q.questionCode} style={{ display: 'grid', gap: 6 }}>
                  <span>{q.questionText}</span>
                  <Radio.Group
                    value={answers[q.questionCode]}
                    onChange={(e) => answer(q.questionCode, e.target.value as YesNo)}
                    optionType="button"
                    options={[
                      { value: 'YES', label: 'Yes' },
                      { value: 'NO', label: 'No' },
                    ]}
                  />
                </div>
              ))}
            </div>
          </AppCard>
        );
      })}

      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
        <AppButtonSecondary onClick={() => navigate('/centres')}>Find a centre</AppButtonSecondary>
        <AppButtonSecondary onClick={startOver} disabled={Object.keys(answers).length === 0}>
          Start over
        </AppButtonSecondary>
        {/* Evaluates here, in the browser — no request is made. */}
        <AppButtonPrimary disabled={!allAnswered} onClick={() => setResult(evaluateReadiness(questions, answers))}>
          Check my readiness
        </AppButtonPrimary>
      </div>

      {result && (
        <AppCard>
          <Result
            status={OUTCOMES[result.outcome].status}
            title={OUTCOMES[result.outcome].title}
            subTitle={OUTCOMES[result.outcome].subTitle}
          >
            {result.advice.length > 0 && (
              <ul style={{ margin: 0, paddingLeft: 20, display: 'grid', gap: 6 }}>
                {result.advice.map((a) => (
                  <li key={a}>{a}</li>
                ))}
              </ul>
            )}
          </Result>
        </AppCard>
      )}
    </div>
  );
};
