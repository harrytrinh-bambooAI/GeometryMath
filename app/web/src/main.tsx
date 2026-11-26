import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router/dom';
import { router } from './app/router.tsx';
import './styles/tokens.css';
import './styles/base.css';
import './styles/icons.css';

const root = document.getElementById('root');
if (!root) throw new Error('Thiếu phần tử #root trong index.html');

createRoot(root).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
