/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        slate: {
          950: '#020617',
          900: '#0f172a',
          800: '#1e293b',
          700: '#334155',
          600: '#475569',
          500: '#64748b',
          400: '#94a3b8',
          300: '#cbd5e1',
          200: '#e2e8f0',
        },
        cyan: {
          300: '#67e8f9',
          400: '#22d3ee',
          500: '#06b6d4',
          600: '#0891b2',
          950: '#083344',
        }
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
        '4xl': '2rem',
      },
      boxShadow: {
        'neon-cyan': '0 0 20px rgba(6, 182, 212, 0.45)',
        'neon-purple': '0 0 20px rgba(168, 85, 247, 0.45)',
        'neon-gold': '0 0 25px rgba(234, 179, 8, 0.5)',
        'elegant-cyan': '0 8px 24px rgba(6, 182, 212, 0.3)',
        'glow-subtle': '0 0 15px rgba(6, 182, 212, 0.15)',
        'glow-card': '0 0 20px rgba(6, 182, 212, 0.1)',
      }
    },
  },
  plugins: [],
}
