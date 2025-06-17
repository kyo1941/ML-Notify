module.exports = {
    preset: 'ts-jest',
    testEnvironment: 'node',
    testMatch: [
      '**/__tests__/**/*.test.[jt]s?(x)',
      '**/__tests__/**/*.spec.[jt]s?(x)',
    ],
    testPathIgnorePatterns: [
      '/node_modules/',
      '/lib/'
    ],
    moduleNameMapper: {
    },
    clearMocks: true,
  };