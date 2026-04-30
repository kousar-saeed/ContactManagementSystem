import { Link } from "react-router-dom";

export default function AppShell({ children }) {
  return (
    <div style={{ fontFamily: "system-ui, Arial, sans-serif", padding: 24 }}>
      <nav style={{ display: "flex", gap: 12, marginBottom: 16 }}>
        <Link to="/">Home</Link>
        <Link to="/contacts">Contacts</Link>
      </nav>
      <main>{children}</main>
    </div>
  );
}

