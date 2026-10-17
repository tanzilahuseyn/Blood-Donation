// The blood component's API client — thin fetch wrappers, like api.ts for animals.
// There is deliberately no call that sends questionnaire answers: they are
// evaluated in the browser (pages/readiness/evaluate.ts) and never leave it.

import { authHeaders, handle, type QueryResult } from './api';
import type { ReadinessQuestion } from './pages/readiness/evaluate';

export interface DonationCentre {
  id: number;
  centreCode: string;
  name: string;
  regionCode: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  openingHours?: string | null;
  phone?: string | null;
  operatorRegistryCode?: string | null;
  /** Platform columns, filled by the database trigger; read-only. */
  sysStatus?: string;
  sysVersion?: number;
  sysCreatedAt?: string;
  sysCreatedBy?: string;
  sysModifiedAt?: string;
  sysModifiedBy?: string;
}

export type CentreBody = Omit<DonationCentre, 'id' | 'sysStatus' | 'sysVersion' | 'sysCreatedAt' | 'sysCreatedBy' | 'sysModifiedAt' | 'sysModifiedBy'>;

export interface OperatorInfo {
  registryCode: string;
  legalName: string;
  address: string;
}

export interface Region {
  code: string;
  name: string;
}

const json = { 'Content-Type': 'application/json' };

export const centresApi = {
  list: (textContains?: string, regionCode?: string): Promise<QueryResult<DonationCentre>> => {
    const q = new URLSearchParams({ limit: '50', sort: 'name' });
    if (textContains) q.set('textContains', textContains);
    if (regionCode) q.set('regionCode', regionCode);
    return fetch(`/api/centres?${q}`, { headers: authHeaders() }).then((r) =>
      handle<QueryResult<DonationCentre>>(r),
    );
  },

  get: (id: number): Promise<DonationCentre> =>
    fetch(`/api/centres/${id}`, { headers: authHeaders() }).then((r) => handle<DonationCentre>(r)),

  create: (body: CentreBody): Promise<DonationCentre> =>
    fetch('/api/centres', { method: 'POST', headers: { ...authHeaders(), ...json }, body: JSON.stringify(body) }).then(
      (r) => handle<DonationCentre>(r),
    ),

  /** PUT — the centre code is immutable; the backend keeps the stored one whatever is sent. */
  update: (id: number, body: CentreBody): Promise<DonationCentre> =>
    fetch(`/api/centres/${id}`, {
      method: 'PUT',
      headers: { ...authHeaders(), ...json },
      body: JSON.stringify(body),
    }).then((r) => handle<DonationCentre>(r)),

  /** DELETE — a soft delete: 204, and the centre code becomes reusable. */
  retire: (id: number): Promise<void> =>
    fetch(`/api/centres/${id}`, { method: 'DELETE', headers: authHeaders() }).then((r) =>
      r.ok ? undefined : handle<void>(r),
    ),

  /**
   * The operator from the business register. A miss — no operator code, or an
   * organisation the register does not know — is 200 with an EMPTY body: null here.
   * The register being down is a 502, which rejects; callers just leave it out.
   */
  operator: async (id: number): Promise<OperatorInfo | null> => {
    const r = await fetch(`/api/centres/${id}/operator`, { headers: authHeaders() });
    if (!r.ok) return handle<null>(r);
    const text = await r.text();
    return text ? (JSON.parse(text) as OperatorInfo) : null;
  },

  regions: (): Promise<Region[]> =>
    fetch('/api/centres/regions', { headers: authHeaders() }).then((r) => handle<Region[]>(r)),
};

export const readinessApi = {
  /** The only questionnaire request: loading the questions. Answers are never sent. */
  questions: (): Promise<ReadinessQuestion[]> =>
    fetch('/api/readiness-questions', { headers: authHeaders() }).then((r) => handle<ReadinessQuestion[]>(r)),
};
