/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#0F6E56",
          hover: "#085041",
          light: "#E1F5EE",
        },
        danger: {
          DEFAULT: "#A32D2D",
          light: "#FDECEA",
        },
        warning: {
          DEFAULT: "#854F0B",
          light: "#FAEEDA",
        },
        info: {
          DEFAULT: "#2E75B6",
          light: "#D5E8F0",
        },
        surface: {
          DEFAULT: "#FFFFFF",
          secondary: "#F5F5F5",
          tertiary: "#E8E8E8",
        },
        text: {
          DEFAULT: "#1A1A1A",
          muted: "#555555",
          hint: "#999999",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
      },
      borderRadius: {
        DEFAULT: "0.5rem",
        md: "0.5rem",
        lg: "0.75rem",
        xl: "1rem",
      },
    },
  },
  plugins: [],
};
