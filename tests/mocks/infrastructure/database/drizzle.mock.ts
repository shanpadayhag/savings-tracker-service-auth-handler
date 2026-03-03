import { jest } from '@jest/globals';
import type { PostgresJsDatabase } from 'drizzle-orm/postgres-js';

const mockDBReturning = jest.fn<() => Promise<unknown[]>>();
const mockDBValues = jest.fn((_values: unknown) => ({ returning: mockDBReturning }));
const mockDBInsert = jest.fn(() => ({ values: mockDBValues }));

const mockDBWhere = jest.fn(() => ({ returning: mockDBReturning }));
const mockDBSet = jest.fn(() => ({ where: mockDBWhere }));
const mockDBUpdate = jest.fn(() => ({ set: mockDBSet }));

const mockDBExecute = jest.fn<() => Promise<unknown[]>>();
const mockDBWhere2 = jest.fn(() => ({ where: mockDBExecute }));
const mockDBFrom = jest.fn(() => ({ where: mockDBWhere2 }));
const mockDBSelect = jest.fn(() => ({ from: mockDBFrom }));

const defaults = {
  insert: mockDBInsert,
  update: mockDBUpdate,
  select: mockDBSelect,
};

export const createDrizzleMock = (
  overrides: Partial<typeof defaults> = {}
): PostgresJsDatabase =>
  ({
    ...defaults,
    ...overrides,
  }) as unknown as PostgresJsDatabase;

export const drizzleMockFns = {
  insert: { mockDBInsert, mockDBValues, mockDBReturning },
  update: { mockDBUpdate, mockDBSet, mockDBWhere },
  select: { mockDBSelect, mockDBFrom, mockDBWhere2, mockDBExecute },
};
