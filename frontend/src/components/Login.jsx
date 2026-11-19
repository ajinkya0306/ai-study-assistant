import React, { useState } from "react";
import { apiRequest } from "../services/api";

export default function Login({ onLogin }) {

    const [isRegister, setIsRegister] = useState(false);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const endpoint = isRegister
                ? "/api/auth/register"
                : "/api/auth/login";

            const body = isRegister
                ? { name, email, password }
                : { email, password };

            const data = await apiRequest(endpoint, {
                method: "POST",
                body: JSON.stringify(body)
            });

            localStorage.setItem("token", data.token);
            onLogin(data);

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <form className="auth-card" onSubmit={handleSubmit}>
                <h2>AI Study Assistant</h2>
                <p>{isRegister ? "Create your account" : "Welcome back"}</p>

                {isRegister && (
                    <input
                        type="text"
                        placeholder="Your name"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        required
                    />
                )}

                <input
                    type="email"
                    placeholder="Email address"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password (minimum 8 characters)"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    minLength={8}
                />

                {error && <p className="auth-error">{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading
                        ? "Please wait..."
                        : isRegister ? "Register" : "Login"}
                </button>

                <p>
                    {isRegister
                        ? "Already have an account?"
                        : "Don't have an account?"}

                    <button
                        type="button"
                        className="auth-switch"
                        onClick={() => {
                            setIsRegister(!isRegister);
                            setError("");
                        }}
                    >
                        {isRegister ? "Login" : "Register"}
                    </button>
                </p>
            </form>
        </div>
    );
}