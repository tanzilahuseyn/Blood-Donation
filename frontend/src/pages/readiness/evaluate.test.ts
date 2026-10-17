/**
 * THE BUSINESS TEST for the readiness outcome: test 6 in
 * docs/src/specifications/BLOOD.01 § Business tests. The questionnaire is evaluated in the
 * browser and never reaches the server, so the rule is tested here rather than in
 * BloodBusinessRulesIT. Written before evaluate.ts exists; watched fail; then made green.
 *
 * Run: npx vitest run
 */
import { describe, expect, it } from 'vitest';
import { evaluateReadiness, type Answers, type ReadinessQuestion } from './evaluate';

// Given an ADVISORY and a BLOCKING question
const advisory: ReadinessQuestion = {
  questionCode: 'ATE',
  category: 'PREPARATION',
  questionText: 'Have you eaten a proper meal in the last four hours?',
  concernAnswer: 'NO',
  severity: 'ADVISORY',
  advice: 'Eat a light, low-fat meal before you go.',
  displayOrder: 1,
};

const blocking: ReadinessQuestion = {
  questionCode: 'FEVER',
  category: 'ELIGIBILITY',
  questionText: 'Have you had a fever in the last two weeks?',
  concernAnswer: 'YES',
  severity: 'BLOCKING',
  advice: 'Wait until you have been well for two weeks.',
  displayOrder: 2,
};

const questions = [advisory, blocking];

describe('the outcome follows the most serious concern raised', () => {
  it('is Ready with no advice when neither concern is raised', () => {
    const answers: Answers = { ATE: 'YES', FEVER: 'NO' };

    expect(evaluateReadiness(questions, answers)).toEqual({ outcome: 'READY', advice: [] });
  });

  it("is Prepare first with that question's advice when only the ADVISORY concern is raised", () => {
    const answers: Answers = { ATE: 'NO', FEVER: 'NO' };

    expect(evaluateReadiness(questions, answers)).toEqual({
      outcome: 'PREPARE_FIRST',
      advice: [advisory.advice],
    });
  });

  it('is Not today with its advice when only the BLOCKING concern is raised', () => {
    const answers: Answers = { ATE: 'YES', FEVER: 'YES' };

    expect(evaluateReadiness(questions, answers)).toEqual({
      outcome: 'NOT_TODAY',
      advice: [blocking.advice],
    });
  });

  it('is Not today with the advice of every raised concern when both are raised', () => {
    const answers: Answers = { ATE: 'NO', FEVER: 'YES' };

    expect(evaluateReadiness(questions, answers)).toEqual({
      outcome: 'NOT_TODAY',
      advice: [advisory.advice, blocking.advice],
    });
  });
});
