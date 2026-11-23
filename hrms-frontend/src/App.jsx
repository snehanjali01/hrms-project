import { useState } from "react";
import Splash from "./Splash";
import Login from "./Login";
import Dashboard from "./Dashboard";
import { clearAuth, loadAuth, saveAuth } from "./auth";

function App() {
  const [showSplash, setShowSplash] = useState(true);
  const [user, setUser] = useState(() => loadAuth());

  function handleLoginSuccess(loginResponse) {
    saveAuth(loginResponse);
    setUser(loginResponse);
  }

  function handleLogout() {
    clearAuth();
    setUser(null);
  }

  if (showSplash) {
    return <Splash onFinish={() => setShowSplash(false)} />;
  }

  if (!user) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return <Dashboard user={user} onLogout={handleLogout} />;
}

export default App;
