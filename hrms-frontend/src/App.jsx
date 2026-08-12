import { useState } from "react";
import Splash from "./Splash";
import Login from "./Login";
import Dashboard from "./Dashboard";

function App() {
  const [showSplash, setShowSplash] = useState(true);
  const [user, setUser] = useState(null);

  if (showSplash) {
    return <Splash onFinish={() => setShowSplash(false)} />;
  }

  if (!user) {
    return <Login onLoginSuccess={(userData) => setUser(userData)} />;
  }

  return <Dashboard user={user} />;
}

export default App;