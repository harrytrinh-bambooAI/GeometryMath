import js from '@eslint/js';
import reactHooks from 'eslint-plugin-react-hooks';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    languageOptions: { ecmaVersion: 2023, globals: { ...globals.browser, ...globals.node } },
    plugins: { 'react-hooks': reactHooks },
    rules: {
      ...reactHooks.configs.recommended.rules,
      // Giờ hiển thị chỉ qua src/shared/time (BR-GEO-233): cấm tự định dạng ngày giờ ngoài helper.
      'no-restricted-syntax': [
        'error',
        {
          selector: "CallExpression[callee.property.name=/^toLocale(String|DateString|TimeString)$/]",
          message: 'Dùng formatVnDateTime/formatVnDate/formatVnTime trong src/shared/time (BR-GEO-233).',
        },
      ],
    },
  },
  {
    files: ['src/shared/time/**'],
    rules: { 'no-restricted-syntax': 'off' },
  },
);
