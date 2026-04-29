import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    create_organizations_staged: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 250 },
        { duration: '1m', target: 500 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },

  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';

export default function () {
  const unique = `${__VU}-${__ITER}-${Date.now()}`;

  const payload = JSON.stringify({
    name: `LOAD_TEST_Org_${unique}`,
    website: `https://load-test-${unique}.example.com`,
    websiteStatus: 'working',
    linkedinUrl: null,
    countryRegion: 'CH',
    email: `load-test-${unique}@example.com`,
    category: 'mobility-fleet-management',
    status: 'active',
    notes: 'created from k6 load test',
    preferredLanguage: 'EN',
  });

  const params = {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {}),
    },
    tags: {
      endpoint: 'POST /api/organizations',
      test_type: 'create_throughput',
    },
  };

  const res = http.post(
    `${BASE_URL}/api/organizations`,
    payload,
    params
  );

  check(res, {
    'created': (r) => r.status === 201 || r.status === 200,
    'not bad request': (r) => r.status !== 400,
    'not unauthorized': (r) => r.status !== 401 && r.status !== 403,
    'not server error': (r) => r.status < 500,
  });

  sleep(1);
}